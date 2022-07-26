package foundation.omni.test

import foundation.omni.netapi.omnicore.RxOmniTestClient
import org.consensusj.bitcoin.jsonrpc.RpcURI
import org.consensusj.bitcoin.jsonrpc.test.RegTestEnvironment
import org.bitcoinj.params.RegTestParams

/**
 *
 */
class RegTestContext {
    static setup(String user, String pass) {
        def client = new RxOmniTestClient(RegTestParams.get(), RpcURI.defaultRegTestURI, user, pass)
        def env = new RegTestEnvironment(client)
        def funder = new RegTestOmniFundingSource(client)
        return [client, env, funder]
    }
}
