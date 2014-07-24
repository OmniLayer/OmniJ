package org.mastercoin.consensus

import org.mastercoin.CurrencyID

/**
 * User: sean
 * Date: 7/11/14
 * Time: 12:35 PM
 */
abstract class ConsensusTool implements ConsensusFetcher {
    void run(List args) {
        String currencyString = args[0]
        CurrencyID currencyID = currencyString ? new CurrencyID(currencyString) : CurrencyID.MSC

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
