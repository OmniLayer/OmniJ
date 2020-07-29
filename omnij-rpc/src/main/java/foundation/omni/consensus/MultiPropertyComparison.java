package foundation.omni.consensus;

import foundation.omni.CurrencyID;
import foundation.omni.rpc.ConsensusFetcher;
import foundation.omni.rpc.ConsensusSnapshot;
import foundation.omni.rpc.SmartPropertyListInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

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

    public void compareAllProperties() throws IOException, InterruptedException {
        Set<CurrencyID> props1 = f1.listProperties().stream()
                .map(SmartPropertyListInfo::getPropertyid)
                .collect(Collectors.toSet());
        Set<CurrencyID> props2 = f2.listProperties().stream()
                .map(SmartPropertyListInfo::getPropertyid)
                .collect(Collectors.toSet());
        props1.addAll(props2);
        compareProperties(props1);
    }

    public void compareProperty(CurrencyID propertyId) throws IOException, InterruptedException {
        compareProperties(Collections.singleton(propertyId));
    }

    void compareProperties(Set<CurrencyID> propertiesToCompare) throws IOException, InterruptedException {
        for (CurrencyID id : propertiesToCompare) {
            log.info("fetching ID:{} from fetcher 1", id);
            ConsensusSnapshot ss1 = f1.getConsensusSnapshot(id);
            log.info("fetching ID:{} from fetcher 2", id);
            ConsensusSnapshot ss2 = f2.getConsensusSnapshot(id);
            log.info("comparing {} h1:{} h2:{}", id, ss1.getBlockHeight(), ss2.getBlockHeight());
            ConsensusComparison comparison = new ConsensusComparison(ss1, ss2);

            comparison.forEach(pair -> {
                if (pair.getEntry1() != pair.getEntry2()) {
                    System.out.println(pair.getAddress() + " " + id + ": " + pair.getEntry1() + "!=" + pair.getEntry2());
                }
            });
        }
    }
}
