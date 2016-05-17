package foundation.omni.test.rpc.basic

import foundation.omni.BaseRegTestSpec
import org.bitcoinj.core.Address
import spock.lang.Shared

import static foundation.omni.CurrencyID.OMNI
import static foundation.omni.CurrencyID.TOMNI


/**
 * Fund an address and check its balances in three different ways
 */
class GetBalancesSpec extends BaseRegTestSpec {
    final static faucetBTC = 10.btc
    final static faucetMSC = 1000.divisible

    @Shared
    Address fundedAddress

    def setupSpec() {
        fundedAddress = createFundedAddress(faucetBTC, faucetMSC)
    }

    def "omniGetBalance returns correct balances"() {
        when:
        def mscBalance = omniGetBalance(fundedAddress, OMNI)
        def tmscBalance = omniGetBalance(fundedAddress, TOMNI)

        then:
        mscBalance.balance.equals(faucetMSC)
        mscBalance.reserved.equals(0.divisible)
        tmscBalance.balance.equals(faucetMSC)
        tmscBalance.reserved.equals(0.divisible)
    }

    def "omniGetAllBalancesForId returns correct balances"() {
        when:
        def mscBalances = omniGetAllBalancesForId(OMNI)
        def tmscBalances = omniGetAllBalancesForId(TOMNI)

        then:
        mscBalances[fundedAddress].balance.equals(faucetMSC)
        mscBalances[fundedAddress].reserved.equals(0.divisible)
        tmscBalances[fundedAddress].balance.equals(faucetMSC)
        tmscBalances[fundedAddress].reserved.equals(0.divisible)
    }

    def "omniGetAllBalancesForAddress returns correct balances"() {
        when:
        def balances = omniGetAllBalancesForAddress(fundedAddress)

        then:
        balances[OMNI].balance.equals(faucetMSC)
        balances[OMNI].reserved.equals(0.divisible)
        balances[TOMNI].balance.equals(faucetMSC)
        balances[TOMNI].reserved.equals(0.divisible)
    }
}