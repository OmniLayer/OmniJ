package foundation.omni.test

import com.msgilligan.bitcoinj.rpc.RpcURI
import com.msgilligan.bitcoinj.test.RegTestEnvironment
import foundation.omni.rpc.test.OmniTestClient
import org.bitcoinj.params.RegTestParams

/**
 *
 */
class RegTestContext {
    static setup(String user, String pass) {
        // If Bitcoin Core 0.16.0 or greater, rpc port for RegTest has changed. ConsensusJ 0.5.0
        // reflects this change, but Travis tests are still testing an old Omni Core.
        // Previously Bitcoin Core (and Omni Core) used the same port as TESTNET for REGTEST
        // This recentBitcoinCore hack allows those tests to pass until we update `travis.yml`
        // and any other test configuration/infrastructure, etc.
        def recentBitcoinCore = false;
        URI regTestRpcUri = recentBitcoinCore ? RpcURI.defaultRegTestURI : RpcURI.defaultTestNetURI
        def client = new OmniTestClient(RegTestParams.get(), regTestRpcUri, user, pass)
        def env = new RegTestEnvironment(client)
        def funder = new RegTestOmniFundingSource(client)
        return [client, env, funder]
    }
}
