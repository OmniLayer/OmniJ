package foundation.omni.test.consensus.remote_core.msc

import foundation.omni.consensus.OmniCoreConsensusTool
import foundation.omni.rpc.OmniClient
import foundation.omni.rpc.test.TestServers
import foundation.omni.test.consensus.BaseConsensusSpec
import spock.lang.Title

import static foundation.omni.CurrencyID.MSC

/**
 */
@Title("Compare Master Core vs. Remote Master Core (e.g. stable/last revision)")
class CompareCoreCoreSpec extends BaseConsensusSpec {

    void setupSpec() {

        OmniClient remoteClient = new OmniClient(testServers.stablePublicMainNetURI, testServers.stableOmniRpcUser, testServers.stableOmniRpcPassword)
        setupComparisonForCurrency(new OmniCoreConsensusTool(remoteClient),
                MSC)
    }
}
