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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Omniwallet REST Java client
 */
public class OmniwalletClient implements ConsensusService {
    public static final HttpUrl omniwalletBase = HttpUrl.parse("https://www.omniwallet.org");
    public static final HttpUrl stagingBase = HttpUrl.parse("https://staging.omniwallet.org");
    static final int CONNECT_TIMEOUT_MILLIS = 15 * 1000; // 15s
    static final int READ_TIMEOUT_MILLIS = 120 * 1000; // 120s (long enough to load USDT rich list)
    static public final int BALANCES_FOR_ADDRESSES_MAX_ADDR = 20;
    private boolean strictMode;
    private Retrofit restAdapter;
    private OmniwalletService service;
    private Map<CurrencyID, PropertyType> cachedPropertyTypes = new HashMap<>();
    private NetworkParameters netParams;
    
    interface OmniwalletService {
        @FormUrlEncoded
        @POST("/v1/address/addr/")
        CompletableFuture<Response<OmniwalletAddressBalance>> balancesForAddress_v1(@Field("addr") Address address);

        @FormUrlEncoded
        @POST("/v2/address/addr/")
        CompletableFuture<Response<Map<Address, OmniwalletAddressBalance>>> balancesForAddress(@Field("addr") Address address);

        @FormUrlEncoded
        @POST("/v2/address/addr/")
        CompletableFuture<Response<Map<Address, OmniwalletAddressBalance>>> balancesForAddresses(@Field("addr") List<Address> addresses);

        @GET("/v1/system/revision.json")
        CompletableFuture<Response<RevisionInfo>> getRevisionInfo();

        @GET("/v1/mastercoin_verify/properties")
        CompletableFuture<Response<List<PropertyVerifyInfo>>> verifyProperties();

        @GET("/v1/mastercoin_verify/addresses")
        CompletableFuture<Response<List<AddressVerifyInfo>>> verifyAddresses(@Query("currency_id") String currencyId);

    }

    /**
     * Default constructor
     */
    public OmniwalletClient() {
        this(omniwalletBase, false);
    }

    public OmniwalletClient(HttpUrl baseURL, boolean debug) {
        this(baseURL, debug, false);
    }

    /**
     * Constructor with debug and strict-mode options
     * 
     * @param baseURL Base URL of server
     * @param debug Enable debugging, logging, etc.
     * @param strictMode Only accept valid amounts from server
     */
    public OmniwalletClient(HttpUrl baseURL, boolean debug, boolean strictMode) {
        this.strictMode = strictMode;
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
    public WalletAddressBalance balancesForAddress(Address address) throws InterruptedException, IOException {
        Response<Map<Address, OmniwalletAddressBalance>> response;
        try {
            response = service.balancesForAddress(address).get();
        } catch (ExecutionException e) {
            throw new IOException(e);
        }
        if (!response.isSuccessful()) {
            throw new IOException("Unsuccessful response in balanceInfosForAddress");
        }
        Map<Address, OmniwalletAddressBalance> resultMap = response.body();
        OmniwalletAddressBalance result = resultMap.get(address);
        return balanceEntryMapper(result);
    }

    @Override
    public OmniJBalances balancesForAddresses(List<Address> addresses) throws InterruptedException, IOException {
        try {
            return balancesForAddressesAsync(addresses).get();
        } catch (ExecutionException e) {
            throw new IOException(e);
        }
    }


    @Override
    public CompletableFuture<OmniJBalances> balancesForAddressesAsync(List<Address> addresses) {
        if (addresses.size() > BALANCES_FOR_ADDRESSES_MAX_ADDR) {
            throw new IllegalArgumentException("Exceeded number of allowable addresses");
        }
        return service.balancesForAddresses(addresses).thenApply(response -> {
            OmniJBalances balances = new OmniJBalances();
            response.body().forEach((address, owb) ->
                    balances.put(address, balanceEntryMapper(owb)));
            return balances;
        });
    }


    private  WalletAddressBalance balanceEntryMapper(OmniwalletAddressBalance owb) {
        WalletAddressBalance wab = new WalletAddressBalance();
        
        for (OmniwalletAddressPropertyBalance pb : owb.getBalance()) {
            CurrencyID id = pb.getId();
            PropertyType type =  pb.isDivisible() ? PropertyType.DIVISIBLE : PropertyType.INDIVISIBLE;
            OmniValue value = pb.getValue();
            OmniValue zero = OmniValue.ofWilletts(0, type);
            if (!pb.isError()) {
                wab.put(id, new BalanceEntry(value, zero, zero));
            }
        }
        return wab;
    }
    

    @Override
    public ConsensusSnapshot getConsensusSnapshot(CurrencyID currencyID) throws IOException, InterruptedException {
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

    public SortedMap<Address, BalanceEntry> getConsensusForCurrency(CurrencyID currencyID) throws InterruptedException, IOException {
        List<AddressVerifyInfo> balances;
        try {
            //balances = service.verifyAddresses(Long.toString(currencyID.getValue())).get().body();
            CompletableFuture<Response<List<AddressVerifyInfo>>> future = service.verifyAddresses(Long.toString(currencyID.getValue()));
            Response<List<AddressVerifyInfo>> response = future.get();
            if (response.isSuccessful()) {
                balances = response.body();
            } else {
                String message = response.message();
                if (message == null) {
                    message = "Retrofit returned null response message";
                }
                throw new IOException(message);
            }
            balances = response.body();
        } catch (ExecutionException e) {
            throw new IOException(e);
        }
        // TODO: We need an accurate, efficient way of determining divisible vs indivisible
        final PropertyType propertyType = lookupPropertyType(currencyID);
        Map<Address, BalanceEntry> unsorted = balances.stream()
                .map(bal -> balanceMapper(bal, propertyType))
                .collect(Collectors.toMap(
                        AddressBalanceEntry::getAddress,
                        address -> new BalanceEntry(address.getBalance(),
                                                    address.getReserved(),
                                                    address.getFrozen())
                ));
        return new TreeMap<>(unsorted);
    }

    public CompletableFuture<RevisionInfo> revisionInfoAsync() {
        return service.getRevisionInfo().thenApply(Response::body);
    }

    @Override
    public Integer currentBlockHeight() throws InterruptedException, IOException  {
        Integer height;
        try {
            height = currentBlockHeightAsync().get();
        } catch (ExecutionException ee) {
            throw new IOException(ee);
        }
        return height;
    }

    @Override
    public CompletableFuture<Integer> currentBlockHeightAsync() {
        return service.getRevisionInfo()
                .thenApply(response -> response.body().getLastBlock());
    }

    @Override
    public List<SmartPropertyListInfo> listProperties() throws InterruptedException, IOException {
        List<PropertyVerifyInfo> properties;
        try {
            properties = service.verifyProperties().get().body();
        } catch (ExecutionException e) {
            throw new IOException(e);
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

        OmniValue balance = toOmniValue(balanceStr, propertyType);
        OmniValue reserved = toOmniValue(reservedStr, propertyType);
        OmniValue frozen = OmniValue.of(0, propertyType);   // Placeholder
        return new AddressBalanceEntry(address, balance, reserved, frozen);
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

    private PropertyType lookupPropertyType(CurrencyID propertyID) throws IOException, InterruptedException {
        if (!cachedPropertyTypes.containsKey(propertyID)) {
            // If we don't have the info, fetch info for all properties from server and update
            List<SmartPropertyListInfo> infos = listProperties();
            infos.forEach(info -> {
                cachedPropertyTypes.put(info.getPropertyid(), divisibleToPropertyType(info.getDivisible()));
            });
        }
        return cachedPropertyTypes.get(propertyID);
    }
    
    private URI consensusURI(CurrencyID currencyID) {
        HttpUrl okUrl = restAdapter
                .baseUrl()
                .newBuilder("/v1/mastercoin_verify/addresses?currency_id=" + currencyID.getValue())
                .build();
        return okUrl.uri();
    }

    private OmniValue toOmniValue(String input, PropertyType type) {
        if (strictMode) {
            /* Validate string */
        }
        return type.isDivisible() ?
                toOmniDivisibleValue(input) : toOmniIndivisibleValue(input);
    }

    private OmniDivisibleValue toOmniDivisibleValue(String inputStr) {
        return toOmniDivisibleValue(new BigDecimal(inputStr));
    }

    private OmniDivisibleValue toOmniDivisibleValue(BigDecimal input) {
        if (input.compareTo(OmniDivisibleValue.MAX_VALUE) > 0) {
            if (strictMode) {
                throw new ArithmeticException("too big");
            }
            return OmniDivisibleValue.MAX;
        } else if (input.compareTo(OmniDivisibleValue.MIN_VALUE) < 0) {
            if (strictMode) {
                throw new ArithmeticException("too small");
            }
            return OmniDivisibleValue.MIN;
        } else {
            return OmniDivisibleValue.of(input);
        }
    }

    private OmniIndivisibleValue toOmniIndivisibleValue(String input) {
        return toOmniIndivisibleValue(new BigInteger(input));
    }

    private OmniIndivisibleValue toOmniIndivisibleValue(BigInteger input) {
        if (input.compareTo(OmniIndivisibleValue.MAX_BIGINT) > 0) {
            if (strictMode) {
                throw new ArithmeticException("too big");
            }
            return OmniIndivisibleValue.MAX;
        } else if (input.compareTo(OmniIndivisibleValue.MIN_BIGINT) < 0) {
            if (strictMode) {
                throw new ArithmeticException("too small");
            }
            return OmniIndivisibleValue.MIN;
        } else {
            return OmniIndivisibleValue.of(input);
        }
    }

    private PropertyType divisibleToPropertyType(boolean divisible) {
        PropertyType type =  divisible ? PropertyType.DIVISIBLE : PropertyType.INDIVISIBLE;
        return type;
    }
}
