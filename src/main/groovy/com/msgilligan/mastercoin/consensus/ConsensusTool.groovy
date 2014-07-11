package com.msgilligan.mastercoin.consensus

/**
 * User: sean
 * Date: 7/11/14
 * Time: 12:35 PM
 */
abstract class ConsensusTool implements ConsensusFetcher {
    public static final Long currencyMSC = 1L

    void run(List args) {
        Long currency = args[0] ?: currencyMSC
        File output = args[1]

        def consensus = this.getConsensusSnapshot(currency)

        if (output != null) {
            this.save(new File(args[0]))
        } else {
            this.print(consensus)
        }
    }

    void save(ConsensusSnapshot snap, File file) {
        file.withWriter { out ->
            snap.entries.each { addr, cb ->
                out.writeLine("${cb.address}\t${cb.balance}\t${cb.reserved}")
            }
        }

    }

    void print(ConsensusSnapshot consensus) {
        consensus.entries.each {  address, ConsensusEntry bal ->
            println "${address}: ${bal.balance}"
        }
    }
}
