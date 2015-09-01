package foundation.omni.consensus

import com.msgilligan.bitcoinj.rpc.RPCURI
import foundation.omni.CurrencyID
import foundation.omni.rpc.MPBalanceEntry
import foundation.omni.rpc.OmniClient
import foundation.omni.rpc.SmartPropertyListInfo
import foundation.omni.rpc.test.TestServers
import groovy.transform.TypeChecked
import org.bitcoinj.core.Address

/**
 * Command-line tool and class for fetching Omni Core consensus data
 */
class OmniCoreConsensusTool extends ConsensusTool {
    protected OmniClient client

    /**
     * URI Constructor
     *
     * @param coreURI URI to connect to - user/pass if required, must be encoded in URL
     */
    OmniCoreConsensusTool(URI coreURI)
    {
        String user = ""
        String pass = ""
        String userInfo = coreURI.getUserInfo()
        if (userInfo != null) {
            String[] userpass = userInfo.split(':')
            user = userpass[0]
            pass = userpass[1]
        }
        OmniClient client = new OmniClient(coreURI, user, pass)
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

    @Override
    URI getServerURI() {
        return this.client.getServerURI()
    }

    public static void main(String[] args) {
        OmniClient client = new OmniClient(RPCURI.defaultMainNetURI, TestServers.rpcTestUser, TestServers.rpcTestPassword)
        OmniCoreConsensusTool tool = new OmniCoreConsensusTool(client)
        tool.run(args.toList())
    }

    private SortedMap<Address, ConsensusEntry> getConsensusForCurrency(CurrencyID currencyID) {
        List<MPBalanceEntry> balances = client.omniGetAllBalancesForId(currencyID)

        TreeMap<Address, ConsensusEntry> map = [:]

        balances.each { MPBalanceEntry item ->

            Address address = item.address
            ConsensusEntry entry = itemToEntry(item)

            if (address != "" && entry.balance > 0) {
                map.put(address, entry)
            }
        }
        return map;
    }

    private ConsensusEntry itemToEntry(MPBalanceEntry item) {
        return new ConsensusEntry(balance: item.balance, reserved:item.reserved)
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
        SortedMap<Address, ConsensusEntry> entries
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
