package org.mastercoin.consensus

import com.msgilligan.bitcoin.rpc.RPCURL
import org.mastercoin.CurrencyID
import org.mastercoin.rpc.MPBalanceEntry
import org.mastercoin.rpc.MastercoinClient

/**
 * User: sean
 * Date: 7/3/14
 * Time: 11:45 AM
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
//        BigDecimal balance = jsonToBigDecimal(item.balance)
//        BigDecimal reservedByOffer = jsonToBigDecimal(item.reservedbyoffer)
//        BigDecimal reservedByAccept = item.reservedbyaccept ? jsonToBigDecimal(item.reservedbyaccept) : new BigDecimal("0")
        BigDecimal reserved = item.reservedByOffer + item.reservedByAccept
        return new ConsensusEntry(balance: item.balance, reserved:reserved)
    }

    /* We're expecting input type Double here */
    private BigDecimal jsonToBigDecimal(Object balanceIn) {
        BigDecimal balanceOut = new BigDecimal(client.jsonDecimalFormat.parse(balanceIn)).setScale(12)
        return balanceOut
    }

    public ConsensusSnapshot getConsensusSnapshot(CurrencyID currencyID) {
        def snap = new ConsensusSnapshot();
        snap.currencyID = currencyID
        snap.blockHeight = client.blockCount
        snap.sourceType = "Master Core"
        snap.sourceURL = client.serverURL
        snap.entries = this.getConsensusForCurrency(currencyID)
        return snap
    }

}
