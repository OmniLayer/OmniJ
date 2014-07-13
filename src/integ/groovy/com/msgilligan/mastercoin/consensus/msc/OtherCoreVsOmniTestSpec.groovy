package com.msgilligan.mastercoin.consensus.msc

import com.msgilligan.mastercoin.consensus.BaseConsensusSpec
import org.mastercoin.CurrencyID

/**
 * User: sean
 * Date: 7/3/14
 * Time: 11:54 AM
 */
class OtherCoreVsOmniTestSpec extends BaseConsensusSpec {

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
        mscSnapshot.currencyID ==  CurrencyID.MSC_VALUE
        mscSnapshot.entries.size() >= 1
    }

    def "Can get Omniwallet consensus data"() {

        when: "we get data"

        then: "it is there"
        omniSnapshot.currencyID == CurrencyID.MSC_VALUE
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
        def omniExtra =  omniSnapshot.entries.keySet() - mscSnapshot.entries.keySet()

        then: "Omni should not have any extra entries"
        omniExtra.size() == 0
    }

    def "Compare Omni & Mastercore: Master Core should not have extra entries"() {

        when: "we have snapshots from both sources"
//        def mscExtraMap =  mscSnapshot.entries - omniSnapshot.entries
        def mscExtra =  mscSnapshot.entries.keySet() - omniSnapshot.entries.keySet()

        then: "Master Core should not have any extra entries"
        mscExtra.size() == 0
    }

    def "Compare Omni & Mastercore: Extra entries should match"() {

        when: "we have snapshots from both sources"
        def mscExtra =  (mscSnapshot.entries - omniSnapshot.entries).keySet()
        def omniExtra =  (omniSnapshot.entries - mscSnapshot.entries).keySet()

        then: "keysets should be the same"
        mscExtra == omniExtra
    }

    def "Compare Omni & Mastercore: Balances should match"() {

        when: "we have snapshots from both sources"
        def intersectingAddresses = comparison.addressIntersection()
        TreeMap<String, BigDecimal> mscIntersect =  mscSnapshot.entries.subMap(intersectingAddresses).collectEntries { key, value -> [ key, value.balance ]}
        TreeMap<String, BigDecimal> OmniIntersect =  omniSnapshot.entries.subMap(intersectingAddresses).collectEntries { key, value -> [ key, value.balance ]}

        then: "All balances in both maps should match"
        mscIntersect == OmniIntersect
    }

    def "Compare Omni & Mastercore: Reserved should match"() {

        when: "we have snapshots from both sources"
        def intersectingAddresses = comparison.addressIntersection()
        TreeMap<String, BigDecimal> mscIntersect =  mscSnapshot.entries.subMap(intersectingAddresses).collectEntries { key, value -> [ key,  value.reserved ]}
        TreeMap<String, BigDecimal> OmniIntersect =  omniSnapshot.entries.subMap(intersectingAddresses).collectEntries { key, value -> [ key, value.reserved ]}

        then: "All balances in both maps should match"
        mscIntersect == OmniIntersect
    }
}
