package foundation.omni.test.consensus.remote_core

import foundation.omni.CurrencyID
import foundation.omni.consensus.OmniCoreConsensusTool
import foundation.omni.rpc.OmniClient
import foundation.omni.test.consensus.BaseConsensusSpec
import org.bitcoinj.base.BitcoinNetwork

/**
 * Base class for Consensus tests between local and remote Omni Core instances
 */
abstract class CompareCoreCoreBaseSpec extends BaseConsensusSpec {

    void setupCoreCoreComparisonForCurrency(CurrencyID currencyID) {
        // Create a client for the remote Omni Core instance
        OmniClient remoteClient = new OmniClient(BitcoinNetwork.MAINNET, testServers.stablePublicMainNetURI, testServers.stableOmniRpcUser, testServers.stableOmniRpcPassword)
        // Use it to create a consensus tool
        OmniCoreConsensusTool remoteTool = new OmniCoreConsensusTool(remoteClient)
        // Use consensus tool to create a consensus comparison for the specified currency ID
        setupComparisonForCurrency(remoteTool, currencyID)
    }

}