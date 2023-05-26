package foundation.omni.test.rpc.basic

import foundation.omni.BaseRegTestSpec
import foundation.omni.CurrencyID
import foundation.omni.BalanceEntry
import org.bitcoinj.base.Address
import spock.lang.Shared

import static foundation.omni.CurrencyID.OMNI
import static foundation.omni.CurrencyID.TOMNI


/**
 * Fund an address and check its balances in three different ways
 */
class GetBalancesSpec extends BaseRegTestSpec {
    final static faucetBTC = 10.btc
    final static faucetOMNI = 1000.divisible

    @Shared
    Address fundedAddress

    def setupSpec() {
        fundedAddress = createFundedAddress(faucetBTC, faucetOMNI)
    }

    def "omniGetBalance returns correct balances"() {
        when:
        def mscBalance = omniGetBalance(fundedAddress, OMNI)
        def tmscBalance = omniGetBalance(fundedAddress, TOMNI)

        then:
        mscBalance.balance.equals(faucetOMNI)
        mscBalance.reserved.equals(0.divisible)
        tmscBalance.balance.equals(faucetOMNI)
        tmscBalance.reserved.equals(0.divisible)
    }

    def "omniGetAllBalancesForId returns correct balances"() {
        when:
        SortedMap<Address, BalanceEntry> mscBalances = omniGetAllBalancesForId(OMNI)
        SortedMap<Address, BalanceEntry> tmscBalances = omniGetAllBalancesForId(TOMNI)

        then:
        mscBalances[fundedAddress].balance.equals(faucetOMNI)
        mscBalances[fundedAddress].reserved.equals(0.divisible)
        tmscBalances[fundedAddress].balance.equals(faucetOMNI)
        tmscBalances[fundedAddress].reserved.equals(0.divisible)
    }

    def "omniGetAllBalancesForAddress returns correct balances"() {
        when:
        SortedMap<CurrencyID, BalanceEntry> balances = omniGetAllBalancesForAddress(fundedAddress)

        then:
        balances[OMNI].balance.equals(faucetOMNI)
        balances[OMNI].reserved.equals(0.divisible)
        balances[TOMNI].balance.equals(faucetOMNI)
        balances[TOMNI].reserved.equals(0.divisible)
    }
}