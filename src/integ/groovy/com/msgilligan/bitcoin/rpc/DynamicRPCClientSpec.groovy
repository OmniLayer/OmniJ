package com.msgilligan.bitcoin.rpc

import foundation.omni.BaseMainNetSpec
import foundation.omni.rpc.OmniCLIClient
import spock.lang.Shared
import spock.lang.Specification


/**
 * Test DynamicRPCClient against a Bitcoin RPC server in RegTest mode
 *
 */
class DynamicRPCClientSpec extends Specification {
    @Shared
    DynamicRPCClient client

    void setupSpec() {
        client = new DynamicRPCClient(RPCURI.defaultRegTestURI, BaseMainNetSpec.rpcuser, BaseMainNetSpec.rpcpassword)

// TODO: Need to implement waitForServer()
// waitForServer() is in BitcoinClient because it uses getBlockCount()
// Either implement something that uses a non-existent method and wait for a "invalid method" response
// to indicate server is up or create a Base BitcoinRPC that has waitForServer() but not static RPC methods

//        log.info "Waiting for server..."
//        Boolean available = client.waitForServer(60)   // Wait up to 1 minute
//        if (!available) {
//            log.error "Timeout error."
//        }
//        assert available
    }
    def "getblockcount"() {
        when:
        def result = client.getblockcount()

        then:
        result >= 0
    }

    def "setgenerate"() {
        when:
        def result = client.setgenerate(true, 2)

        then:
        result == null
    }

    def "getinfo" () {
        when:
        def info = client.getinfo()

        then:
        info != null
        info.version >= 90100
        info.protocolversion >= 70002

    }

    def "non-existent method throws JsonRPCStatusException"() {
        when:
        client.idontexist("parm", 2)

        then:
        JsonRPCStatusException e = thrown()
        e.message == "Method not found"
        e.httpMessage == "Not Found"
        e.httpCode == 404
        e.response == null
        e.responseJson.result == null
        e.responseJson.error.code == -32601
        e.responseJson.error.message == "Method not found"
    }

}