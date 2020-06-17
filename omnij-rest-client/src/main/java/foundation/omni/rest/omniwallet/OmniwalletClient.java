package foundation.omni.rest.omniwallet;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import foundation.omni.netapi.omniwallet.OmniwalletAbstractClient;
import foundation.omni.netapi.omniwallet.json.OmniwalletPropertiesListResponse;
import org.bitcoinj.core.Address;
import foundation.omni.netapi.omniwallet.json.AddressVerifyInfo;
import foundation.omni.netapi.omniwallet.json.OmniwalletAddressBalance;
import foundation.omni.netapi.omniwallet.json.OmniwalletClientModule;
import foundation.omni.netapi.omniwallet.json.RevisionInfo;
import foundation.omni.CurrencyID;
import okhttp3.ConnectionSpec;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.http.Query;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Omniwallet REST Java client
 */
public class OmniwalletClient extends OmniwalletAbstractClient {
    private final OmniwalletService service;

    interface OmniwalletService {
        @FormUrlEncoded
        @POST("/v2/address/addr/")
        CompletableFuture<Response<Map<Address, OmniwalletAddressBalance>>> balancesForAddress(@Field("addr") Address address);

        @FormUrlEncoded
        @POST("/v2/address/addr/")
        CompletableFuture<Response<Map<Address, OmniwalletAddressBalance>>> balancesForAddresses(@Field("addr") List<Address> addresses);

        @GET("/v1/system/revision.json")
        CompletableFuture<Response<RevisionInfo>> getRevisionInfo();
        
        @GET("/v1/properties/list")
        CompletableFuture<Response<OmniwalletPropertiesListResponse>> propertiesList();

        @GET("/v1/mastercoin_verify/addresses")
        CompletableFuture<Response<List<AddressVerifyInfo>>> verifyAddresses(@Query("currency_id") String currencyId);
    }

    /**
     * Default constructor
     */
    public OmniwalletClient() {
        this(omniwalletBase, false);
    }

    public OmniwalletClient(URI baseURI, boolean debug) {
        this(baseURI, debug, false);
    }

    /**
     * Constructor with debug and strict-mode options
     * 
     * @param baseURI Base URL of server
     * @param debug Enable debugging, logging, etc.
     * @param strictMode Only accept valid amounts from server
     */
    public OmniwalletClient(URI baseURI, boolean debug, boolean strictMode) {
        this(baseURI, debug, strictMode, defaultHttpClient(debug));
    }

    public OmniwalletClient(URI baseURI, boolean debug, boolean strictMode, OkHttpClient client) {
        this(baseURI, debug, strictMode, client, Executors.newFixedThreadPool(2));
    }

    public OmniwalletClient(URI baseURI, boolean debug, boolean strictMode,  OkHttpClient client, Executor executor) {
        super(baseURI, debug, strictMode);
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new OmniwalletClientModule(netParams));

        Retrofit restAdapter = new Retrofit.Builder()
                .client(client)
                .baseUrl(HttpUrl.get(baseURI))
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .callbackExecutor(executor)
                .build();

        service = restAdapter.create(OmniwalletService.class);
    }

    public static OkHttpClient defaultHttpClient(boolean debug) {

        OkHttpClient.Builder builder = defaultHttpClientBuilder();

        if (debug) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(interceptor);
        }

        return builder.build();
    }

    public static OkHttpClient.Builder  defaultHttpClientBuilder() {

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS))
                .connectTimeout(CONNECT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                .readTimeout(READ_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);

        return builder;
    }
    
    @Override
    protected CompletableFuture<Map<Address, OmniwalletAddressBalance>> balanceMapForAddress(Address address) {
        return service.balancesForAddress(address).thenApply(Response::body);
    }

    @Override
    protected CompletableFuture<Map<Address, OmniwalletAddressBalance>> balanceMapForAddresses(List<Address> addresses) {
        return service.balancesForAddresses(addresses).thenApply(Response::body);
    }


    @Override
    protected CompletableFuture<List<AddressVerifyInfo>> verifyAddresses(CurrencyID currencyID) {
        return service.verifyAddresses(Long.toString(currencyID.getValue()))
                .thenApply(Response::body);
    }

    public CompletableFuture<RevisionInfo> revisionInfoAsync() {
        return service.getRevisionInfo().thenApply(Response::body);
    }


    @Override
    public CompletableFuture<Integer> currentBlockHeightAsync() {
        return service.getRevisionInfo()
                .thenApply(response -> response.body().getLastBlock());
    }
    
    @Override
    public CompletableFuture<OmniwalletPropertiesListResponse> propertiesList() {
        return service.propertiesList().thenApply(Response::body);
    }
}
