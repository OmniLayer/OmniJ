package com.msgilligan.bitcoin.rpc

import foundation.omni.BaseRegTestSpec
import spock.lang.Specification


/**
 *
 * Test of CLI API methods (all lowercase, matching RPC as closely as possible)
 *
 */
class BitcoinCLIAPISpec extends BaseRegTestSpec {

    def "return basic info" () {
        when: "we request info"
        def info = getinfo()

        then: "we get back some basic information"
        info != null
        info.version >= 90100
        info.protocolversion >= 70002
    }


    def "Generate a block upon request"() {
        given: "a certain starting height"
        def startHeight = getblockcount()

        when: "we generate 1 new block"
        setgenerate(true, 1)

        then: "the block height is 1 higher"
        getblockcount() == startHeight + 1

    }

}