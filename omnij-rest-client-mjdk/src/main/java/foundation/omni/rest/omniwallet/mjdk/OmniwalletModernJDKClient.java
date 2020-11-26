package foundation.omni.rest.omniwallet.mjdk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import foundation.omni.CurrencyID;
import foundation.omni.netapi.omniwallet.OmniwalletAbstractClient;
import foundation.omni.netapi.omniwallet.json.AddressVerifyInfo;
import foundation.omni.netapi.omniwallet.json.OmniwalletAddressBalance;
import foundation.omni.netapi.omniwallet.json.OmniwalletClientModule;
import foundation.omni.netapi.omniwallet.json.OmniwalletPropertiesListResponse;
import foundation.omni.netapi.omniwallet.json.RevisionInfo;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.NetworkParameters;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

/**
 * {@link OmniwalletAbstractClient} implementation using JDK 11+ {@link HttpClient}
 */
public class OmniwalletModernJDKClient extends OmniwalletAbstractClient {
    private static final Logger log = LoggerFactory.getLogger(OmniwalletModernJDKClient.class);
    final HttpClient client;
    private final UncheckedObjectMapper objectMapper;

    public OmniwalletModernJDKClient(URI baseURI) {
        this(baseURI, true, false, null);
    }

    /**
     *
     * @param baseURI Base URL of server
     * @param debug Enable debugging, logging, etc.
     * @param strictMode Only accept valid amounts from server
     * @param netParams Specify active Bitcoin network (used for Address validation)
     */
    public OmniwalletModernJDKClient(URI baseURI, boolean debug, boolean strictMode, NetworkParameters netParams) {
        super(baseURI, true, false, netParams);
        log.info("OmniwalletModernJDKClient opened for: {}", baseURI);
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMinutes(2))
                .build();
        objectMapper = new UncheckedObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new OmniwalletClientModule(netParams));
    }

//    private static void log(String s, Throwable t) {
//        if ((s != null)) {
//            log.debug(s.substring(0 ,Math.min(100, s.length())));
//        } else {
//            log.error("exception: ", t);
//        }
//    }


    @Override
    protected CompletableFuture<OmniwalletPropertiesListResponse> propertiesList() {
        HttpRequest request = buildGetRequest("/v1/properties/list");

        //log.debug("Send aysnc: {}", request);
        return client.sendAsync(request, BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                //.whenComplete(OmniwalletModernJDKClient::log)
                .thenApply(s -> objectMapper.readValue(s, OmniwalletPropertiesListResponse.class));

    }

    @Override
    public CompletableFuture<RevisionInfo> revisionInfo() {
        HttpRequest request = buildGetRequest("/v1/system/revision.json");
        
        //log.debug("Send aysnc: {}", request);
        return client.sendAsync(request, BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                //.whenComplete(OmniwalletModernJDKClient::log)
                .thenApply(s -> objectMapper.readValue(s, RevisionInfo.class));

    }


    @Override
    protected CompletableFuture<Map<Address, OmniwalletAddressBalance>> balanceMapForAddress(Address address) {
        return balanceMapForAddresses(Collections.singletonList(address));
    }

    @Override
    protected CompletableFuture<Map<Address, OmniwalletAddressBalance>> balanceMapForAddresses(List<Address> addresses) {
        TypeReference<HashMap<Address, OmniwalletAddressBalance>> typeRef = new TypeReference<>() {};
        String addressesFormEnc = formEncodeAddressList(addresses);
        log.info("Addresses are: {}", addressesFormEnc);
        HttpRequest request = buildPostRequest("/v2/address/addr/", addressesFormEnc);

        //log.debug("Send aysnc: {}", request);
        return client.sendAsync(request, BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                //.whenComplete((s,e) -> log.info(s))
                .thenApply(s -> objectMapper.readValue(s, typeRef));
    }

    @Override
    protected CompletableFuture<List<AddressVerifyInfo>> verifyAddresses(CurrencyID currencyID) {
        JavaType resultType = objectMapper.getTypeFactory().constructCollectionType(List.class, AddressVerifyInfo.class);

        HttpRequest request = buildGetRequest("/v1/mastercoin_verify/addresses?currency_id=" + currencyID.toString());

        //log.debug("Send aysnc: {}", request);
        return client.sendAsync(request, BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                //.whenComplete(OmniwalletModernJDKClient::log)
                .thenApply(s -> objectMapper.readValue(s, resultType));
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

    class UncheckedObjectMapper extends com.fasterxml.jackson.databind.ObjectMapper {
//        /**
//         * Parses the given JSON string into a Map.
//         */
//        Map<String, String> readValue(String content) {
//            return this.readValue(content, new TypeReference<>() {
//            });
//        }
        /**
         * Parses the given JSON string into a Class.
         */
        @Override
        public <T> T readValue(String content, Class<T> clazz) {
            try {
                return super.readValue(content, clazz);
            } catch (JsonProcessingException e) {
                throw new CompletionException(e);
            }
        }

        /**
         * Parses the given JSON string into a JavaType.
         */
        @Override
        public <T> T readValue(String content, JavaType javaType) {
            try {
                return super.readValue(content, javaType);
            } catch (JsonProcessingException e) {
                throw new CompletionException(e);
            }
        }

        /**
         * Parses the given JSON string into a JavaType.
         */
        @Override
        public <T> T readValue(String content, TypeReference<T> type) {
            try {
                return super.readValue(content, type);
            } catch (JsonProcessingException e) {
                throw new CompletionException(e);
            }
        }

    }
}
