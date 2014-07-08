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
    static def rpcproto = "http"
    static def rpchost = "127.0.0.1"
    static def rpcport = 8332
    static def rpcfile = "/"
    static def rpcuser = "bitcoinrpc"
    static def rpcpassword = "pass"

    @Shared
    MastercoinClient client;
    @Shared
    MasterCoreConsensusFetcher mscFetcher;
    @Shared
    OmniwalletConsensusFetcher omniFetcher;
    @Shared
    Long currencyMSC = 1L

    void setupSpec() {
        def rpcServerURL = new URL(rpcproto, rpchost, rpcport, rpcfile)
        client = new MastercoinClient(rpcServerURL, rpcuser, rpcpassword)
        System.out.println("Waiting for server")
        Boolean available = client.waitForServer(60*60)   // Wait up to 1 hour
        if (!available) {
            System.out.println("Timeout error.")
        }

        mscFetcher = new MasterCoreConsensusFetcher()
        omniFetcher = new OmniwalletConsensusFetcher()
    }


    def "returns mastercoin version along with basic info" () {
        when: "we request info"
        def info = client.getInfo()

        then: "we get back some mastercoin information, too"
        info != null
        info.mastercoreversion >= 10003
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


    def "Compare Omni & Mastercore"() {

        when: "we get data from both sources"

        Map<String, ConsensusBalance> mscConsensus = mscFetcher.getConsensusForCurrency(currencyMSC)
        Map<String, ConsensusBalance> omniConsensus = omniFetcher.getConsensusForCurrency(currencyMSC)

        then: "it matches"

        mscConsensus == omniConsensus
        mscConsensus.size() == omniConsensus.size()         // Redundant given above test
    }
}
