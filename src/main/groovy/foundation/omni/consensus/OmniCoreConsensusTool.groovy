package foundation.omni.consensus

import com.msgilligan.bitcoin.rpc.RPCURL
import foundation.omni.CurrencyID
import foundation.omni.rpc.MPBalanceEntry
import foundation.omni.rpc.OmniClient
import org.bitcoinj.core.Address

/**
 * Command-line tool and class for fetching Omni Core consensus data
 */
class OmniCoreConsensusTool extends ConsensusTool {
    static def rpcuser = "bitcoinrpc"
    static def rpcpassword = "pass"
    protected OmniClient client

    OmniCoreConsensusTool(OmniClient client)
    {
        this.client = client
    }

    public static void main(String[] args) {
        OmniClient client = new OmniClient(RPCURL.defaultMainNetURL, rpcuser, rpcpassword)
        OmniCoreConsensusTool tool = new OmniCoreConsensusTool(client)
        tool.run(args.toList())
    }

    private SortedMap<Address, ConsensusEntry> getConsensusForCurrency(CurrencyID currencyID) {
        List<MPBalanceEntry> balances = client.getallbalancesforid_MP(currencyID)

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
    public ConsensusSnapshot getConsensusSnapshot(CurrencyID currencyID) {
        /* Since getallbalancesforid_MP doesn't return the blockHeight, we have to check
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
