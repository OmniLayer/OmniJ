package foundation.omni.test.consensus.remote_core.msc

import com.msgilligan.bitcoin.rpc.RPCURI
import foundation.omni.consensus.OmniCoreConsensusTool
import foundation.omni.rpc.OmniClient
import foundation.omni.test.consensus.BaseConsensusSpec
import spock.lang.Title

import static foundation.omni.CurrencyID.MSC

/**
 */
@Title("Compare Master Core vs. Remote Master Core (e.g. stable/last revision)")
class CompareCoreCoreSpec extends BaseConsensusSpec {

    void setupSpec() {

        OmniClient remoteClient = new OmniClient(RPCURI.stablePublicMainNetURI, RPCURI.stableOmniRpcUser, RPCURI.stableOmniRpcPassword)
        setupComparisonForCurrency(new OmniCoreConsensusTool(remoteClient),
                MSC)
    }
}
