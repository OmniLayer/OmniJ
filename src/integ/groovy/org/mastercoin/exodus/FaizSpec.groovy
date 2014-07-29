package org.mastercoin.exodus

import com.msgilligan.bitcoin.rpc.RPCURL
import org.mastercoin.BaseRegTestSpec
import org.mastercoin.CurrencyID
import org.mastercoin.MPNetworkParameters
import org.mastercoin.MPRegTestParams
import org.mastercoin.consensus.ConsensusComparison
import org.mastercoin.consensus.ConsensusEntry
import org.mastercoin.consensus.ConsensusSnapshot
import org.mastercoin.consensus.ConsensusTool
import org.mastercoin.consensus.MasterCoreConsensusTool
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Stepwise
import spock.lang.Unroll

import static org.mastercoin.CurrencyID.*

/**
 * User: sean
 * Date: 7/26/14
 * Time: 7:00 PM
 */
class FaizSpec extends BaseRegTestSpec {

    final static String moneyManAddress = "moneyqMan7uh8FqdCA2BV5yZ8qVrc9ikLP"
    final static String exodusAddress = "mpexoDuSkGGqvqrkrjiFng38QPkJQVFyqv"
    final static BigDecimal sendAmount = 0.5

    @Shared
    ConsensusTool consensusFetcher

    @Shared
    ConsensusComparison comparison

    def setupSpec() {
        consensusFetcher = new MasterCoreConsensusTool(client)
    }

    def "Faiz's test"() {
        when: "we create a new wallet for mastercoins and move some BTC into it"
        def accountname = "msc"
        def accountAddress = client.getAccountAddress(accountname)
        client.sendToAddress(accountAddress, 2*sendAmount + 0.1)
        client.generateBlock()

        then: "we have some BTC there"
        client.getBalance(accountname, null) >= 2*sendAmount + 0.1

        when: "We send some BTC to the moneyManAddress and generate a block"
        Map<String, BigDecimal> amounts = [(moneyManAddress): sendAmount, (exodusAddress): sendAmount]
        def txid1 = client.sendMany(accountname, amounts)
        client.generateBlock()
        def tx1 = client.getTransaction(txid1)

        then: "transaction was confirmed"
        tx1.confirmations == 1

        when: "we check balances"
        def balances = client.getallbalancesforid_MP(MSC)
        def mscBalance = balances.find{it.address == accountAddress}.balance

        then: "We get some addresses with MSC balances (at least test address + exodus)"
        balances.size() >= 2

        and: "The balance for the account we just sent MSC to is correct"
        mscBalance == 100 * sendAmount
    }

    @Unroll
    def "Test #address #entry.balance == #expected"(Map.Entry<String, ConsensusEntry> kv, String address, ConsensusEntry entry, BigDecimal expected) {
        expect:
        entry.balance == expected
        entry.reserved == 0

        where:
        kv << consensusFetcher.getConsensusSnapshot(MSC).getEntriesExcluding(exodusAddress)
        address = kv.key
        entry = kv.value
        expected = sendAmount * 100
    }

    @Ignore
    @Unroll
    def "#address #entry1 == #entry2"() {
        setup:
        TreeMap<String, ConsensusEntry> expected = [moneyManAddress:new ConsensusEntry(0,0), exodusAddress: new ConsensusEntry(0,0)]
        def expectedSnap = new ConsensusSnapshot()
        expectedSnap.entries = expected

        when:
        def actualSnap = consensusFetcher.getConsensusSnapshot(MSC)
        def comparison = new ConsensusComparison(expectedSnap, actualSnap)

        then:
        expectedSnap.entries == actualSnap.entries
    }
}