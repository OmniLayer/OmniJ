package foundation.omni.rest.omniwallet;


import com.squareup.okhttp.OkHttpClient;
import foundation.omni.CurrencyID;
import foundation.omni.OmniDivisibleValue;
import foundation.omni.OmniIndivisibleValue;
import foundation.omni.OmniValue;
import foundation.omni.PropertyType;
import foundation.omni.rest.OmniBalanceService;
import foundation.omni.rest.OmniJBalances;
import foundation.omni.rest.WalletAddressBalance;
import foundation.omni.rpc.AddressBalanceEntry;
import foundation.omni.rpc.BalanceEntry;
import foundation.omni.rpc.ConsensusFetcher;
import foundation.omni.rpc.ConsensusSnapshot;
import foundation.omni.rpc.SmartPropertyListInfo;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import retrofit.Call;
import retrofit.Response;
import retrofit.Retrofit;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.JacksonConverterFactory;
import retrofit.http.Path;
import retrofit.http.Query;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Omniwallet RPC Java client
 */
public class OmniwalletClient implements OmniBalanceService, ConsensusFetcher {
    static final String omniwalletBase = "https://www.omniwallet.org";
    static final int CONNECT_TIMEOUT_MILLIS = 15 * 1000; // 15s
    static final int READ_TIMEOUT_MILLIS = 20 * 1000; // 20s
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
        Call<List<Map<String, Object>>> verifyAddresses(@Query("currencyId") String currencyId);

    }

    public OmniwalletClient() {
        OkHttpClient client = initClient();

        Retrofit restAdapter = new Retrofit.Builder()
                .client(client)
                .baseUrl(omniwalletBase)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
        //      .setLogLevel(Retrofit.LogLevel.FULL)    // Don't know how to do this in Retrofit 2.0

        service = restAdapter.create(OmniwalletService.class);
    }

    private OkHttpClient initClient() {
        OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(CONNECT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        client.setReadTimeout(READ_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        return client;
    }

    List<BalanceInfo> balancesForAddress(Address address) {
        Response<Map<String, Object>> response;
        Map<String, Object> result = null;

        try {
            response = service.balancesForAddress(address.toString()).execute();
            result = response.body();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        List<Map<String, Object>> balances = (List<Map<String, Object>>) result.get("balance");
        List<BalanceInfo> list = new ArrayList<BalanceInfo>();
        balances.stream().forEach(bal -> {
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
    public OmniJBalances balancesForAddresses(List<Address> addresses) {
        OmniJBalances balances = new OmniJBalances();

        addresses.stream().forEach(address -> {
            List<BalanceInfo> result = balancesForAddress(address);
            WalletAddressBalance bal = new WalletAddressBalance();
            result.stream().forEach(bi -> {
                bal.put(bi.id, bi.value);
            });
            balances.put(address, bal);
        });
        return balances;
    }

    @Override
    public ConsensusSnapshot getConsensusSnapshot(CurrencyID currencyID) {
        return null;
    }

//    public SortedMap<Address, BalanceEntry> getConsensusForCurrency(CurrencyID currencyID) {
//        List<Map<String, Object>> balances;
//        try {
//            balances = service.verifyAddresses(Long.toString(currencyID.getValue())).execute().body();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        Map<Address, BalanceEntry> unsorted = balances.stream()
//                .map(this::balanceMapper)
//                .collect(Collectors.toMap(
//                        AddressBalanceEntry::getAddress, address -> new BalanceEntry(address.getBalance(), address.getReserved())
//                ));
//        SortedMap<Address, BalanceEntry> sorted = new TreeMap<>(unsorted);
//        return sorted;
//    }

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
        List<SmartPropertyListInfo> result = properties.stream()
                .map(this::propertyMapper)
                .collect(Collectors.toList());
        return result;
    }

    private AddressBalanceEntry balanceMapper(Map<String, Object> item) {

        Address address;
        try {
            address = new Address(null, (String) item.get("address"));
        } catch (AddressFormatException e) {
            e.printStackTrace();
            return null;
        }

        BigDecimal balance = jsonToBigDecimal((String) item.get("balance"));
        BigDecimal reserved = jsonToBigDecimal((String) item.get("reserved_balance"));

        // TODO: This should distinguish between Divisible and Indivisible!!
        AddressBalanceEntry balanceEntry = new AddressBalanceEntry(address,
                OmniDivisibleValue.of(balance), OmniDivisibleValue.of(reserved));
        return balanceEntry;
    }

    private static BigDecimal jsonToBigDecimal(String balanceIn) {
        BigDecimal balanceOut =  new BigDecimal(balanceIn).setScale(8);
        return balanceOut;
    }


    private SmartPropertyListInfo propertyMapper(Map<String, Object> property) {
        Number idnum = (Number) property.get("currencyID");
        CurrencyID id;
        try {
            id = new CurrencyID(idnum.longValue());
        } catch (NumberFormatException e) {
            id = null;
        }
        if (id != null && id != CurrencyID.BTC) {
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
            a = new Address(null, addressString);
        } catch (AddressFormatException e) {
            a = null;
        }
        return a;
    }
}
