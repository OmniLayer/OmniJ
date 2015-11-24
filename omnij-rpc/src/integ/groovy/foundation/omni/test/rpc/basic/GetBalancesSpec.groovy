package foundation.omni.test.rpc.basic

import foundation.omni.BaseRegTestSpec
import foundation.omni.CurrencyID
import foundation.omni.rpc.BalanceEntry
import org.bitcoinj.core.Address
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

import static foundation.omni.CurrencyID.MSC
import static foundation.omni.CurrencyID.TMSC


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
        def mscBalance = omniGetBalance(fundedAddress, MSC)
        def tmscBalance = omniGetBalance(fundedAddress, TMSC)

        then:
        mscBalance.balance.equals(faucetMSC)
        mscBalance.reserved.equals(0.divisible)
        tmscBalance.balance.equals(faucetMSC)
        tmscBalance.reserved.equals(0.divisible)
    }

    def "omniGetAllBalancesForId returns correct balances"() {
        when:
        def mscBalances = omniGetAllBalancesForId(MSC)
        def tmscBalances = omniGetAllBalancesForId(TMSC)

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
        balances[MSC].balance.equals(faucetMSC)
        balances[MSC].reserved.equals(0.divisible)
        balances[TMSC].balance.equals(faucetMSC)
        balances[TMSC].reserved.equals(0.divisible)
    }
}