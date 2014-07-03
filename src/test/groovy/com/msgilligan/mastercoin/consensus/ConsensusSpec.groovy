package com.msgilligan.mastercoin.consensus

import com.msgilligan.bitcoin.rpc.MastercoinClient
import spock.lang.Shared
import spock.lang.Specification

/**
 * User: sean
 * Date: 7/3/14
 * Time: 11:54 AM
 */
class ConsensusSpec extends Specification {
    @Shared
    MasterCoreConsensusFetcher mscFetcher;
    @Shared
    OmniwalletConsensusFetcher omniFetcher;
    @Shared
    Long currencyMSC = 1L

    void setupSpec() {
        mscFetcher = new MasterCoreConsensusFetcher()
        omniFetcher = new OmniwalletConsensusFetcher()
    }

    def "get Mastercore consensus data"() {

        when: "we get data"

        def consensus = mscFetcher.getConsensusForCurrency(currencyMSC)

        then: "it is there"
            consensus.size() >= 1
    }

    def "get Omniwallet consensus data"() {

        when: "we get data"

        def consensus = omniFetcher.getConsensusForCurrency(currencyMSC)

        then: "it is there"

        consensus.size() >= 1
    }


    def "compare Omni & Mastercore"() {

        when: "we get data from both sources"

        def mscConsensus = omniFetcher.getConsensusForCurrency(currencyMSC)
        def omniConsensus = mscFetcher.getConsensusForCurrency(currencyMSC)

        then: "it matches"

//        mscConsensus == omniConsensus
        mscConsensus.size() == omniConsensus.size()         // Redundant given above test
    }
}
