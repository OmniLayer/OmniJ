package foundation.omni.test

import foundation.omni.rpc.test.OmniTestClient
import org.bitcoinj.base.BitcoinNetwork
import org.consensusj.bitcoin.jsonrpc.RpcURI
import org.consensusj.bitcoin.jsonrpc.test.RegTestEnvironment

/**
 *
 */
class RegTestContext {
    static setup(String user, String pass) {
        def client = new OmniTestClient(BitcoinNetwork.REGTEST, RpcURI.defaultRegTestURI, user, pass)
        def env = new RegTestEnvironment(client)
        def funder = new RegTestOmniFundingSource(client)
        return [client, env, funder]
    }
}
