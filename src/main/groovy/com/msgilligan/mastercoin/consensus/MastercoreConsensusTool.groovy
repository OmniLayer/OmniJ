package com.msgilligan.mastercoin.consensus

import java.text.NumberFormat

/**
 * User: sean
 * Date: 7/3/14
 * Time: 12:41 PM
 */
class MastercoreConsensusTool {
    public static void main(String[] args) {
        MasterCoreConsensusFetcher mscFetcher;
        Long currencyMSC = 1L
        NumberFormat format = NumberFormat.getNumberInstance()

        mscFetcher = new MasterCoreConsensusFetcher()

        def mscConsensus = mscFetcher.getConsensusSnapshot(currencyMSC)

        def outputFile = new File("msc_consensus.txt")

        outputFile.withWriter { out ->
            mscConsensus.entries.each { addr, cb ->
                out.writeLine("${cb.address}\t${cb.balance}\t${cb.reserved}")
            }
        }

    }
}
