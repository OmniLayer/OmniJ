package foundation.omni.netapi.omnicore;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import foundation.omni.CurrencyID;
import foundation.omni.OmniDivisibleValue;
import foundation.omni.OmniIndivisibleValue;
import foundation.omni.OmniValue;
import foundation.omni.json.pojo.OmniPropertyInfo;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.LegacyAddress;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.params.MainNetParams;
import org.consensusj.analytics.service.TokenRichList;
import org.consensusj.jsonrpc.JacksonRpcClient;
import org.consensusj.jsonrpc.JsonRpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Interface with default methods implementing omniproxy RPCs
 */
public interface OmniProxyMethods extends JacksonRpcClient {
    Logger log = LoggerFactory.getLogger(OmniProxyMethods.class);

    default CompletableFuture<List<OmniPropertyInfo>> omniProxyListProperties()  {
        return supplyAsync(this::omniProxyListPropertiesSync);
    }

    private List<OmniPropertyInfo> omniProxyListPropertiesSync() throws IOException {
        JavaType javaType = getMapper().getTypeFactory().constructCollectionType(List.class, OmniPropertyInfo.class);
        return send("omniproxy.listproperties", javaType);
    }

    default CompletableFuture<List<OmniPropertyInfo>> omniProxyListPropertiesAddBitcoin()  {
        return omniProxyListProperties().thenApply(result -> {
            List<OmniPropertyInfo> smartPropertyList = new ArrayList<>();
            smartPropertyList.add(bitcoinInfo());   // Add "fake" Bitcoin info
            smartPropertyList.addAll(result);       // Add the list of Omni Properties
            return smartPropertyList;
        });
    }

    private OmniPropertyInfo bitcoinInfo() {
        return new OmniPropertyInfo(CurrencyID.BTC,
                "Bitcoin",
                "n/a",
                "n/a",
                "n/a",
                "n/a",
                true,
                LegacyAddress.fromBase58(MainNetParams.get(), "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa"),
                Sha256Hash.ZERO_HASH,
                false,
                false,
                false,
                "20975046.05472019");  // TODO: Get correct current amount+
    }

    default CompletableFuture<TokenRichList<OmniValue, CurrencyID>> omniProxyGetRichList(CurrencyID id, int size) {
        return supplyAsync(() -> omniProxyGetRichListSync(id, size));
    }

    default TokenRichList<OmniValue, CurrencyID> omniProxyGetRichListSync(CurrencyID id, int size) throws IOException {
        // TODO: Can we replace JsonNode with a JavaType for TokenRichList<OmniValue, CurrencyID> ??
        // JavaType javaType = client.getMapper().getTypeFactory().constructParametricType(TokenRichList.class, OmniValue.class, CurrencyID.class);

        JsonNode node = send("omniproxy.getrichlist", JsonNode.class, id.getValue(), size);
        if (node instanceof NullNode) {
            log.error("Got null node: {}", node);
            throw new JsonRpcException("Got null node");
        }
        List<TokenRichList.TokenBalancePair<OmniValue>> listOnly = new ArrayList<>();
        JsonNode listNode = node.get("richList");
        if (listNode != null) {
            listNode.iterator().forEachRemaining(elementNode-> {
                listOnly.add(nodeToBalancePair(elementNode));
            });
        }
        return new TokenRichList<>(
                0,
                Sha256Hash.ZERO_HASH,
                0,
                CurrencyID.OMNI,
                listOnly,
                parseOmniValue(node.get("otherBalanceTotal").asText())
        );
    }

    private TokenRichList.TokenBalancePair<OmniValue> nodeToBalancePair(JsonNode node) {
        Address address = Address.fromString(null, node.get("address").asText());
        OmniValue value = parseOmniValue(node.get("balance").asText());
        return new TokenRichList.TokenBalancePair<>(address, value);
    }

    private OmniValue parseOmniValue(String numberString) {
        boolean divisible = numberString.contains(".");  // Divisible amounts always contain a decimal point
        return divisible ?  OmniDivisibleValue.of(new BigDecimal(numberString)) :
                OmniIndivisibleValue.of(Long.parseLong(numberString));
    }
}
