package foundation.omni.netapi.omnicore;

import com.msgilligan.bitcoinj.json.pojo.AddressGroupingItem;
import foundation.omni.json.pojo.OmniPropertyInfo;
import foundation.omni.rpc.SmartPropertyListInfo;
import org.consensusj.jsonrpc.JsonRpcException;
import org.consensusj.jsonrpc.JsonRpcStatusException;
import foundation.omni.CurrencyID;
import foundation.omni.consensus.OmniCoreConsensusFetcher;
import foundation.omni.netapi.ConsensusService;
import foundation.omni.netapi.OmniJBalances;
import foundation.omni.netapi.WalletAddressBalance;
import foundation.omni.rpc.BalanceEntry;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.NetworkParameters;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Omni Core "REST" client that implements same interfaces as Omniwallet REST client
 */
public class OmniCoreClient extends OmniCoreConsensusFetcher implements ConsensusService {
    public OmniCoreClient(NetworkParameters netParms, URI server, String rpcuser, String rpcpassword) {
        super(netParms, server, rpcuser, rpcpassword);
    }

    @Override
    public CompletableFuture<Integer> currentBlockHeightAsync() {
        final CompletableFuture<Integer> future = new CompletableFuture<>();
        CompletableFuture.runAsync(() -> {
            try {
                future.complete(currentBlockHeight());
            } catch (IOException e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    @Override
    public CompletableFuture<List<OmniPropertyInfo>> listSmartProperties() throws InterruptedException, IOException {
        final CompletableFuture<List<OmniPropertyInfo>> future = new CompletableFuture<>();
        CompletableFuture.runAsync(() -> {
            try {
                List<SmartPropertyListInfo> smartPropertyList = listProperties();
                List<OmniPropertyInfo> smartPropertyInfoList = smartPropertyList.stream()
                        .map(OmniPropertyInfo::new)
                        .collect(Collectors.toList());
                future.complete(smartPropertyInfoList);
            } catch (IOException e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }


    @Override
    public OmniJBalances balancesForAddresses(List<Address> addresses) throws IOException {
        OmniJBalances balances = new OmniJBalances();
        for (Address address : addresses) {
            WalletAddressBalance bal = balancesForAddress(address);
            balances.put(address, bal);
        }
        return balances;
    }

    @Override
    public CompletableFuture<OmniJBalances> balancesForAddressesAsync(List<Address> addresses) {
        // TODO: Implement with parallel requests to balancesForAddressAsync
        final CompletableFuture<OmniJBalances> future = new CompletableFuture<>();
        CompletableFuture.runAsync(() -> {
            try {
                future.complete(balancesForAddresses(addresses));
            } catch (IOException e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }
    

    @Override
    public WalletAddressBalance balancesForAddress(Address address)throws IOException {
        SortedMap<CurrencyID, BalanceEntry> entries = new TreeMap<>();
        try {
            entries = client.omniGetAllBalancesForAddress(address);
        } catch (JsonRpcException e) {
            if (!e.getMessage().equals("Address not found")) {
                throw e;
            }
            // Address not found, so return empty entries map
        }
        
        WalletAddressBalance result = new WalletAddressBalance();
        entries.forEach(result::put);
        return result;
    }

    public CompletableFuture<WalletAddressBalance> balancesForAddressAsync(Address address) {
        final CompletableFuture<WalletAddressBalance> future = new CompletableFuture<>();
        CompletableFuture.runAsync(() -> {
            try {
                future.complete(balancesForAddress(address));
            } catch (IOException e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    /**
     * Get all addresses in a wallet
     * Requires wallet support to be active
     * @return A list of addresses
     * @throws JsonRpcStatusException JSON HTTP status was not 200
     * @throws IOException Exception at lower-level
     */
    public List<Address> getWalletAddresses() throws JsonRpcStatusException, IOException {
        // Get a list of every address
        List<Address> addresses = new ArrayList<>();
        List<List<AddressGroupingItem>> addressItems = client.listAddressGroupings();
        addressItems.forEach(group -> group.forEach( item -> {
            addresses.add(item.getAddress());
        }));
        return addresses;
    }
}

