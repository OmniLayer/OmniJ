package org.mastercoin.test.consensus

import groovy.json.JsonSlurper
import org.mastercoin.BaseMainNetSpec
import org.mastercoin.CurrencyID
import org.mastercoin.consensus.MasterCoreConsensusTool
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

import static org.mastercoin.CurrencyID.*

class RPCSmokeTestSpec extends BaseMainNetSpec {

    def "Master Core RPC is working" () {
        setup: "no setup required here"

        expect: "client is not null"
        client != null

        when: "we request info"
        def info = getInfo()

        then: "we get back some version information, too"
        info != null
        info.version >= 90000
    }

    def "Can get Mastercore consensus data"() {
        setup:
        def mscFetcher = new MasterCoreConsensusTool(client)

        when: "we get data"
        def mscSnapshot = mscFetcher.getConsensusSnapshot(MSC)

        then: "it is there"
        mscSnapshot.currencyID == MSC
        mscSnapshot.entries.size() >= 1
    }

}
