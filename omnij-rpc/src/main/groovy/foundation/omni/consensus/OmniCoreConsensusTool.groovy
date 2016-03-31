package foundation.omni.consensus

import com.msgilligan.bitcoinj.rpc.RPCURI
import foundation.omni.CurrencyID
import foundation.omni.rpc.BalanceEntry
import foundation.omni.rpc.ConsensusSnapshot
import foundation.omni.rpc.OmniClient
import foundation.omni.rpc.SmartPropertyListInfo
import foundation.omni.rpc.test.TestServers
import groovy.transform.TypeChecked
import org.bitcoinj.core.Address
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.params.MainNetParams

/**
 * Command-line tool and class for fetching Omni Core consensus data
 */
class OmniCoreConsensusTool implements ConsensusTool {
    protected OmniClient client

    /**
     * URI Constructor
     *
     * @netParams *bitcoinj* NetworkParameters for server to connect to
     * @param coreURI URI to connect to - user/pass if required, must be encoded in URL
     */
    OmniCoreConsensusTool(NetworkParameters netParams, URI coreURI)
    {
        String user = ""
        String pass = ""
        String userInfo = coreURI.getUserInfo()
        if (userInfo != null) {
            String[] userpass = userInfo.split(':')
            user = userpass[0]
            pass = userpass[1]
        }
        OmniClient client = new OmniClient(netParams, coreURI, user, pass)
        this.client = client
    }

    /**
     * Constructor that takes an existing OmniClient
     *
     * @param client An existing client instance
     */
    OmniCoreConsensusTool(OmniClient client)
    {
        this.client = client
    }

    public static void main(String[] args) {
        OmniClient client = new OmniClient(MainNetParams.get(), RPCURI.defaultMainNetURI, TestServers.instance.rpcTestUser, TestServers.instance.rpcTestPassword)
        OmniCoreConsensusTool tool = new OmniCoreConsensusTool(client)
        tool.run(args.toList())
    }

    private SortedMap<Address, BalanceEntry> getConsensusForCurrency(CurrencyID currencyID) {
        SortedMap<Address, BalanceEntry> balances = client.omniGetAllBalancesForId(currencyID)
        // TODO: Filter out empty address strings or 0 balances?
        return balances;
    }

    @Override
    Integer currentBlockHeight() {
        return client.getBlockCount()
    }

    @Override
    @TypeChecked
    List<SmartPropertyListInfo> listProperties() {
        List<SmartPropertyListInfo> props = client.omniListProperties()
        return props
    }

    @Override
    public ConsensusSnapshot getConsensusSnapshot(CurrencyID currencyID) {
        /* Since omni_getallbalancesforid doesn't return the blockHeight, we have to check
         * blockHeight before and after the call to make sure it didn't change.
         */
        Integer beforeBlockHeight = currentBlockHeight()
        Integer curBlockHeight
        SortedMap<Address, BalanceEntry> entries
        while (true) {
            entries = this.getConsensusForCurrency(currencyID)
            curBlockHeight = currentBlockHeight()
            if (curBlockHeight == beforeBlockHeight) {
                // If blockHeight didn't change, we're done
                break;
            }
            // Otherwise we have to try again
            beforeBlockHeight = curBlockHeight
        }
        def snap = new ConsensusSnapshot(currencyID, curBlockHeight, "Omni Core", client.serverURI, entries);
        return snap
    }

}
