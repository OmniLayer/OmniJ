package org.mastercoin.test.consensus.remote_core.msc

import com.msgilligan.bitcoin.rpc.RPCURL
import org.mastercoin.consensus.MasterCoreConsensusTool
import org.mastercoin.consensus.OmniwalletConsensusTool
import org.mastercoin.rpc.MastercoinClient
import org.mastercoin.test.consensus.BaseConsensusSpec
import spock.lang.Title

import static org.mastercoin.CurrencyID.MSC

/**
 */
@Title("Compare Master Core vs. Remote Master Core (e.g. stable/last revision)")
class CompareCoreCoreSpec extends BaseConsensusSpec {

    void setupSpec() {

        MastercoinClient remoteClient = new MastercoinClient(RPCURL.stablePublicMainNetURL, 'xmc-msc-rpc', 'emdERDIDE82934$%$')
        setupComparisonForCurrency(new MasterCoreConsensusTool(remoteClient),
                MSC)
    }
}
