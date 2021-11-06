package foundation.omni.netapi.analytics;

import io.reactivex.rxjava3.core.Flowable;
import org.consensusj.bitcoin.json.pojo.ChainTip;
import foundation.omni.CurrencyID;
import foundation.omni.OmniDivisibleValue;
import foundation.omni.OmniIndivisibleValue;
import foundation.omni.OmniValue;
import foundation.omni.netapi.ConsensusService;
import foundation.omni.rpc.BalanceEntry;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Sha256Hash;
import org.consensusj.analytics.service.RichListService;
import org.consensusj.analytics.service.TokenRichList;
import org.consensusj.analytics.util.collector.LargestSliceCollector;
import org.consensusj.analytics.util.collector.LargestSliceList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.stream.Collectors;

/**
 * Omni protocol rich list service that provides Single and Observable interfaces for getting rich list information
 * by property.
 * WARNING: Incubating.
 * WARNING: We're seriously lying about how GENERIC we really are: {@code N} must be OmniValue and {@code ID} must be CurrencyID.
 */
public class OmniLayerRichListService<N extends Number & Comparable<? super N>, ID> implements RichListService<N, ID> {
    private static final Logger log = LoggerFactory.getLogger(OmniLayerRichListService.class);
    private final ConsensusService consensusService;
    private final Flowable<ChainTip> chainTipFlowable;
    private final N smartZero = (N) OmniDivisibleValue.ofWilletts(0);
    private final ChainTip fakeChainTip = new ChainTip(0, Sha256Hash.ZERO_HASH, 0, "mock");

    public OmniLayerRichListService(ConsensusService consensusService) {
        this.consensusService = consensusService;
        this.chainTipFlowable = Flowable.fromPublisher(consensusService.chainTipPublisher());
    }

    /**
     * Asynchronously get a single rich list for a currency
     *
     * TODO: Use ConsensusSnapshot (or equivalent) so we can include blockheight and hash in TokenRichList
     * 
     * @param currencyID The currency ID
     * @param numEntries The requested number of entries in the list
     * @return A {@code Single} that represents an async request for rich list information
     */
    @Override
    public Single<TokenRichList<N, ID>> richList(ID currencyID, int numEntries) {
        return getConsensusForCurrency((CurrencyID) currencyID)
                .map(balances -> this.collectSlices(balances, numEntries))
                .map(slices -> sliceSetToRichList(slices, (ID) currencyID, fakeChainTip));
    }

    /**
     * Subscribe to ongoing rich list updates (update for each new block) for a currency
     *
     * @param currencyID The currency ID
     * @param numEntries The requested number of entries in each list
     * @return An {@code Observable} to subscribe to for a stream of rich list updates
     */
    @Override
    public Observable<TokenRichList<N, ID>> richListUpdates(ID currencyID, int numEntries) {
        boolean usingOmniwalletClient = false;

        if (!usingOmniwalletClient || !currencyID.equals(CurrencyID.USDT)) {
            return chainTipFlowable.flatMapSingle(t -> this.richList(currencyID, numEntries)).toObservable();
        } else {
            // Disable USDT for now, since it times out
            return Observable.error(new RuntimeException("USDT rich list not supported on Omniwallet"));
        }
    }

    /**
     * getConsensusForCurrency as an RxJava Single
     * 
     * @param currencyID The currency to fetch consensus for
     * @return A Single which (upon subscription) will trigger a new async call (and CompletableFuture)
     */
    private Single<SortedMap<Address, BalanceEntry>> getConsensusForCurrency(CurrencyID currencyID) {
        return Single.defer(() -> Single.fromCompletionStage(consensusService.getConsensusForCurrencyAsync(currencyID)));
    }

    private LargestSliceList<Map.Entry<Address, BalanceEntry>, N> collectSlices(SortedMap<Address, BalanceEntry> balances, int numEntries) {
        log.info("Fetching consensus information to build rich list...");
        return balances.entrySet() // TODO: Make this generic
                .parallelStream()
                .collect(new LargestSliceCollector<>(numEntries, this::balanceExtractor, smartZero, this::smartPlus));
    }

    private TokenRichList<N, ID> sliceSetToRichList(LargestSliceList<Map.Entry<Address, BalanceEntry>, N> sliceSet, ID currencyID, ChainTip chainTip) {
        List<TokenRichList.TokenBalancePair<N>> pairList = sliceSet.getSliceList()
                .stream()
                .map(this::mapEntryToBalancePair)
                .collect(Collectors.toList());
        return new TokenRichList<N, ID>(chainTip.getHeight(),
                chainTip.getHash(),
                Instant.now().toEpochMilli(),
                (ID) currencyID,
                pairList,
                (N) sliceSet.getTotalOther());
    }

    private TokenRichList.TokenBalancePair<N> mapEntryToBalancePair(Map.Entry<Address, BalanceEntry> entry) {
        return new TokenRichList.TokenBalancePair<>(entry.getKey(), (N) BalanceEntry.totalBalance(entry.getValue()));
    }
    
    // Extract a total OmniValue from a BalanceEntry in a Map.Entry
    private N balanceExtractor(Map.Entry<Address, BalanceEntry> entry) {
        return (N) BalanceEntry.totalBalance(entry.getValue());
    }

    // Allows values to be the other type (divis/indivis) as long as one is a zero value.
    public N smartPlus(N l, N r) {
        OmniValue left = (OmniValue) l;
        OmniValue right = (OmniValue) r;
        if ((left instanceof OmniDivisibleValue || left.getWilletts() == 0) && (right instanceof OmniDivisibleValue || right.getWilletts() == 0)) {
            return (N) OmniDivisibleValue.ofWilletts(left.getWilletts() + right.getWilletts());
        } else if ((left instanceof OmniIndivisibleValue || left.getWilletts() == 0) && (right instanceof OmniIndivisibleValue || right.getWilletts() == 0)) {
            return (N) OmniIndivisibleValue.ofWilletts(left.getWilletts() + right.getWilletts());
        } else {
            throw new ArithmeticException("Can't use plus with mixed OmniDivisible and OmniIndivisible operands");
        }
    }
}
