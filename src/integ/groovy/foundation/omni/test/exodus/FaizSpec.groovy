package foundation.omni.test.exodus

import foundation.omni.BaseRegTestSpec
import foundation.omni.OmniRegTestParams
import foundation.omni.consensus.ConsensusComparison
import foundation.omni.consensus.ConsensusEntry
import foundation.omni.consensus.ConsensusTool
import foundation.omni.consensus.OmniCoreConsensusTool
import spock.lang.Shared
import spock.lang.Unroll

import static foundation.omni.CurrencyID.*

/**
 * User: sean
 * Date: 7/26/14
 * Time: 7:00 PM
 */
class FaizSpec extends BaseRegTestSpec {
    final static BigDecimal sendAmount = 0.5
    final static BigDecimal extraAmount = 0.1

    @Shared
    ConsensusTool consensusFetcher

    @Shared
    ConsensusComparison comparison

    def setupSpec() {
        consensusFetcher = new OmniCoreConsensusTool(client)
    }

    def "Faiz's test"() {
        setup: "Create a new, unique address in a dedicated account"
        def accountname = "msc"
        def accountAddress = getAccountAddress(accountname)

        when: "we create a new account for Mastercoins and send some BTC to it"
        sendToAddress(accountAddress, 2*sendAmount + extraAmount)
        generateBlock()

        then: "we have the correct amount of BTC there"
        getBalance(accountname) >= 2*sendAmount + extraAmount

        when: "We send the BTC to the moneyManAddress and generate a block"
        def amounts = [(OmniRegTestParams.MoneyManAddress): sendAmount, (OmniRegTestParams.ExodusAddress): sendAmount]
        def txid = sendMany(accountname, amounts)
        generateBlock()
        def tx = getTransaction(txid)

        then: "transaction was confirmed"
        tx.confirmations == 1

        and: "The balances for the account we just sent MSC to is correct"
        getbalance_MP(accountAddress, MSC) == 100 * sendAmount
        getbalance_MP(accountAddress, TMSC) == 100 * sendAmount
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
}