package com.msgilligan.mastercoin.consensus

/**
 * User: sean
 * Date: 7/8/14
 * Time: 12:31 PM
 */
class OmniConsensusTool {
    public static void main(String[] args) {
        OmniwalletConsensusFetcher fetcher;
        Long currencyMSC = 1L

        fetcher = new OmniwalletConsensusFetcher()

        def omniConsensus = fetcher.getConsensusForCurrency(currencyMSC)

        def outputFile = new File("omni_consensus.txt")

        outputFile.withWriter { out ->
            omniConsensus.each { addr, cb ->
                out.writeLine("${cb.address}\t${cb.balance}\t${cb.reserved}")
            }
        }

    }
}
