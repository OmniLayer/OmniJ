package foundation.omni.test.consensus.remote_core.maidsafecoin

import com.msgilligan.bitcoin.rpc.RPCURI
import foundation.omni.consensus.OmniCoreConsensusTool
import foundation.omni.rpc.OmniClient
import foundation.omni.test.consensus.BaseConsensusSpec
import spock.lang.Title

import static foundation.omni.CurrencyID.MaidSafeCoin

/**
 */
@Title("Compare Master Core vs. Remote Master Core (e.g. stable/last revision)")
class CompareCoreCoreSpec extends BaseConsensusSpec {

    void setupSpec() {

        OmniClient remoteClient = new OmniClient(RPCURI.stablePublicMainNetURI, 'xmc-msc-rpc', 'emdERDIDE82934$%$')
        setupComparisonForCurrency(new OmniCoreConsensusTool(remoteClient),
                MaidSafeCoin)
    }
}
