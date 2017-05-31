package foundation.omni.rest.omniwallet;


import com.fasterxml.jackson.databind.ObjectMapper;
import foundation.omni.rest.ConsensusService;
import foundation.omni.rest.omniwallet.json.AddressVerifyInfo;
import foundation.omni.rest.omniwallet.json.OmniwalletAddressBalance;
import foundation.omni.rest.omniwallet.json.OmniwalletAddressPropertyBalance;
import foundation.omni.rest.omniwallet.json.OmniwalletClientModule;
import foundation.omni.rest.omniwallet.json.PropertyVerifyInfo;
import foundation.omni.rest.omniwallet.json.RevisionInfo;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import foundation.omni.CurrencyID;
import foundation.omni.OmniDivisibleValue;
import foundation.omni.OmniIndivisibleValue;
import foundation.omni.OmniValue;
import foundation.omni.PropertyType;
import foundation.omni.rest.OmniJBalances;
import foundation.omni.rest.WalletAddressBalance;
import foundation.omni.rpc.AddressBalanceEntry;
import foundation.omni.rpc.BalanceEntry;
import foundation.omni.rpc.ConsensusSnapshot;
import foundation.omni.rpc.SmartPropertyListInfo;
import okhttp3.logging.HttpLoggingInterceptor;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.http.Query;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Omniwallet REST Java client
 */
public class OmniwalletClient implements ConsensusService {
    public static final HttpUrl omniwalletBase = HttpUrl.parse("https://www.omniwallet.org");
    public static final HttpUrl stagingBase = HttpUrl.parse("https://staging.omniwallet.org");
    static final int CONNECT_TIMEOUT_MILLIS = 15 * 1000; // 15s
    static final int READ_TIMEOUT_MILLIS = 20 * 1000; // 20s
    static public final int BALANCES_FOR_ADDRESSES_MAX_ADDR = 20;
    private Retrofit restAdapter;
    private OmniwalletService service;
    private Map<CurrencyID, PropertyType> cachedPropertyTypes = new HashMap<>();
    private NetworkParameters netParams;
    
    interface OmniwalletService {
        @FormUrlEncoded
        @POST("/v1/address/addr/")
        Call<OmniwalletAddressBalance> balancesForAddress_v1(@Field("addr") Address address);

        @FormUrlEncoded
        @POST("/v2/address/addr/")
        Call<Map<Address, OmniwalletAddressBalance>> balancesForAddress(@Field("addr") Address address);

        @FormUrlEncoded
        @POST("/v2/address/addr/")
        Call<Map<Address, OmniwalletAddressBalance>> balancesForAddresses(@Field("addr") List<Address> addresses);

        @GET("/v1/system/revision.json")
        Call<RevisionInfo> getRevisionInfo();

        @GET("/v1/mastercoin_verify/properties")
        Call<List<PropertyVerifyInfo>> verifyProperties();

        @GET("/v1/mastercoin_verify/addresses")
        Call<List<AddressVerifyInfo>> verifyAddresses(@Query("currency_id") String currencyId);

    }

    public OmniwalletClient() {
        this(omniwalletBase, false);
    }

    public OmniwalletClient(HttpUrl baseURL, boolean debug) {
        netParams = MainNetParams.get();
        OkHttpClient client = initClient(debug);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new OmniwalletClientModule(netParams));

        restAdapter = new Retrofit.Builder()
                .client(client)
                .baseUrl(baseURL)
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .build();

        service = restAdapter.create(OmniwalletService.class);
    }

    private OkHttpClient initClient(boolean debug) {

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                .readTimeout(READ_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);

        if (debug) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(interceptor);
        }

        return builder.build();
    }

    @Override
    public WalletAddressBalance balancesForAddress(Address address) throws IOException {
        Response<Map<Address, OmniwalletAddressBalance>> response = service.balancesForAddress(address).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Unsuccessful response in balanceInfosForAddress");
        }
        Map<Address, OmniwalletAddressBalance> resultMap = response.body();
        OmniwalletAddressBalance result = resultMap.get(address);
        return balanceEntryMapper(result);
    }

    @Override
    public OmniJBalances balancesForAddresses(List<Address> addresses) throws IOException {
        if (addresses.size() > BALANCES_FOR_ADDRESSES_MAX_ADDR) {
            throw new IllegalArgumentException("Exceeded number of allowable addresses");
        }
        OmniJBalances balances = new OmniJBalances();

        Map<Address, OmniwalletAddressBalance> owbs = service.balancesForAddresses(addresses).execute().body();
        if (owbs == null) {
            throw new IOException("Invalid response");
        }
        owbs.forEach((address, owb) -> balances.put(address, balanceEntryMapper(owb)));
        return balances;
    }


    private  WalletAddressBalance balanceEntryMapper(OmniwalletAddressBalance owb) {
        WalletAddressBalance wab = new WalletAddressBalance();
        
        for (OmniwalletAddressPropertyBalance pb : owb.getBalance()) {
            CurrencyID id = new CurrencyID(toLong(pb.getId()));
            PropertyType type =  pb.isDivisible() ? PropertyType.DIVISIBLE : PropertyType.INDIVISIBLE;
            OmniValue value = toOmniValue(toLong(pb.getValue()), type);
            wab.put(id, value);
        }
        return wab;
    }
    

    @Override
    public ConsensusSnapshot getConsensusSnapshot(CurrencyID currencyID) {
        /* Since getConsensusForCurrency() doesn't return the blockHeight, we have to check
         * blockHeight before and after the call to make sure it didn't change.
         *
         * Note: Omniwallet blockheight can lag behind Blockchain.info and Omni Core and this
         * loop does not resolve that issue, it only makes sure the reported block height
         * matches the data returned.
         */
        int beforeBlockHeight = currentBlockHeight();
        int curBlockHeight;
        SortedMap<Address, BalanceEntry> entries;
        while (true) {
            entries = this.getConsensusForCurrency(currencyID);
            curBlockHeight = currentBlockHeight();
            if (curBlockHeight == beforeBlockHeight) {
                // If blockHeight didn't change, we're done
                break;
            }
            // Otherwise we have to try again
            beforeBlockHeight = curBlockHeight;
        }
        ConsensusSnapshot snap = new ConsensusSnapshot(currencyID,
                                        curBlockHeight,
                                        "Omniwallet",
                                        consensusURI(currencyID),
                                        entries);
        return snap;
    }

    public SortedMap<Address, BalanceEntry> getConsensusForCurrency(CurrencyID currencyID) {
        List<AddressVerifyInfo> balances;
        try {
            balances = service.verifyAddresses(Long.toString(currencyID.getValue())).execute().body();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // TODO: We need an accurate, efficient way of determining divisible vs indivisible
        PropertyType propertyType;
        try {
            propertyType = lookupPropertyType(currencyID);
        } catch (IOException e) {
            throw new RuntimeException("Failure trying to fetch propertyType");
        }
        Map<Address, BalanceEntry> unsorted = balances.stream()
                .map(propertyType.equals(PropertyType.INDIVISIBLE) ? this::indivBalanceMapper : this::divisBalanceMapper)
                .collect(Collectors.toMap(
                        AddressBalanceEntry::getAddress, address -> new BalanceEntry(address.getBalance(), address.getReserved())
                ));
        SortedMap<Address, BalanceEntry> sorted = new TreeMap<>(unsorted);
        return sorted;
    }

    @Override
    public Integer currentBlockHeight() {
        RevisionInfo revisionInfo;
        try {
            revisionInfo = service.getRevisionInfo().execute().body();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String blockHeightStr = revisionInfo.getLastBlock();
        Integer blockHeight = Integer.valueOf(blockHeightStr);
        return blockHeight;
    }

    @Override
    public List<SmartPropertyListInfo> listProperties() {
        List<PropertyVerifyInfo> properties;
        try {
            properties = service.verifyProperties().execute().body();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<SmartPropertyListInfo> result = new ArrayList<>();
        properties.forEach(prop -> {
            SmartPropertyListInfo info = propertyMapper(prop);
            if (info != null)  {
                result.add(info);
            }
        });
        return result;
    }

    private AddressBalanceEntry balanceMapper(AddressVerifyInfo item, PropertyType propertyType) {

        Address address = item.getAddress();
        String balanceStr = item.getBalance();
        String reservedStr = item.getReservedBalance();

        AddressBalanceEntry balanceEntry;
        if (propertyType.equals(PropertyType.DIVISIBLE)) {
            OmniDivisibleValue balance = OmniDivisibleValue.of(new BigDecimal(balanceStr));
            OmniDivisibleValue reserved = (reservedStr != null) ?
                    OmniDivisibleValue.of(new BigDecimal(reservedStr)) :
                    OmniDivisibleValue.of(0);

            balanceEntry = new AddressBalanceEntry(address, balance, reserved);
        } else {
            BigInteger bigBalance = new BigInteger(balanceStr);
            // Workaround for Omniwallet bug where it can return balance greater than the maximum allowed for indivisible
            OmniIndivisibleValue balance;
            if (bigBalance.compareTo(BigInteger.valueOf(OmniIndivisibleValue.MAX_VALUE)) == 1) {
                balance = OmniIndivisibleValue.of(OmniIndivisibleValue.MAX_VALUE);
            } else {
                balance = OmniIndivisibleValue.of(Long.parseLong(balanceStr));
            }
            OmniIndivisibleValue reserved = (reservedStr != null) ?
                    OmniIndivisibleValue.of(Long.parseLong(reservedStr)) :
                    OmniIndivisibleValue.of(0);

            balanceEntry = new AddressBalanceEntry(address, balance, reserved);
        }
        return balanceEntry;
    }

    private AddressBalanceEntry divisBalanceMapper(AddressVerifyInfo item) {
        return balanceMapper(item, PropertyType.DIVISIBLE);
    }

    private AddressBalanceEntry indivBalanceMapper(AddressVerifyInfo item) {
        return balanceMapper(item, PropertyType.INDIVISIBLE);
    }

    private static OmniValue stringToOmniValue(String valueString) {
        boolean divisible = valueString.contains(".");  // Divisible amounts always contain a decimal point
        if (divisible) {
            return OmniValue.of(new BigDecimal(valueString), PropertyType.DIVISIBLE);
        } else {
            return OmniValue.of(Long.parseLong(valueString), PropertyType.INDIVISIBLE);
        }
    }

    private SmartPropertyListInfo propertyMapper(PropertyVerifyInfo property) {
        final String category = "";
        final String subCategory = "";
        final String data = "";
        final String url = "";
        long idnum = property.getCurrencyID();
        CurrencyID id;
        try {
            id = new CurrencyID(idnum);
        } catch (NumberFormatException e) {
            id = null;
        }
        String protocol = property.getProtocol();
        if (id != null && protocol.equals("Omni")) {
            SmartPropertyListInfo prop = new SmartPropertyListInfo(id,
                    property.getName(),
                    category,
                    subCategory,
                    data,
                    url,
                    property.isDivisible());
            return prop;
        }
        return null;
    }

    private PropertyType lookupPropertyType(CurrencyID propertyID) throws IOException {
        if (!cachedPropertyTypes.containsKey(propertyID)) {
            // If we don't have the info, fetch info for all properties from server and update
            List<SmartPropertyListInfo> infos = listProperties();
            infos.forEach(info -> {
                cachedPropertyTypes.put(info.getPropertyid(), divisibleToPropertyType(info.getDivisible()));
            });
        }
        return cachedPropertyTypes.get(propertyID);
    }


    private long toLong(Object obj) {
        return obj instanceof String ? Long.parseLong((String) obj) : (Integer) obj;
    }

    private URI consensusURI(CurrencyID currencyID) {
        HttpUrl okUrl = restAdapter
                .baseUrl()
                .newBuilder("/v1/mastercoin_verify/addresses?currency_id=" + currencyID.getValue())
                .build();
        return okUrl.uri();
    }

    private PropertyType divisibleToPropertyType(boolean divisible) {
        PropertyType type =  divisible ? PropertyType.DIVISIBLE : PropertyType.INDIVISIBLE;
        return type;
    }
    
    private OmniValue toOmniValue(long amount, PropertyType type) {
        return type.equals(PropertyType.DIVISIBLE) ?
                OmniDivisibleValue.ofWillets(amount) : OmniIndivisibleValue.ofWillets(amount);
    }
}
