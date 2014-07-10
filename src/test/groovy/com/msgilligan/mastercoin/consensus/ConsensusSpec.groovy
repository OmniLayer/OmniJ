package com.msgilligan.mastercoin.consensus

import com.msgilligan.bitcoin.rpc.MastercoinClient
import groovy.json.JsonSlurper
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Unroll

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
    ConsensusSnapshot omniSnapshot
    @Shared
    ConsensusSnapshot mscSnapshot
    @Shared
    Long currencyMSC = 1L

    void setupSpec() {
        def rpcServerURL = new URL(rpcproto, rpchost, rpcport, rpcfile)
        client = new MastercoinClient(rpcServerURL, rpcuser, rpcpassword)
        System.err.println("Waiting for server...")
        Boolean available = client.waitForServer(60*60)   // Wait up to 1 hour
        if (!available) {
            System.err.println("Timeout error.")
        }


        //
        // Get in sync with Blockchain.info
        //
        def curHeight = 0
        def newHeight = new JsonSlurper().parse(new URL("http://blockchain.info/latestblock")).height
        println "Blockchain.info current height: ${newHeight}"
        while ( newHeight > curHeight ) {
            curHeight = newHeight
            Boolean upToDate = client.waitForSync(curHeight, 60*60)
            newHeight = new JsonSlurper().parse(new URL("http://blockchain.info/latestblock")).height
            println "Blockchain.info current height: ${newHeight}"
        }

        mscFetcher = new MasterCoreConsensusFetcher()
        omniFetcher = new OmniwalletConsensusFetcher()
    }


    def "Master Core RPC is working" () {
        when: "we request info"
        def info = client.getInfo()

        then: "we get back some mastercoin information, too"
        info != null
        info.mastercoreversion >= 10003
    }

    def "Can get Mastercore consensus data"() {

        when: "we get data"
        mscSnapshot = mscFetcher.getConsensusSnapshot(currencyMSC)

        then: "it is there"
        mscSnapshot.currencyID ==  currencyMSC
        mscSnapshot.entries.size() >= 1
    }

    def "Can get Omniwallet consensus data"() {

        when: "we get data"
        omniSnapshot = omniFetcher.getConsensusSnapshot(currencyMSC)

        then: "it is there"
        omniSnapshot.currencyID == currencyMSC
        omniSnapshot.entries.size() >= 1
    }


    def "Compare Omni & Mastercore: Number of consensus entries"() {

        when: "we have snapshots from both sources, and get the sizes"
        def mscSize = mscSnapshot.entries.size()
        def omniSize = omniSnapshot.entries.size()

        then: "They both have the same number of entries"
        mscSize == omniSize
    }

    def "Compare Omni & Mastercore: Omni should not have extra entries"() {

        when: "we have snapshots from both sources"
        def omniExtra =  omniSnapshot.entries - mscSnapshot.entries

        then: "Omni should not have any extra entries"
        omniExtra == [:]
    }

    def "Compare Omni & Mastercore: Master Core should not have extra entries"() {

        when: "we have snapshots from both sources"
        def mscExtra =  mscSnapshot.entries - omniSnapshot.entries

        then: "Master Core should not have any extra entries"
        mscExtra == [:]
    }

    @Unroll
    def "compare #address balance msc vs omni (#mscBalance == #omniBalance)"() {
        expect:
        omniBalance == mscBalance

        where:
        address << omniSnapshot.entries.keySet()
        omniBalance = omniSnapshot.entries[address].balance
        mscBalance = mscSnapshot.entries[address].balance
    }
}
