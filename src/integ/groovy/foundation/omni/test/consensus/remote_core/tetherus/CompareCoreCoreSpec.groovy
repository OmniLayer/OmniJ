package foundation.omni.test.consensus.remote_core.tetherus

import foundation.omni.consensus.OmniCoreConsensusTool
import foundation.omni.rpc.OmniClient
import foundation.omni.test.TestServers
import foundation.omni.test.consensus.BaseConsensusSpec
import spock.lang.Title

import static foundation.omni.CurrencyID.TetherUS

/**
 */
@Title("Compare Master Core vs. Remote Master Core (e.g. stable/last revision)")
class CompareCoreCoreSpec extends BaseConsensusSpec {

    void setupSpec() {

        OmniClient remoteClient = new OmniClient(TestServers.stablePublicMainNetURI, TestServers.stableOmniRpcUser, TestServers.stableOmniRpcPassword)
        setupComparisonForCurrency(new OmniCoreConsensusTool(remoteClient),
                TetherUS)
    }
}
