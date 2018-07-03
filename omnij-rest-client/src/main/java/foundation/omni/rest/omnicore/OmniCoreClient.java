package foundation.omni.rest.omnicore;

import com.msgilligan.bitcoinj.json.pojo.AddressGroupingItem;
import org.consensusj.jsonrpc.JsonRPCException;
import org.consensusj.jsonrpc.JsonRPCStatusException;
import foundation.omni.CurrencyID;
import foundation.omni.consensus.OmniCoreConsensusFetcher;
import foundation.omni.rest.ConsensusService;
import foundation.omni.rest.OmniJBalances;
import foundation.omni.rest.WalletAddressBalance;
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
        } catch (JsonRPCException e) {
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
     * @throws JsonRPCStatusException JSON HTTP status was not 200
     * @throws IOException Exception at lower-level
     */
    public List<Address> getWalletAddresses() throws JsonRPCStatusException, IOException {
        // Get a list of every address
        List<Address> addresses = new ArrayList<>();
        List<List<AddressGroupingItem>> addressItems = client.listAddressGroupings();
        addressItems.forEach(group -> group.forEach( item -> {
            addresses.add(item.getAddress());
        }));
        return addresses;
    }
}

