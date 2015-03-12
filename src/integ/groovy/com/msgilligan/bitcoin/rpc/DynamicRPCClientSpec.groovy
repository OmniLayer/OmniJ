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
    def "test getblockcount"() {
        when:
        Map response = client.getblockcount()

        then:
        response.error == null
        response.result >= 0
    }

    def "test setgenerate"() {
        when:
        Map response = client.setgenerate(true, 2)

        then:
        response.error == null
    }

    def "test non-existent method"() {
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