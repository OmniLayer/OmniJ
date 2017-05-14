package foundation.omni.rest.omniwallet;


import foundation.omni.rest.ConsensusService;
import foundation.omni.rest.omniwallet.json.AddressVerifyInfo;
import foundation.omni.rest.omniwallet.json.OmniwalletAddressBalance;
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
import org.bitcoinj.core.AddressFormatException;
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
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Omniwallet REST Java client
 */
public class OmniwalletClient implements ConsensusService {
    static final String omniwalletBase = "https://www.omniwallet.org";
    static final String stagingBase = "https://staging.omniwallet.org";
    static final int CONNECT_TIMEOUT_MILLIS = 15 * 1000; // 15s
    static final int READ_TIMEOUT_MILLIS = 20 * 1000; // 20s
    private Retrofit restAdapter;
    private OmniwalletService service;
    private Map<CurrencyID, PropertyType> cachedPropertyTypes = new HashMap<>();

    interface OmniwalletService {
        @FormUrlEncoded
        @POST("/v1/address/addr/")
        Call<OmniwalletAddressBalance> balancesForAddress(@Field("addr") String Address);

        @GET("/v1/system/revision.json")
        Call<RevisionInfo> getRevisionInfo();

        @GET("/v1/mastercoin_verify/properties")
        Call<List<PropertyVerifyInfo>> verifyProperties();

        @GET("/v1/mastercoin_verify/addresses")
        Call<List<AddressVerifyInfo>> verifyAddresses(@Query("currency_id") String currencyId);

    }

    public OmniwalletClient() {
        OkHttpClient client = initClient();

        restAdapter = new Retrofit.Builder()
                .client(client)
                .baseUrl(omniwalletBase)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        service = restAdapter.create(OmniwalletService.class);
    }

    private OkHttpClient initClient() {
        boolean debug = false;

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
        List<BalanceInfo> infos = balanceInfosForAddress(address);
        WalletAddressBalance result = new WalletAddressBalance();
        infos.forEach(info -> result.put(info.id, info.value));
        return result;
    }

    // TODO: The returned `value` field of this method doesn't include reserved balance
    private List<BalanceInfo> balanceInfosForAddress(Address address) throws IOException {
        List<BalanceInfo> list = new ArrayList<>();
        Response<OmniwalletAddressBalance> response = service.balancesForAddress(address.toString()).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Unsuccessful response in balanceInfosForAddress");
        }
        OmniwalletAddressBalance result = response.body();
        if (result == null) {
            return list;
        }
        result.getBalance().forEach(bal -> {
            BalanceInfo b = new BalanceInfo();
            b.id = new CurrencyID(toLong(bal.getId()));
            PropertyType type =  bal.isDivisible() ? PropertyType.DIVISIBLE : PropertyType.INDIVISIBLE;
            b.value = toOmniValue(toLong(bal.getValue()), type);
            list.add(b);
        });

        return list;
    }

    @Override
    public OmniJBalances balancesForAddresses(List<Address> addresses) throws IOException {
        OmniJBalances balances = new OmniJBalances();

        for (Address address : addresses) {
            balances.put(address, balancesForAddress(address));
        }
        return balances;
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
        PropertyType propertyType = PropertyType.DIVISIBLE;
        try {
            propertyType = lookupPropertyType(currencyID);
        } catch (IOException e) {
            throw new RuntimeException("Failure trying to fetch propertyType");
        }
        Function<AddressVerifyInfo, AddressBalanceEntry> mapper;
        // TODO: We need an accurate way of determining divisible vs indivisible
        // unused balanceMapper tried to use stringToOmniValue() but apparently that didn't work
        if (propertyType == PropertyType.INDIVISIBLE) {
            mapper = this::indivBalanceMapper;
        } else {
            mapper = this::divisBalanceMapper;
        }
        Map<Address, BalanceEntry> unsorted = balances.stream()
                .map(mapper)
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

    private AddressBalanceEntry balanceMapper(AddressVerifyInfo item) {

        Address address;
        try {
            address = Address.fromBase58(null, item.getAddress());
        } catch (AddressFormatException e) {
            e.printStackTrace();
            return null;
        }

        String balanceStr = item.getBalance();
        String reservedStr = item.getReservedBalance();
        OmniValue balance = stringToOmniValue(balanceStr);
        OmniValue reserved = (reservedStr != null) ?
                                    stringToOmniValue(reservedStr) :
                                    OmniValue.of(0, balance.getPropertyType());
        // Workaround for reserved balances

        if ((balance.getPropertyType() == PropertyType.DIVISIBLE) &&
                (reserved.getPropertyType() == PropertyType.INDIVISIBLE) &&
                (reserved.getWillets() == 0)) {
            reserved = OmniDivisibleValue.of(0);
        }

        AddressBalanceEntry balanceEntry = new AddressBalanceEntry(address, balance, reserved);
        return balanceEntry;
    }

    private AddressBalanceEntry divisBalanceMapper(AddressVerifyInfo item) {

        Address address;
        try {
            address = Address.fromBase58(null, item.getAddress());
        } catch (AddressFormatException e) {
            e.printStackTrace();
            return null;
        }

        String balanceStr = item.getBalance();
        String reservedStr = item.getReservedBalance();
        OmniDivisibleValue balance = OmniDivisibleValue.of(new BigDecimal(balanceStr));
        OmniDivisibleValue reserved = (reservedStr != null) ?
                OmniDivisibleValue.of(new BigDecimal(reservedStr)) :
                OmniDivisibleValue.of(0);

        AddressBalanceEntry balanceEntry = new AddressBalanceEntry(address, balance, reserved);
        return balanceEntry;
    }

    private AddressBalanceEntry indivBalanceMapper(AddressVerifyInfo item) {

        Address address;
        try {
            address = Address.fromBase58(null, item.getAddress());
        } catch (AddressFormatException e) {
            e.printStackTrace();
            return null;
        }

        String balanceStr = item.getBalance();
        String reservedStr = item.getReservedBalance();
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

        AddressBalanceEntry balanceEntry = new AddressBalanceEntry(address, balance, reserved);
        return balanceEntry;
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
    
    private BigDecimal toBigDecimal(Object obj) {
        return obj instanceof String ? new BigDecimal((String) obj) : new BigDecimal((Integer) obj);
    }

    private OmniValue toOmniValue(long amount, PropertyType type) {
        return type.equals(PropertyType.DIVISIBLE) ?
                OmniDivisibleValue.ofWillets(amount) : OmniIndivisibleValue.ofWillets(amount);
    }

    private Address toAddress(String addressString) {
        Address a;
        try {
            a = Address.fromBase58(null, addressString);
        } catch (AddressFormatException e) {
            a = null;
        }
        return a;
    }
}
