package foundation.omni.rest.omniwallet.mjdk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import foundation.omni.CurrencyID;
import foundation.omni.rest.omniwallet.OmniwalletAbstractClient;
import foundation.omni.rest.omniwallet.json.AddressVerifyInfo;
import foundation.omni.rest.omniwallet.json.OmniwalletAddressBalance;
import foundation.omni.rest.omniwallet.json.OmniwalletClientModule;
import foundation.omni.rest.omniwallet.json.OmniwalletPropertiesListResponse;
import foundation.omni.rest.omniwallet.json.RevisionInfo;
import org.bitcoinj.core.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * OmniwalletAbstractClient implementation using JDK 11+ java.net.http HttpClient
 */
public class OmniwalletModernJDKClient extends OmniwalletAbstractClient {
    private static final Logger log = LoggerFactory.getLogger(OmniwalletModernJDKClient.class);
    final HttpClient client;
    private final UncheckedObjectMapper objectMapper;

    public OmniwalletModernJDKClient(URI baseURI) {
        super(baseURI, true, false);
        this.client = HttpClient.newHttpClient();
        objectMapper = new UncheckedObjectMapper();
        objectMapper.registerModule(new OmniwalletClientModule(netParams));
    }
    
    public Integer currentBlockHeight() {
        try {
            return currentBlockHeightAsync().get();
        } catch (InterruptedException | ExecutionException ie) {
            throw new RuntimeException(ie);
        }
    }
    
    @Override
    protected CompletableFuture<OmniwalletPropertiesListResponse> propertiesList() {
        HttpRequest request = HttpRequest
                .newBuilder(baseURI.resolve("/v1/properties/list"))
                .header("Accept", "application/json")
                .build();

        return client.sendAsync(request, BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(s -> objectMapper.readValue(s, OmniwalletPropertiesListResponse.class));

    }

    @Override
    public CompletableFuture<Integer> currentBlockHeightAsync() {
        return revisionInfoAsync().thenApply(RevisionInfo::getLastBlock);
    }

    private CompletableFuture<RevisionInfo> revisionInfoAsync() {
        HttpRequest request = HttpRequest
                .newBuilder(baseURI.resolve("/v1/system/revision.json"))
                .header("Accept", "application/json")
                .build();

        return client.sendAsync(request, BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
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
        HttpRequest request = null;
        request = HttpRequest
                .newBuilder(baseURI.resolve("/v2/address/addr/"))
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(addressesFormEnc))
                .build();

        return client.sendAsync(request, BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .whenComplete((s,e) -> log.info(s))
                .thenApply(s -> objectMapper.readValue(s, typeRef));
    }

    @Override
    protected CompletableFuture<List<AddressVerifyInfo>> verifyAddresses(CurrencyID currencyID) {
        JavaType resultType = objectMapper.getTypeFactory().constructCollectionType(List.class, AddressVerifyInfo.class);

        HttpRequest request = HttpRequest
                .newBuilder(baseURI.resolve("/v1/mastercoin_verify/addresses?currency_id=" + currencyID.toString()))
                .header("Accept", "application/json")
                .build();

        return client.sendAsync(request, BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(s -> objectMapper.readValue(s, resultType));
    }

    /**
     * Convert an address list containing 1 or more entries
     * @param addressList
     * @return
     */
    static String formEncodeAddressList(List<Address> addressList) {
        return addressList.stream()
                .map(Address::toString)     // Convert to string
                .map(a -> URLEncoder.encode(a, StandardCharsets.UTF_8)) // URL Encode as UTF-8
                .map(a -> "addr=" + a)      // Form encode
                .collect(Collectors.joining("&"));
    }

    class UncheckedObjectMapper extends com.fasterxml.jackson.databind.ObjectMapper {
        /**
         * Parses the given JSON string into a Map.
         */
        Map<String, String> readValue(String content) {
            return this.readValue(content, new TypeReference<>() {
            });
        }
        /**
         * Parses the given JSON string into a Class.
         */
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
        public <T> T readValue(String content, TypeReference<T> type) {
            try {
                return super.readValue(content, type);
            } catch (JsonProcessingException e) {
                throw new CompletionException(e);
            }
        }

    }
}
