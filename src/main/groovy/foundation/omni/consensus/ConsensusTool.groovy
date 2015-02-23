package foundation.omni.consensus

import foundation.omni.CurrencyID
import groovy.transform.TypeChecked
import org.bitcoinj.core.Address

/**
 * Base class for fetching Master Protocol consensus data
 */
@TypeChecked
abstract class ConsensusTool implements ConsensusFetcher {
    void run(List<String> args) {
        Long currencyIDNum =  args[0] ? Long.parseLong(args[0], 10) : CurrencyID.MSC_VALUE
        CurrencyID currencyID = new CurrencyID(currencyIDNum)

        String fileName = args[1]

        def consensus = this.getConsensusSnapshot(currencyID)

        if (fileName != null) {
            File output = new File(fileName)
            this.save(consensus, output)
        } else {
            this.print(consensus)
        }
    }

    void save(ConsensusSnapshot snap, File file) {
        output(snap, file.newPrintWriter(), true)
    }

    void print(ConsensusSnapshot snap) {
        output(snap, System.out.newPrintWriter(), false)
    }

    void output(ConsensusSnapshot snap, PrintWriter writer, boolean tsv) {
        snap.entries.each { Address address, ConsensusEntry entry ->
            if (tsv) {
                writer.println("${address}\t${entry.balance}\t${entry.reserved}")
            } else {
                writer.println("${address}: ${entry.balance}, ${entry.reserved}")
            }
        }
    }
}
