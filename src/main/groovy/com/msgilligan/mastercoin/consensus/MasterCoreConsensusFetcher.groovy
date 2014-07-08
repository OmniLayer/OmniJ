package com.msgilligan.mastercoin.consensus

import com.msgilligan.bitcoin.rpc.MastercoinClient

/**
 * User: sean
 * Date: 7/3/14
 * Time: 11:45 AM
 */
class MasterCoreConsensusFetcher implements ConsensusFetcher {
    static def rpcproto = "http"
    static def rpchost = "127.0.0.1"
    static def rpcport = 8332
    static def rpcfile = "/"
    static def rpcuser = "bitcoinrpc"
    static def rpcpassword = "pass"
    protected MastercoinClient client

    MasterCoreConsensusFetcher() {
        def rpcServerURL = new URL(rpcproto, rpchost, rpcport, rpcfile)
        client = new MastercoinClient(rpcServerURL, rpcuser, rpcpassword)
    }

    public static void main(String[] args) {
        MasterCoreConsensusFetcher mscFetcher
        Long currencyMSC = 1L

        mscFetcher = new MasterCoreConsensusFetcher()

        println "Block count is: " + mscFetcher.client.getBlockCount()

        def mscConsensus = mscFetcher.getConsensusForCurrency(currencyMSC)
        mscConsensus.each {  address, ConsensusBalance bal ->
            println "${address}: ${bal.balance}"
        }
    }

    @Override
    Map<String, ConsensusBalance> getConsensusForCurrency(Long currencyID) {
        List<Object> balances = client.getallbalancesforid_MP(currencyID)

        TreeMap<String, ConsensusBalance> map = [:]
        balances.each { Object item ->
            String address = item.address
            BigDecimal balance = new BigDecimal(Double.toString(item.balance)).setScale(8)
            BigDecimal reservedByOffer = new BigDecimal(Double.toString(item.reservedbyoffer)).setScale(8)
            BigDecimal reservedByAccept = new BigDecimal(Double.toString(item.reservedbyoffer)).setScale(8)
            BigDecimal reserved = reservedByOffer + reservedByAccept;

            if (address != "") {
                map.put(item.address, new ConsensusBalance(address: address, balance: balance, reserved:reserved))
            }
        }
        return map;
    }
}
