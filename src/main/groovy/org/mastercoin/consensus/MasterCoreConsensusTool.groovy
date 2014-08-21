package org.mastercoin.consensus

import com.msgilligan.bitcoin.rpc.RPCURL
import org.mastercoin.CurrencyID
import org.mastercoin.rpc.MPBalanceEntry
import org.mastercoin.rpc.MastercoinClient

/**
 * Command-line tool and class for fetching Master Core consensus data
 */
class MasterCoreConsensusTool extends ConsensusTool {
    static def rpcuser = "bitcoinrpc"
    static def rpcpassword = "pass"
    protected MastercoinClient client

    MasterCoreConsensusTool(MastercoinClient client)
    {
        this.client = client
    }

    public static void main(String[] args) {
        MastercoinClient client = new MastercoinClient(RPCURL.defaultMainNetURL, rpcuser, rpcpassword)
        MasterCoreConsensusTool tool = new MasterCoreConsensusTool(client)
        tool.run(args.toList())
    }

    private SortedMap<String, ConsensusEntry> getConsensusForCurrency(CurrencyID currencyID) {
        List<MPBalanceEntry> balances = client.getallbalancesforid_MP(currencyID)

        TreeMap<String, ConsensusEntry> map = [:]

        balances.each { MPBalanceEntry item ->

            String address = item.address
            ConsensusEntry entry = itemToEntry(item)

            if (address != "" && entry.balance > 0) {
                map.put(address, entry)
            }
        }
        return map;
    }

    private ConsensusEntry itemToEntry(MPBalanceEntry item) {
        BigDecimal reserved = item.reservedByOffer + (item.reservedByAccept ?: 0)
        return new ConsensusEntry(balance: item.balance, reserved:reserved)
    }

    public ConsensusSnapshot getConsensusSnapshot(CurrencyID currencyID) {
        def snap = new ConsensusSnapshot();
        snap.currencyID = currencyID
        snap.sourceType = "Master Core"
        snap.sourceURL = client.serverURL

        /* Since getallbalancesforid_MP doesn't return the blockHeight, we have to check
         * blockHeight before and after the call to make sure it didn't change.
         */
        Integer beforeBlockHeight = client.blockCount
        while (true) {
            snap.entries = this.getConsensusForCurrency(currencyID)
            snap.blockHeight = client.blockCount
            if (snap.blockHeight == beforeBlockHeight) {
                // If blockHeight didn't change, we're done
                break;
            }
            // Otherwise we have to try again
            beforeBlockHeight = snap.blockHeight
        }
        return snap
    }

}
