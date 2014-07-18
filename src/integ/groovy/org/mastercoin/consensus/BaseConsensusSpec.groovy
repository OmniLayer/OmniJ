package org.mastercoin.consensus

import com.msgilligan.bitcoin.rpc.MastercoinClient
import groovy.json.JsonSlurper
import org.mastercoin.CurrencyID
import spock.lang.Shared
import spock.lang.Specification

/**
 * User: sean
 * Date: 7/9/14
 * Time: 11:31 PM
 */
abstract class  BaseConsensusSpec extends Specification {
    static def rpcproto = "http"
    static def rpchost = "127.0.0.1"
    static def rpcport = 8332
    static def rpcfile = "/"
    static def rpcuser = "bitcoinrpc"
    static def rpcpassword = "pass"

    @Shared
    MastercoinClient client;
    @Shared
    ConsensusSnapshot mscSnapshot
    @Shared
    ConsensusSnapshot omniSnapshot
    @Shared
    ConsensusComparison comparison

    void setupSpec() {
        def rpcServerURL = new URL(rpcproto, rpchost, rpcport, rpcfile)
        client = new MastercoinClient(rpcServerURL, rpcuser, rpcpassword)
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

        MasterCoreConsensusTool mscFetcher = new MasterCoreConsensusTool()
        mscSnapshot = mscFetcher.getConsensusSnapshot(CurrencyID.MSC_VALUE)

        OmniwalletConsensusTool omniFetcher = new OmniwalletConsensusTool()
        omniSnapshot = omniFetcher.getConsensusSnapshot(CurrencyID.MSC_VALUE)

        comparison = new ConsensusComparison(c1: mscSnapshot, c2: omniSnapshot)
    }
}
