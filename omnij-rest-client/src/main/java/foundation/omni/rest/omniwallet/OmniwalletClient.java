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
import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import retrofit.Call;
import retrofit.Response;
import retrofit.Retrofit;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;
import retrofit.JacksonConverterFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Omniwallet RPC Java client
 */
public class OmniwalletClient implements OmniBalanceService {
    static final String omniwalletBase = "https://www.omniwallet.org";
    static final int CONNECT_TIMEOUT_MILLIS = 15 * 1000; // 15s
    static final int READ_TIMEOUT_MILLIS = 20 * 1000; // 20s
    private OmniwalletService service;


    interface OmniwalletService {
        @FormUrlEncoded
        @POST("/v1/address/addr/")
        Call<Map<String, Object>> balancesForAddress(@Field("addr") String Address);
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
