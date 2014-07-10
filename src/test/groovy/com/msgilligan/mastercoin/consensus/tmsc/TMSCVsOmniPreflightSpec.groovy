package com.msgilligan.mastercoin.consensus.tmsc

import com.msgilligan.bitcoin.rpc.MastercoinClient
import com.msgilligan.mastercoin.consensus.ConsensusSnapshot
import com.msgilligan.mastercoin.consensus.MasterCoreConsensusFetcher
import com.msgilligan.mastercoin.consensus.OmniwalletConsensusFetcher
import groovy.json.JsonSlurper
import spock.lang.Shared
import spock.lang.Specification

/**
 * User: sean
 * Date: 7/10/14
 * Time: 1:36 AM
 */
class TMSCVsOmniPreflightSpec extends Specification {
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
    Long currencyTMSC = 2L
    @Shared
    Long currencyID = currencyTMSC

    void setupSpec() {
        def rpcServerURL = new URL(rpcproto, rpchost, rpcport, rpcfile)
        client = new MastercoinClient(rpcServerURL, rpcuser, rpcpassword)
        System.err.println("Waiting for server...")
        Boolean available = client.waitForServer(60 * 60)   // Wait up to 1 hour
        if (!available) {
            System.err.println("Timeout error.")
        }

        //
        // Get in sync with Blockchain.info
        //
        def curHeight = 0
        def newHeight = new JsonSlurper().parse(new URL("http://blockchain.info/latestblock")).height
        println "Blockchain.info current height: ${newHeight}"
        while (newHeight > curHeight) {
            curHeight = newHeight
            Boolean upToDate = client.waitForSync(curHeight, 60 * 60)
            newHeight = new JsonSlurper().parse(new URL("http://blockchain.info/latestblock")).height
            println "Blockchain.info current height: ${newHeight}"
        }

        mscFetcher = new MasterCoreConsensusFetcher()
        mscSnapshot = mscFetcher.getConsensusSnapshot(currencyID)

        omniFetcher = new OmniwalletConsensusFetcher()
        omniSnapshot = omniFetcher.getConsensusSnapshot(currencyID)
    }

    def "Can get Mastercore consensus data"() {

        when: "we get data"

        then: "it is there"
        mscSnapshot.currencyID == currencyID
        mscSnapshot.entries.size() >= 1
    }

    def "Can get Omniwallet consensus data"() {

        when: "we get data"

        then: "it is there"
        omniSnapshot.currencyID == currencyID
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
        def omniExtra =  (omniSnapshot.entries - mscSnapshot.entries).keySet()

        then: "Omni should not have any extra entries"
        omniExtra == [:]
    }

    def "Compare Omni & Mastercore: Master Core should not have extra entries"() {

        when: "we have snapshots from both sources"
        def mscExtra =  (mscSnapshot.entries - omniSnapshot.entries).keySet()

        then: "Master Core should not have any extra entries"
        mscExtra == [:]
    }

    def "Compare Omni & Mastercore: Balances should match"() {

        when: "we have snapshots from both sources"
        def intersectingAddresses = omniSnapshot.entries.intersect(mscSnapshot.entries).keySet()
        TreeMap<String, BigDecimal> mscIntersect =  mscSnapshot.entries.subMap(intersectingAddresses).collectEntries { key, value -> [ key, value.balance ]}
        TreeMap<String, BigDecimal> OmniIntersect =  omniSnapshot.entries.subMap(intersectingAddresses).collectEntries { key, value -> [ key, value.balance ]}

        then: "All balances in both maps should match"
        mscIntersect == OmniIntersect
    }

    def "Compare Omni & Mastercore: Reserved should match"() {

        when: "we have snapshots from both sources"
        def intersectingAddresses = omniSnapshot.entries.intersect(mscSnapshot.entries).keySet()
        TreeMap<String, BigDecimal> mscIntersect =  mscSnapshot.entries.subMap(intersectingAddresses).collectEntries { key, value -> [ key,  value.reserved ]}
        TreeMap<String, BigDecimal> OmniIntersect =  omniSnapshot.entries.subMap(intersectingAddresses).collectEntries { key, value -> [ key, value.reserved ]}

        then: "All balances in both maps should match"
        mscIntersect == OmniIntersect
    }

}
