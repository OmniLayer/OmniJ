package foundation.omni.rest.omniwallet.mjdk;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import foundation.omni.CurrencyID;
import foundation.omni.netapi.omniwallet.OmniwalletAbstractClient;
import foundation.omni.netapi.omniwallet.json.AddressVerifyInfo;
import foundation.omni.netapi.omniwallet.json.OmniwalletAddressBalance;
import foundation.omni.netapi.omniwallet.json.OmniwalletClientModule;
import foundation.omni.netapi.omniwallet.json.OmniwalletPropertiesListResponse;
import foundation.omni.netapi.omniwallet.json.RevisionInfo;
import org.bitcoinj.base.Address;
import org.bitcoinj.base.Network;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * {@link OmniwalletAbstractClient} implementation using JDK 11+ {@link HttpClient}
 */
public class OmniwalletModernJDKClient extends OmniwalletAbstractClient {
    private static final Logger log = LoggerFactory.getLogger(OmniwalletModernJDKClient.class);
    final HttpClient client;
    private final JsonMapper objectMapper = new JsonMapper();

    public OmniwalletModernJDKClient(URI baseURI) {
        this(baseURI, true, false, (Network) null);
    }

    /**
     *
     * @param baseURI Base URL of server
     * @param debug Enable debugging, logging, etc.
     * @param strictMode Only accept valid amounts from server
     * @param network Specify active Bitcoin network (used for Address validation)
     */
    public OmniwalletModernJDKClient(URI baseURI, boolean debug, boolean strictMode, Network network) {
        super(baseURI, true, false, network);
        log.info("OmniwalletModernJDKClient opened for: {}", baseURI);
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMinutes(2))
                .build();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new OmniwalletClientModule(network));
    }

    @Override
    protected CompletableFuture<OmniwalletPropertiesListResponse> propertiesList() {
        HttpRequest request = buildGetRequest("/v1/properties/list");
        return sendAsync(request, OmniwalletPropertiesListResponse.class);
    }

    @Override
    public CompletableFuture<RevisionInfo> revisionInfo() {
        HttpRequest request = buildGetRequest("/v1/system/revision.json");
        return sendAsync(request, RevisionInfo.class);
    }

    @Override
    protected CompletableFuture<Map<Address, OmniwalletAddressBalance>> balanceMapForAddress(Address address) {
        return balanceMapForAddresses(Collections.singletonList(address));
    }

    @Override
    protected CompletableFuture<Map<Address, OmniwalletAddressBalance>> balanceMapForAddresses(List<Address> addresses) {
        TypeReference<Map<Address, OmniwalletAddressBalance>> typeRef = new TypeReference<>() {};
        String addressesFormEnc = formEncodeAddressList(addresses);
        log.info("Addresses are: {}", addressesFormEnc);
        HttpRequest request = buildPostRequest("/v2/address/addr/", addressesFormEnc);
        return sendAsync(request, typeRef);
    }

    @Override
    protected CompletableFuture<List<AddressVerifyInfo>> verifyAddresses(CurrencyID currencyID) {
        TypeReference<List<AddressVerifyInfo>> typeRef = new TypeReference<>() {};
        HttpRequest request = buildGetRequest("/v1/mastercoin_verify/addresses?currency_id=" + currencyID.toString());
        return sendAsync(request, typeRef);
    }

    private <R> CompletableFuture<R> sendAsync(HttpRequest request, Class<R> clazz) {
        log.debug("Send aysnc: {}", request);
        return sendAsyncCommon(request)
                .thenApply(mappingFunc(clazz));
    }

    private <R> CompletableFuture<R> sendAsync(HttpRequest request, TypeReference<R> typeReference) {
        log.debug("Send aysnc: {}", request);
        return sendAsyncCommon(request)
                .thenApply(mappingFunc(typeReference));
    }

    private CompletableFuture<String> sendAsyncCommon(HttpRequest request) {
        log.debug("Send aysnc: {}", request);
        return client.sendAsync(request, BodyHandlers.ofString())
                .thenComposeAsync(this::handleStatusError)
                .thenApply(HttpResponse::body)
                .whenComplete(OmniwalletModernJDKClient::log);
    }

    private CompletableFuture<HttpResponse<String>> handleStatusError(HttpResponse<String> response) {
        if (response.statusCode() != 200) {
            String errorResponse = response.body();
            log.error("Bad status code: {}: {}", response.statusCode(), errorResponse);
            return CompletableFuture.failedFuture(new RuntimeException(response.statusCode() + " : " + errorResponse));
        } else {
            return CompletableFuture.completedFuture(response);
        }
    }

    private <R> MappingFunction<R> mappingFunc(Class<R> clazz) {
        return s -> objectMapper.readValue(s, clazz);
    }

    private <R> MappingFunction<R> mappingFunc(TypeReference<R> typeReference) {
        return s -> objectMapper.readValue(s, typeReference);
    }

    private HttpRequest buildGetRequest(String uriPath) {
        return HttpRequest
                .newBuilder(baseURI.resolve(uriPath))
                .header("Accept", "application/json")
                .build();
    }

    private HttpRequest buildPostRequest(String uriPath, String postData) {
        return HttpRequest
                .newBuilder(baseURI.resolve(uriPath))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(postData))
                .build();
    }

    /**
     * Convert an address list containing 1 or more entries
     * @param addressList A list of addresses
     * @return a form-encoded string containing the list of addresses
     */
    static String formEncodeAddressList(List<Address> addressList) {
        return addressList.stream()
                .map(Address::toString)     // Convert to string
                .map(a -> URLEncoder.encode(a, StandardCharsets.UTF_8)) // URL Encode as UTF-8
                .map(a -> "addr=" + a)      // Form encode
                .collect(Collectors.joining("&"));
    }

    private static void log(String s, Throwable t) {
        if ((s != null)) {
            log.debug(s.substring(0 ,Math.min(100, s.length())));
        } else {
            log.error("exception: ", t);
        }
    }

    @FunctionalInterface
    interface MappingFunction<R> extends ThrowingFunction<String, R> {}

    @FunctionalInterface
    interface ThrowingFunction<T,R> extends Function<T, R> {

        /**
         * Gets a result wrapping checked Exceptions with {@link RuntimeException}
         * @return a result
         */
        @Override
        default R apply(T t) {
            try {
                return applyThrows(t);
            } catch (final Exception e) {
                throw new CompletionException(e);
            }
        }

        /**
         * Gets a result.
         *
         * @return a result
         * @throws Exception Any checked Exception
         */
        R applyThrows(T t) throws Exception;
    }
}
