package foundation.omni.rest.omniwallet;


import foundation.omni.rest.ConsensusService;
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
import java.net.URI;
import java.util.ArrayList;
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



    interface OmniwalletService {
        @FormUrlEncoded
        @POST("/v1/address/addr/")
        Call<Map<String, Object>> balancesForAddress(@Field("addr") String Address);

        @GET("/v1/system/revision.json")
        Call<Map<String, Object>> getRevisionInfo();

        @GET("/v1/mastercoin_verify/properties")
        Call<List<Map<String, Object>>> verifyProperties();

        @GET("/v1/mastercoin_verify/addresses")
        Call<List<Map<String, Object>>> verifyAddresses(@Query("currency_id") String currencyId);

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
        Response<Map<String, Object>> response = service.balancesForAddress(address.toString()).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Unsuccessful response in balanceInfosForAddress");
        }
        Map<String, Object> result = response.body();
        if (result == null) {
            return list;
        }
        List<Map<String, Object>> balances = (List<Map<String, Object>>) result.get("balance");
        balances.forEach(bal -> {
            BalanceInfo b = new BalanceInfo();
            b.id = new CurrencyID(toLong(bal.get("id")));
            b.symbol = (String) bal.get("symbol");
            boolean divisible = (boolean) bal.get("divisible");
            PropertyType type = divisible ? PropertyType.DIVISIBLE : PropertyType.INDIVISIBLE;
            b.value = toOmniValue(toLong(bal.get("value")), type);
            b.pendingneg = toOmniValue(toLong(bal.get("pendingneg")), type);
            b.pendingpos = toOmniValue(toLong(bal.get("pendingpos")), type);
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
        List<Map<String, Object>> balances;
        try {
            balances = service.verifyAddresses(Long.toString(currencyID.getValue())).execute().body();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Function<Map<String, Object>, AddressBalanceEntry> mapper;
        // TODO: We need an accurate way of determining divisible vs indivisible
        // unused balanceMapper tried to use stringToOmniValue() but apparently that didn't work
        if (currencyID.equals(CurrencyID.MAID) || currencyID.equals(CurrencyID.SEC)) {
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
        Map<String, Object> revisionInfo;
        try {
            revisionInfo = service.getRevisionInfo().execute().body();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Integer blockHeight = (Integer) revisionInfo.getOrDefault("last_block", -1);
        return blockHeight;
    }

    @Override
    public List<SmartPropertyListInfo> listProperties() {
        List<Map<String, Object>> properties;
        try {
            properties = service.verifyProperties().execute().body();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<SmartPropertyListInfo> result = new ArrayList<>();
        properties.forEach(prop -> {
            SmartPropertyListInfo info = propertyMapper(prop);
            if (info != null) {
                result.add(info);
            }
        });
        return result;
    }

    private AddressBalanceEntry balanceMapper(Map<String, Object> item) {

        Address address;
        try {
            address = Address.fromBase58(null, (String) item.get("address"));
        } catch (AddressFormatException e) {
            e.printStackTrace();
            return null;
        }

        String balanceStr = (String) item.get("balance");
        String reservedStr = (String) item.get("reserved_balance");
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

    private AddressBalanceEntry divisBalanceMapper(Map<String, Object> item) {

        Address address;
        try {
            address = Address.fromBase58(null, (String) item.get("address"));
        } catch (AddressFormatException e) {
            e.printStackTrace();
            return null;
        }

        String balanceStr = (String) item.get("balance");
        String reservedStr = (String) item.get("reserved_balance");
        OmniDivisibleValue balance = OmniDivisibleValue.of(new BigDecimal(balanceStr));
        OmniDivisibleValue reserved = (reservedStr != null) ?
                OmniDivisibleValue.of(new BigDecimal(reservedStr)) :
                OmniDivisibleValue.of(0);

        AddressBalanceEntry balanceEntry = new AddressBalanceEntry(address, balance, reserved);
        return balanceEntry;
    }

    private AddressBalanceEntry indivBalanceMapper(Map<String, Object> item) {

        Address address;
        try {
            address = Address.fromBase58(null, (String) item.get("address"));
        } catch (AddressFormatException e) {
            e.printStackTrace();
            return null;
        }

        String balanceStr = (String) item.get("balance");
        String reservedStr = (String) item.get("reserved_balance");
        OmniIndivisibleValue balance = OmniIndivisibleValue.of(Long.valueOf(balanceStr));
        OmniIndivisibleValue reserved = (reservedStr != null) ?
                OmniIndivisibleValue.of(Long.valueOf(balanceStr)) :
                OmniIndivisibleValue.of(0);

        AddressBalanceEntry balanceEntry = new AddressBalanceEntry(address, balance, reserved);
        return balanceEntry;
    }

    private static OmniValue stringToOmniValue(String valueString) {
        boolean divisible = valueString.contains(".");  // Divisible amounts always contain a decimal point
        if (divisible) {
            return OmniValue.of(new BigDecimal(valueString), PropertyType.DIVISIBLE);
        } else {
            return OmniValue.of(Long.valueOf(valueString), PropertyType.INDIVISIBLE);
        }
    }


    private SmartPropertyListInfo propertyMapper(Map<String, Object> property) {
        Number idnum = (Number) property.get("currencyID");
        CurrencyID id;
        try {
            id = new CurrencyID(idnum.longValue());
        } catch (NumberFormatException e) {
            id = null;
        }
        String protocol = (String) property.get("Protocol");
        if (id != null && protocol.equals("Omni")) {
            String name = (String) property.get("name");
            String category = "";
            String subCategory = "";
            String data = "";
            String url = "";
            Boolean divisible = null;
            SmartPropertyListInfo prop = new SmartPropertyListInfo(id,
                    name,
                    category,
                    subCategory,
                    data,
                    url,
                    divisible);
            return prop;
        }
        return null;
    }


    private long toLong(Object obj) {
        return obj instanceof String ? Long.valueOf((String) obj) : (Integer) obj;
    }

    private URI consensusURI(CurrencyID currencyID) {
        HttpUrl okUrl = restAdapter
                .baseUrl()
                .newBuilder("/v1/mastercoin_verify/addresses?currency_id=" + currencyID.getValue())
                .build();
        return okUrl.uri();
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
