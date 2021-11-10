package foundation.omni.netapi.omnicore;

import foundation.omni.OmniValue;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import org.consensusj.analytics.service.RichListService;
import org.consensusj.analytics.service.TokenRichList;
import org.consensusj.bitcoin.json.pojo.AddressGroupingItem;
import foundation.omni.json.pojo.OmniPropertyInfo;
import foundation.omni.rpc.ConsensusSnapshot;
import foundation.omni.rpc.OmniClient;
import org.consensusj.bitcoin.json.pojo.ChainTip;
import org.consensusj.jsonrpc.JsonRpcException;
import org.consensusj.jsonrpc.JsonRpcStatusException;
import foundation.omni.CurrencyID;
import foundation.omni.netapi.ConsensusService;
import foundation.omni.netapi.OmniJBalances;
import foundation.omni.netapi.WalletAddressBalance;
import foundation.omni.rpc.BalanceEntry;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.NetworkParameters;
import org.reactivestreams.Publisher;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Omni Core "REST" client that implements same interfaces as Omniwallet REST client
 */
public class OmniCoreClient implements ConsensusService, RichListService<OmniValue, CurrencyID>, AutoCloseable {
    protected final RxOmniClient client;

    /**
     * Constructor that takes an existing RxOmniClient
     *
     * @param client An existing client instance
     */
    public OmniCoreClient(RxOmniClient client)
    {
        this.client = client;
    }

    public OmniCoreClient(SSLSocketFactory sslSocketFactory, NetworkParameters netParams, URI coreURI, String user, String pass, boolean useZmq, boolean isOmniProxy) {
        client = new RxOmniClient(sslSocketFactory, netParams, coreURI, user, pass, useZmq, isOmniProxy);
    }

    public OmniCoreClient(SSLSocketFactory sslSocketFactory, NetworkParameters netParams, URI coreURI, String user, String pass) {
        this(sslSocketFactory, netParams, coreURI, user, pass, false, false);
    }

    public OmniCoreClient(NetworkParameters netParams, URI coreURI, String user, String pass) {
        this((SSLSocketFactory)SSLSocketFactory.getDefault(), netParams, coreURI, user, pass);
    }
    
    @Override
    public CompletableFuture<Integer> currentBlockHeightAsync() {
        return client.supplyAsync(client::getBlockCount);
    }

    /**
     * @return List of OmniPropertyInfo including an entry for Bitcoin
     */
    @Override
    public CompletableFuture<List<OmniPropertyInfo>> listSmartProperties() {
        if (client.isOmniProxyServer()) {
            return client.omniProxyListProperties();
        } else {
            return listSmartPropertiesInternal();
        }
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

    @Override
    public CompletableFuture<ChainTip> getActiveChainTip() {
        return client.supplyAsync(client::getChainTips)
                .thenApply(ChainTip::getActiveChainTip)
                .thenApply(opt -> opt.orElseThrow(() -> new RuntimeException("No active ChainTip")));
    }


    /**
     * @deprecated use getRxOmniClient
     */
    @Deprecated
    public OmniClient getOmniClient() {
        return client;
    }

    public RxOmniClient getRxOmniClient() {
        return client;
    }

    @Override
    public Single<TokenRichList<OmniValue, CurrencyID>> richList(CurrencyID id, int n) {
        return Single.defer(() -> Single.fromCompletionStage(client.omniProxyGetRichList(id, n)));
    }

    @Override
    public Flowable<TokenRichList<OmniValue, CurrencyID>> richListUpdates(CurrencyID id, int n) {
        return client.pollOnNewBlock(() -> client.omniProxyGetRichListSync(id, n));
    }

    @Override
    public Publisher<ChainTip> chainTipPublisher() {
            return client.chainTipPublisher();
    }

    @Override
    public void close() throws Exception {
        this.client.close();
    }

    /**
     * Get all addresses in a wallet
     * Requires wallet support to be active
     * @return A list of addresses
     * @throws JsonRpcStatusException JSON HTTP status was not 200
     * @throws IOException Exception at lower-level
     */
    public List<Address> getWalletAddresses() throws JsonRpcStatusException, IOException {
        // Produce a list of every address in every group
        return client.listAddressGroupings()
                .stream()
                .flatMap( g -> g.stream().map(AddressGroupingItem::getAddress))
                .collect(Collectors.toList());
    }

    /**
     * Fetch Omni Core style list of SmartPropertyListInfo (not including Bitcoin) and
     * convert to list of OmniPropertyInfo.
     * @return Omniwallet/OmniProxy style list of OmniPropertyInfo (including Bitcoin)
     */
    private CompletableFuture<List<OmniPropertyInfo>> listSmartPropertiesInternal() {
        return client.supplyAsync(client::omniListProperties)
                .thenApply(list -> {
                    // Convert SmartPropertyListInfo to OmniPropertyInfo (with "mock" data for some fields)
                    Stream<OmniPropertyInfo> stream = list.stream()
                            .map(OmniPropertyInfo::new);
                    // Prepend a "mock" Bitcoin entry
                    return streamPrepend(OmniPropertyInfo.mockBitcoinPropertyInfo(), stream)
                            .collect(Collectors.toList());
                });
    }

    /* Prepend an element to a steam */
    private <E> Stream<E> streamPrepend(E newFirst, Stream<E> stream) {
        return Stream.concat(Stream.of(newFirst), stream);
    }
}

