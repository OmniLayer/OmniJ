package foundation.omni.consensus

import foundation.omni.CurrencyID

/**
 * Base class for fetching Master Protocol consensus data
 */

abstract class ConsensusTool implements ConsensusFetcher {
    void run(List args) {
        String currencyString = args[0]
        Long currencyLong =  Long.parseLong(currencyString, 10)
        CurrencyID currencyID = currencyString ? new CurrencyID(currencyLong) : CurrencyID.MSC

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
        file.withWriter { out ->
            snap.entries.each { addr, cb ->
                out.writeLine("${addr}\t${cb.balance}\t${cb.reserved}")
            }
        }

    }

    void print(ConsensusSnapshot consensus) {
        consensus.entries.each {  address, ConsensusEntry bal ->
            println "${address}: ${bal.balance}"
        }
    }
}
