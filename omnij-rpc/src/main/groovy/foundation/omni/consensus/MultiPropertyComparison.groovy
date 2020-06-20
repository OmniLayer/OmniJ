package foundation.omni.consensus

import foundation.omni.CurrencyID
import foundation.omni.rpc.ConsensusFetcher
import foundation.omni.rpc.ConsensusSnapshot
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

/**
 * Consensus comparison across all properties
 */
@Slf4j
@CompileStatic
class MultiPropertyComparison {
    private final ConsensusFetcher f1
    private final ConsensusFetcher f2

    MultiPropertyComparison(ConsensusFetcher left, ConsensusFetcher right) {
        this.f1 = left
        this.f2 = right
    }

    void compareAllProperties() {
        Set<CurrencyID> props1 = f1.listProperties().collect{it.propertyid}.toSet()
        Set<CurrencyID> props2 = f2.listProperties().collect{it.propertyid}.toSet()
        compareProperties(props1 + props2)
    }

    void compareProperty(CurrencyID propertyId) {
        compareProperties(Collections.singleton(propertyId))
    }

    void compareProperties(Set<CurrencyID> propertiesToCompare) {
        propertiesToCompare.each { id ->
            log.debug("fetching ID:${id} from fetcher 1")
            ConsensusSnapshot ss1 = f1.getConsensusSnapshot(id)
            log.debug("fetching ID:${id} from fetcher 2")
            ConsensusSnapshot ss2 = f2.getConsensusSnapshot(id)
            log.debug("comparing ${id} h1:${ss1.blockHeight} h2:${ss2.blockHeight}")
            ConsensusComparison comparison = new ConsensusComparison(ss1, ss2)

            comparison.each { pair ->
                if (pair.entry1 != pair.entry2) {
                        println "${pair.address} ${id}: ${pair.entry1} != ${pair.entry2}"
                }
            }
        }
    }
}
