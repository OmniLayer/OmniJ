package foundation.omni.consensus

import com.msgilligan.bitcoinj.rpc.RPCURI
import foundation.omni.CurrencyID
import foundation.omni.rpc.BalanceEntry
import foundation.omni.rpc.ConsensusSnapshot
import foundation.omni.rpc.OmniClient
import foundation.omni.rpc.SmartPropertyListInfo
import foundation.omni.rpc.test.TestServers
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.bitcoinj.core.Address
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.params.MainNetParams

/**
 * Command-line tool and class for fetching Omni Core consensus data
 */
@CompileStatic
class OmniCoreConsensusTool extends OmniCoreConsensusFetcher implements ConsensusTool {
    protected OmniClient client

    /**
     * URI Constructor
     *
     * @netParams *bitcoinj* NetworkParameters for server to connect to
     * @param coreURI URI to connect to - user/pass if required, must be encoded in URL
     */
    OmniCoreConsensusTool(NetworkParameters netParams, URI coreURI)
    {
        super(netParams, coreURI);
    }

    /**
     * Constructor that takes an existing OmniClient
     *
     * @param client An existing client instance
     */
    OmniCoreConsensusTool(OmniClient client)
    {
        super(client);
    }

    public static void main(String[] args) {
        OmniClient client = new OmniClient(MainNetParams.get(), RPCURI.defaultMainNetURI, TestServers.instance.rpcTestUser, TestServers.instance.rpcTestPassword)
        OmniCoreConsensusTool tool = new OmniCoreConsensusTool(client)
        tool.run(args.toList())
    }



}
