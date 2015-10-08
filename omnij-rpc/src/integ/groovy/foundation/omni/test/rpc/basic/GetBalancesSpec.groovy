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
        mscBalance.balance == faucetMSC.numberValue()
        mscBalance.reserved == 0.0
        tmscBalance.balance == faucetMSC.numberValue()
        tmscBalance.reserved == 0.0
    }

    def "omniGetAllBalancesForId returns correct balances"() {
        when:
        def mscBalances = omniGetAllBalancesForId(MSC)
        def tmscBalances = omniGetAllBalancesForId(TMSC)

        then:
        mscBalances[fundedAddress].balance == faucetMSC.numberValue()
        mscBalances[fundedAddress].reserved == 0.0
        tmscBalances[fundedAddress].balance == faucetMSC.numberValue()
        tmscBalances[fundedAddress].reserved == 0.0
    }

    def "omniGetAllBalancesForAddress returns correct balances"() {
        when:
        def balances = omniGetAllBalancesForAddress(fundedAddress)

        then:
        balances[MSC].balance == faucetMSC.numberValue()
        balances[MSC].reserved == 0.0
        balances[TMSC].balance == faucetMSC.numberValue()
        balances[TMSC].reserved == 0.0
    }
}