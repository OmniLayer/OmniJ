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

import javax.net.ssl.SSLSocketFactory;
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

    public OmniCoreClient(SSLSocketFactory sslSocketFactory, NetworkParameters netParams, URI coreURI, String user, String pass) {
        client = new OmniClient(sslSocketFactory, netParams, coreURI, user, pass);
    }

    public OmniCoreClient(NetworkParameters netParams, URI coreURI, String user, String pass) {
        client = new OmniClient(netParams, coreURI, user, pass);
    }
    
    @Override
    public CompletableFuture<Integer> currentBlockHeightAsync() {
        return client.supplyAsync(client::getBlockCount);
    }

    @Override
    public CompletableFuture<List<OmniPropertyInfo>> listSmartProperties() {
        return client.supplyAsync(() -> client.omniListProperties().stream()
                .map(OmniPropertyInfo::new)
                .collect(Collectors.toList()));
    }

    @Override
    public CompletableFuture<SortedMap<Address, BalanceEntry>> getConsensusForCurrencyAsync(CurrencyID currencyID) {
        return client.supplyAsync(() -> client.omniGetAllBalancesForId(currencyID));
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
        return client.supplyAsync(() -> balancesForAddresses(addresses));
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
        result.putAll(entries);
        return result;
    }

    @Override
    public ConsensusSnapshot createSnapshot(CurrencyID id, int blockHeight, SortedMap<Address, BalanceEntry> entries) {
        return new ConsensusSnapshot(id,blockHeight, "Omni Core", client.getServerURI(), entries);
    }

    @Override
    public CompletableFuture<WalletAddressBalance> balancesForAddressAsync(Address address) {
        return client.supplyAsync(() -> balancesForAddress(address));
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

