package foundation.omni.rpc;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import foundation.omni.CurrencyID;
import foundation.omni.OmniValue;
import foundation.omni.json.pojo.OmniPropertyInfo;
import foundation.omni.json.pojo.OmniJBalances;
import foundation.omni.json.pojo.WalletAddressBalance;
import org.bitcoinj.base.Address;
import org.bitcoinj.base.AddressParser;
import org.bitcoinj.base.DefaultAddressParser;
import org.bitcoinj.base.Sha256Hash;
import org.consensusj.analytics.service.TokenRichList;
import org.consensusj.jsonrpc.JacksonRpcClient;
import org.consensusj.jsonrpc.JsonRpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Interface with default methods implementing omniproxy RPCs.
 */
public interface OmniProxyMethods extends JacksonRpcClient {
    Logger log = LoggerFactory.getLogger(OmniProxyMethods.class);
    AddressParser addressParser = new DefaultAddressParser();

    /**
     * Determine if remote server is an OmniProxy server.
     * @return true if server is OmniProxy
     */
    boolean isOmniProxyServer();

    private List<OmniPropertyInfo> omniProxyListPropertiesSync() throws IOException {
        JavaType javaType = getMapper().getTypeFactory().constructCollectionType(List.class, OmniPropertyInfo.class);
        return send("omniproxy.listproperties", javaType);
    }

    default CompletableFuture<List<OmniPropertyInfo>> omniProxyListProperties()  {
        return supplyAsync(this::omniProxyListPropertiesSync);
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
        JsonNode listNode = node.get("richList");
        List<TokenRichList.TokenBalancePair<OmniValue>> listOnly =
                (listNode != null)
                ? StreamSupport.stream(listNode.spliterator(), false)
                            .map(this::nodeToBalancePair)
                            .collect(Collectors.toList())
                : List.of();
        return new TokenRichList<>(
                0,
                Sha256Hash.ZERO_HASH,
                0,
                CurrencyID.OMNI,
                listOnly,
                OmniValue.of(node.get("otherBalanceTotal").asText())
        );
    }

    default WalletAddressBalance omniProxyGetBalance(Address address) throws IOException {
        return send("omniproxy.getbalance", WalletAddressBalance.class, address);
    }

    default OmniJBalances omniProxyGetBalances(List<Address> addresses) throws IOException {
        return send("omniproxy.getbalances", OmniJBalances.class, addresses.toArray());
    }

    private TokenRichList.TokenBalancePair<OmniValue> nodeToBalancePair(JsonNode node) {
        Address address = addressParser.parseAddressAnyNetwork(node.get("address").asText());
        OmniValue value = OmniValue.of(node.get("balance").asText());
        return new TokenRichList.TokenBalancePair<>(address, value);
    }
}
