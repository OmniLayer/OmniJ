package org.mastercoin

import com.msgilligan.bitcoin.rpc.RPCURL
import org.mastercoin.rpc.MastercoinCLIClient
import org.mastercoin.rpc.MastercoinClient
import groovy.json.JsonSlurper
import org.mastercoin.rpc.MastercoinClientDelegate
import spock.lang.Shared
import spock.lang.Specification

/**
 * User: sean
 * Date: 7/20/14
 * Time: 12:53 AM
 */
abstract class BaseMainNetSpec extends Specification implements MastercoinClientDelegate {
    {
        client = new MastercoinCLIClient(RPCURL.defaultMainNetURL, BaseMainNetSpec.rpcuser, BaseMainNetSpec.rpcpassword)
    }
//abstract class BaseMainNetSpec extends Specification  {
    static final String rpcuser = "bitcoinrpc"
    static final String rpcpassword = "pass"

//    @Shared
//    MastercoinClient client;

    void setupSpec() {
//        client = new MastercoinClient(RPCURL.defaultMainNetURL, rpcuser, rpcpassword)
        System.err.println("Waiting for server...")
        Boolean available = client.waitForServer(3*60*60)   // Wait up to 3 hours
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
    }

//    def methodMissing(String name, args) {
//        client."$name"(*args)
//    }
//
//    def propertyMissing(String name) {
//        client."$name"
//    }
//
//    def propertyMissing(String name, value) {
//        client."$name" = value
//    }

}
