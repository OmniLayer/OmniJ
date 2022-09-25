package foundation.omni.consensus;

import foundation.omni.CurrencyID;
import foundation.omni.BalanceEntry;
import foundation.omni.rpc.ConsensusFetcher;
import foundation.omni.json.pojo.SmartPropertyListInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.StreamSupport;

/**
 * Consensus comparison across all properties
 */
public class MultiPropertyComparison {
    private static final Logger log = LoggerFactory.getLogger(MultiPropertyComparison.class);
    private final ConsensusFetcher f1;
    private final ConsensusFetcher f2;

    public MultiPropertyComparison(ConsensusFetcher left, ConsensusFetcher right) {
        this.f1 = left;
        this.f2 = right;
    }

    public long compareAllProperties() throws InterruptedException, ExecutionException {
        CompletableFuture<Set<CurrencyID>> props1f = f1.listSmartProperties()
                .thenApply(l -> l.stream()
                    .map(SmartPropertyListInfo::getPropertyid)
                    .collect(TreeSet::new, SortedSet::add, SortedSet::addAll)
                );
        CompletableFuture<Set<CurrencyID>> props2f = f2.listSmartProperties()
                .thenApply(l -> l.stream()
                    .map(SmartPropertyListInfo::getPropertyid)
                    .collect(TreeSet::new, SortedSet::add, SortedSet::addAll)
                );
        Set<CurrencyID> props = props1f.thenCombine(props2f, (props1, props2) -> {
            props1.addAll(props2);
            return props1;
        }).get();
        props.remove(CurrencyID.BTC);  // We can't do a consensus comparison for BTC
        return compareProperties(props);
    }

    public long compareProperty(CurrencyID id) throws InterruptedException, ExecutionException {
        log.info("fetching ConsensusComparison for ID:{} ", id);
        ConsensusComparison comparison = getConsensusComparison(id).get();
        log.info("comparing {} h1:{} h2:{}", id, comparison.getC1().getBlockHeight(), comparison.getC2().getBlockHeight());

        long mismatches = StreamSupport.stream(comparison.spliterator(), false)
                .filter(this::pairNotEqual)
                .peek(pair -> printMismatch(id, pair))
                .count();

        return mismatches;
    }
    
    private CompletableFuture<ConsensusComparison> getConsensusComparison(CurrencyID currencyID) {
        return f1.getConsensusSnapshotAsync(currencyID)
                .thenCombine(f2.getConsensusSnapshotAsync(currencyID), ConsensusComparison::new);
    }
    
    public long compareProperties(Set<CurrencyID> propertiesToCompare) throws InterruptedException, ExecutionException {
        long mismatches = 0;  // Assume true, until we find something that doesn't match

        for (CurrencyID id : propertiesToCompare) {
            boolean propertyEqual = compareProperty(id) == 0;
            if (!propertyEqual) {
                mismatches++;
            }
        }
        return mismatches;
    }

    boolean pairNotEqual(ConsensusEntryPair pair) {
        return !pairEqual(pair);
    }
    boolean pairEqual(ConsensusEntryPair pair) {
        BalanceEntry entry1 = pair.getEntry1();
        BalanceEntry entry2 = pair.getEntry2();
        log.debug("about to compare pair {}: {} {}", pair.getAddress(), entry1, entry2);
        return ((entry1 == null && entry2 == null) ||
                (entry1 == null && BalanceEntry.totalBalance(entry2).getWilletts() == 0) ||
                (entry2 == null && BalanceEntry.totalBalance(entry1).getWilletts() == 0) ||
                ((entry1 != null) && (entry2 != null)  && entry1.equals(entry2)));
    }

    void printMismatch(CurrencyID id, ConsensusEntryPair pair) {
        System.out.println(pair.getAddress() + " " + id + ": " + pair.getEntry1() + "!=" + pair.getEntry2());
    }
}
