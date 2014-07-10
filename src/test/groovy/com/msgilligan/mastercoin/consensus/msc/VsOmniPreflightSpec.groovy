package com.msgilligan.mastercoin.consensus.msc

import com.msgilligan.bitcoin.rpc.MastercoinClient
import com.msgilligan.mastercoin.consensus.BaseConsensusSpec
import com.msgilligan.mastercoin.consensus.ConsensusSnapshot
import com.msgilligan.mastercoin.consensus.MasterCoreConsensusFetcher
import com.msgilligan.mastercoin.consensus.OmniwalletConsensusFetcher
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
class VsOmniPreflightSpec extends BaseConsensusSpec {

    def "Master Core RPC is working" () {
        when: "we request info"
        def info = client.getInfo()

        then: "we get back some mastercoin information, too"
        info != null
        info.mastercoreversion >= 10003
    }

    def "Can get Mastercore consensus data"() {

        when: "we get data"

        then: "it is there"
        mscSnapshot.currencyID ==  currencyMSC
        mscSnapshot.entries.size() >= 1
    }

    def "Can get Omniwallet consensus data"() {

        when: "we get data"

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
    def "#address extra in Omni"() {
        expect:
        address == null

        where:
        address << omniSnapshot.entries - mscSnapshot.entries
    }

    @Unroll
    def "#address extra in Master"() {
        expect:
        address == null

        where:
        address << mscSnapshot.entries - omniSnapshot.entries
    }

    @Unroll
    def "compare #address balance msc vs omni (#mscBalance == #omniBalance)"() {
        expect:
        omniBalance == mscBalance

        where:
        address << omniSnapshot.entries.intersect(mscSnapshot.entries).keySet()
        omniBalance = omniSnapshot.entries[address].balance
        mscBalance = mscSnapshot.entries[address].balance
    }
}
