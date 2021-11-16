package foundation.omni.netapi.omnicore;

import foundation.omni.OmniDivisibleValue;
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
import org.consensusj.bitcoin.json.pojo.bitcore.AddressBalanceInfo;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Omni Core "REST" client that implements same interfaces as Omniwallet REST client
 */
public class OmniCoreClient implements ConsensusService, RichListService<OmniValue, CurrencyID>, AutoCloseable {
    Logger log = LoggerFactory.getLogger(OmniCoreClient.class);
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
        try {
            return balancesForAddressesAsync(addresses).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException(e);
        }
    }

    @Override
    public CompletableFuture<OmniJBalances> balancesForAddressesAsync(List<Address> addresses) {
        OmniJBalances balances = new OmniJBalances();
        // TODO: Limit total number of parallel requests?
        CompletableFuture[] futures = addresses.parallelStream()
                .map(address -> this.balancesForAddressAsync(address)
                        .thenAccept(wab -> balances.put(address, wab)))
                .toArray(CompletableFuture[]::new);

        return CompletableFuture
                .allOf(futures)
                .thenApply(v -> balances);
    }

    @Override
    public WalletAddressBalance balancesForAddress(Address address) throws IOException {
        try {
            return balancesForAddressAsync(address).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException(e);
        }
    }

    @Override
    public ConsensusSnapshot createSnapshot(CurrencyID id, int blockHeight, SortedMap<Address, BalanceEntry> entries) {
        return new ConsensusSnapshot(id,blockHeight, "Omni Core", client.getServerURI(), entries);
    }

    @Override
    public CompletableFuture<WalletAddressBalance> balancesForAddressAsync(Address address) {
        boolean serverHasAddressIndex = checkAddressIndex();
        if (hasAddressIndex) {
            // Server has address index (for Bitcoin addresses), combine Omni & Bitcoin balances
            WalletAddressBalance wab = new WalletAddressBalance();
            return omniGetAllBalancesForAddressAsync(address)
                    .handle(this::ignoreAddressNotFound)
                    .thenCompose(Function.identity())
                    .thenAccept(wab::putAll)
                    // combine separate query of Bitcoin balance
                    .thenCombine(getAddressBalanceAsync(address).thenAccept(balanceInfo -> wab.put(CurrencyID.BTC, btcBalanceInfoToOmniBalanceEntry(balanceInfo))),
                                    (Void v1, Void v2) -> wab);
        } else {
            // No address index (for Bitcoin addresses), just return Omni property balances
            return omniGetAllBalancesForAddressAsync(address)
                    .handle(this::ignoreAddressNotFound)
                    .thenCompose(Function.identity())
                    .thenApply(WalletAddressBalance::new);
        }
    }

    private Boolean hasAddressIndex;

    // This is needed because isAddressIndexEnabled() can throw checked exceptions
    private synchronized boolean checkAddressIndex() {
        if (hasAddressIndex == null) {
            try {
                hasAddressIndex = getRxOmniClient().isAddressIndexEnabled();
            } catch (IOException e) {
                // ignore checked exception and just mark hasAddressIndex as `false`
                // maybe we should throw a runtime exception?
                log.error("error checking for getaddressbalance", e);
                hasAddressIndex = false;
            }
        }
        return hasAddressIndex;
    }

    private CompletableFuture<SortedMap<CurrencyID, BalanceEntry>> omniGetAllBalancesForAddressAsync(Address address) {
        return client.supplyAsync(() -> client.omniGetAllBalancesForAddress(address));
    }

    /**
     * Convert a Bitcoin {@link AddressBalanceInfo} to an Omni {@link BalanceEntry}
     * <ul>
     *     <li>For {@link CurrencyID#BTC} 1 willett equals 1 satoshi</li>
     *     <li>We will treat "immature" (newly mined) coins as "reserved"</li>
     * </ul>
     * </p>
     * @param info Bitcoin balance in Bitcoin/BitCore format
     * @return Bitcoin balance in Omni format
     */
    private BalanceEntry btcBalanceInfoToOmniBalanceEntry(AddressBalanceInfo info) {
        return new BalanceEntry(OmniDivisibleValue.ofWilletts(info.getBalance().value - info.getImmature().value),  // balance
                OmniDivisibleValue.ofWilletts(info.getImmature().value),  // reserved
                OmniDivisibleValue.ZERO);  // Bitcoins can't be frozen!
    }

    // Async wrapper call to getAddressBalance()
    private CompletableFuture<AddressBalanceInfo> getAddressBalanceAsync(Address address) {
        return client.supplyAsync(() -> client.getAddressBalance(address));
    }

    /**
     * Map a (result, throwable) to a new CompletionStage such that:
     * <ul>
     *     <li>Successful results are passed through unchanged</li>
     *     <li>"Address not found" JsonRpcExceptions are replaced with successful result with an empty map</li>
     *     <li>Other Throwables are passed through unchanged</li>
     * </ul>
     * <p>
     * In JDK 11 and earlier, this will typically be called as
     * {@code
     *                 .handle(this::ignoreAddressNotFound)
     *                 .thenCompose(Function.identity())
     * }
     * In JDK 12 and later this method could be simplified and called with {@code CompletionStage.exceptionallyCompose()}
     * @param result result
     * @param t throwable
     * @return new completion stage with "Address not found" exception replaced with empty map
     */
    private CompletionStage<SortedMap<CurrencyID, BalanceEntry>> ignoreAddressNotFound(SortedMap<CurrencyID, BalanceEntry> result, Throwable t) {
        if (result != null) {
            return CompletableFuture.completedFuture(result);
        } else if (t instanceof JsonRpcException && t.getMessage().equals("Address not found")) {
            /* Address not found: return empty map */
            return CompletableFuture.completedFuture(new TreeMap<>());
        } else {
            /* Other failure, pass it through */
            return CompletableFuture.failedFuture(t);
        }
    }

    @Override
    public CompletableFuture<ChainTip> getActiveChainTip() {
        return client.supplyAsync(client::getChainTips)
                .thenApply(ChainTip::findActiveChainTipOrElseThrow);
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

