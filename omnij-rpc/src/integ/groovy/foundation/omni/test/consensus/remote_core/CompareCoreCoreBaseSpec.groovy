package foundation.omni.test.consensus.remote_core

import foundation.omni.CurrencyID
import foundation.omni.consensus.OmniCoreConsensusTool
import foundation.omni.rpc.OmniClient
import foundation.omni.test.consensus.BaseConsensusSpec

/**
 *
 */
class CompareCoreCoreBaseSpec extends BaseConsensusSpec {

    void setupCoreCoreComparisonForCurrency(CurrencyID currencyID) {
        OmniClient remoteClient = new OmniClient(testServers.stablePublicMainNetURI, testServers.stableOmniRpcUser, testServers.stableOmniRpcPassword)
        OmniCoreConsensusTool remoteTool = new OmniCoreConsensusTool(remoteClient)
        setupComparisonForCurrency(remoteTool, currencyID)
    }

}