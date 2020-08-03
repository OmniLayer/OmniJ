package foundation.omni.netapi.omnicore;

import com.msgilligan.bitcoinj.json.pojo.AddressGroupingItem;
import foundation.omni.json.pojo.OmniPropertyInfo;
import foundation.omni.rpc.ConsensusSnapshot;
import foundation.omni.rpc.OmniClient;
import org.consensusj.jsonrpc.JsonRpcException;
import org.consensusj.jsonrpc.JsonRpcStatusException;
import foundation.omni.CurrencyID;
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
public class OmniCoreClient implements ConsensusService {
    protected final OmniClient client;
    
    /**
     * Constructor that takes an existing OmniClient
     *
     * @param client An existing client instance
     */
    public OmniCoreClient(OmniClient client)
    {
        this.client = client;
    }

    public OmniCoreClient(NetworkParameters netParams, URI coreURI, String user, String pass) {
        client = new OmniClient(netParams, coreURI, user, pass);
    }
    
    @Override
    public CompletableFuture<Integer> currentBlockHeightAsync() {
        final CompletableFuture<Integer> future = new CompletableFuture<>();
        CompletableFuture.runAsync(() -> {
            try {
                future.complete(client.getBlockCount());
            } catch (IOException e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    @Override
    public CompletableFuture<List<OmniPropertyInfo>> listSmartProperties() {
        final CompletableFuture<List<OmniPropertyInfo>> future = new CompletableFuture<>();
        CompletableFuture.runAsync(() -> {
            try {
                List<OmniPropertyInfo> smartPropertyInfoList = client.omniListProperties().stream()
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
    public CompletableFuture<SortedMap<Address, BalanceEntry>> getConsensusForCurrencyAsync(CurrencyID currencyID) {
        final CompletableFuture<SortedMap<Address, BalanceEntry>> future = new CompletableFuture<>();
        CompletableFuture.runAsync(() -> {
            try {
                // TODO: Filter out empty address strings or 0 balances?
                SortedMap<Address, BalanceEntry> consensus = client.omniGetAllBalancesForId(currencyID);
                future.complete(consensus);
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

    @Override
    public ConsensusSnapshot createSnapshot(CurrencyID id, int blockHeight, SortedMap<Address, BalanceEntry> entries) {
        return new ConsensusSnapshot(id,blockHeight, "Omni Core", client.getServerURI(), entries);
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

