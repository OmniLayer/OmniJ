package foundation.omni.test.rpc.bitcore

import org.bitcoinj.base.Coin
import org.bitcoinj.base.Sha256Hash
import org.consensusj.bitcoin.json.pojo.bitcore.AddressBalanceInfo
import foundation.omni.BaseRegTestSpec
import spock.lang.Requires

/**
 * Test of OmniCore Bitcore address index JSON-RPC method: {@code getaddressbalance}
 * If {@code help} reports address index is not available, these tests are ignored.
 */
class GetAddressBalanceSpec extends BaseRegTestSpec {

    @Requires({ instance.isAddressIndexEnabled()})
    def "get 1 address balance"() {
        given:
        def address = client.getNewAddress()
        client.generateToAddress(1, address)
        Coin expectedBalance = getBlockReward()


        when:
        AddressBalanceInfo balanceInfo = client.getAddressBalance(address)

        then:
        balanceInfo != null
        balanceInfo.balance == expectedBalance
        balanceInfo.received == expectedBalance
        balanceInfo.immature == expectedBalance

    }

    @Requires({ instance.isAddressIndexEnabled()})
    def "get multi-address balance"() {
        given:
        def address1 = client.getNewAddress()
        def address2 = client.getNewAddress()
        client.generateToAddress(1, address1)
        Coin expectedBalance1 = getBlockReward()
        client.generateToAddress(1, address2)
        Coin expectedBalance = expectedBalance1 + getBlockReward()

        when:
        AddressBalanceInfo balanceInfo = client.getAddressBalance([address1, address2])

        then:
        balanceInfo != null
        balanceInfo.balance == expectedBalance
        balanceInfo.received == expectedBalance
        balanceInfo.immature == expectedBalance
    }

    // Get the block reward for the current best block by finding the coinbase transaction output value
    private Coin getBlockReward() {
        Sha256Hash hash = client.getBlockChainInfo().bestBlockHash
        return client.getBlock(hash).transactions[0].outputs[0].value
    }
}
