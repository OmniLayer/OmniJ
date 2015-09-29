package foundation.omni.test

import com.msgilligan.bitcoinj.rpc.RPCURI
import com.msgilligan.bitcoinj.test.RegTestEnvironment
import foundation.omni.rpc.OmniCLIClient

/**
 *
 */
class RegTestContext {
    static setup(String user, String pass) {
        def client = new OmniCLIClient(RPCURI.defaultRegTestURI, user, pass)
        def env = new RegTestEnvironment(client)
        def funder = new RegTestOmniFundingSource(client)
        return [client, env, funder]
    }
}
