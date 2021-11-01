package foundation.omni.test

import org.consensusj.bitcoin.rpc.RpcURI
import org.consensusj.bitcoin.test.RegTestEnvironment
import foundation.omni.rpc.test.OmniTestClient
import org.bitcoinj.params.RegTestParams

/**
 *
 */
class RegTestContext {
    static setup(String user, String pass) {
        def client = new OmniTestClient(RegTestParams.get(), RpcURI.defaultRegTestURI, user, pass)
        def env = new RegTestEnvironment(client)
        def funder = new RegTestOmniFundingSource(client)
        return [client, env, funder]
    }
}
