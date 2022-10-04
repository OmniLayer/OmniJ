package foundation.omni.test

import foundation.omni.rpc.test.OmniTestClient
import org.consensusj.bitcoin.jsonrpc.RpcURI
import org.consensusj.bitcoin.jsonrpc.test.RegTestEnvironment
import org.bitcoinj.params.RegTestParams
import org.consensusj.bitcoin.jsonrpc.test.RegTestFundingSource

/**
 *
 */
class RegTestContext {
    static setup(String user, String pass) {
        def client = new OmniTestClient(RegTestParams.get(), RpcURI.defaultRegTestURI, user, pass)
        def env = new RegTestEnvironment(client)
        def funder = new RegTestOmniFundingSource(client, new RegTestFundingSource(client))
        return [client, env, funder]
    }
}
