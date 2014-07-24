package org.mastercoin.consensus

import org.mastercoin.CurrencyID
import org.mastercoin.rpc.MastercoinClient

/**
 * User: sean
 * Date: 7/3/14
 * Time: 11:45 AM
 */
class MasterCoreConsensusTool extends ConsensusTool {
    static def rpcproto = "http"
    static def rpchost = "127.0.0.1"
    static def rpcport = 8332
    static def rpcfile = "/"
    static def rpcuser = "bitcoinrpc"
    static def rpcpassword = "pass"
    protected MastercoinClient client
    private URL rpcServerURL

    MasterCoreConsensusTool() {
        rpcServerURL = new URL(rpcproto, rpchost, rpcport, rpcfile)
        client = new MastercoinClient(rpcServerURL, rpcuser, rpcpassword)
    }

    public static void main(String[] args) {
        MasterCoreConsensusTool tool = new MasterCoreConsensusTool()
        tool.run(args.toList())
    }

    private SortedMap<String, ConsensusEntry> getConsensusForCurrency(CurrencyID currencyID) {
        List<Object> balances = client.getallbalancesforid_MP(currencyID)

        TreeMap<String, ConsensusEntry> map = [:]

        balances.each { Object item ->

            String address = item.address
            ConsensusEntry entry = itemToEntry(item)

            if (address != "" && entry.balance > 0) {
                map.put(address, entry)
            }
        }
        return map;
    }

    private ConsensusEntry itemToEntry(Object item) {
        BigDecimal balance = jsonToBigDecimal(item.balance)
        BigDecimal reservedByOffer = jsonToBigDecimal(item.reservedbyoffer)
        BigDecimal reservedByAccept = item.reservedbyaccept ? jsonToBigDecimal(item.reservedbyaccept) : new BigDecimal("0")
        BigDecimal reserved = reservedByOffer + reservedByAccept
        return new ConsensusEntry(balance: balance, reserved:reserved)
    }

    /* We're expecting input type Double here */
    private BigDecimal jsonToBigDecimal(Object balanceIn) {
        BigDecimal balanceOut = new BigDecimal(Double.toString(balanceIn)).setScale(12)
        return balanceOut
    }

    public ConsensusSnapshot getConsensusSnapshot(CurrencyID currencyID) {
        def snap = new ConsensusSnapshot();
        snap.currencyID = currencyID
        snap.blockHeight = client.getBlockCount()
        snap.sourceType = "Master Core"
        snap.sourceURL = rpcServerURL
        snap.entries = this.getConsensusForCurrency(currencyID)
        return snap
    }

}
