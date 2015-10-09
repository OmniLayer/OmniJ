package foundation.omni.test.rpc.basic

import com.msgilligan.bitcoinj.rpc.JsonRPCStatusException
import foundation.omni.BaseRegTestSpec
import foundation.omni.CurrencyID
import spock.lang.Ignore
import spock.lang.Unroll

import static foundation.omni.CurrencyID.*

class SimpleSendSpec extends BaseRegTestSpec {
    // Need to make sure these variables are set up with values that match their names
    static final CurrencyID nonExistentCurrencyID = new CurrencyID(293487L)
    def richAddress = getNewAddress()  // Should be an address we know has a > 0 balance,
                                              //      ... otherwise it will fail
                                              // We need to seed the address with coins
    final static faucetBTC = 10.btc
    final static faucetMSC = 1000.divisible

    @Unroll
    def "Can simple send #amount MSC from one address to another"() {
        setup:
        def faucetAddress = createFundedAddress(faucetBTC, faucetMSC)

        when: "we send MSC"
        def startBalance = omniGetBalance(faucetAddress, MSC).balance
        def toAddress = getNewAddress()
        omniSend(faucetAddress, toAddress, MSC, amount)

        and: "a block is generated"
        generateBlock()
        def endBalance = omniGetBalance(faucetAddress, MSC).balance

        then: "the toAddress has the correct MSC balance and source address is reduced by right amount"
        amount.numberValue() == omniGetBalance(toAddress, MSC).balance
        endBalance == startBalance - amount.numberValue()

        where:
        fundingMSC | amount
        1000.0     | 1.divisible
        1.0        | 0.12345678.divisible
        0.000001   | 0.00000001.divisible
    }

    def "When the amount to transfer is zero Simple Sends are rejected by the RPC"() {
        // Note: We also need to submit via P2P and confirm these same invalid tx'es and make sure they are
        // treated as invalid by the Omni Core parser
        given: "a new, empty destination address"
        def fundedAddress = createFundedAddress(faucetBTC, faucetMSC)
        def toAddress = getNewAddress()

        when: "the amount to transfer is zero"
        omniSend(fundedAddress, toAddress, MSC, 0.divisible)
        // TODO: Test sending a negative amount of coins?
        // TODO: Check that the *right type* of exceptions are thrown
        // Currently it seems they're all 500s

        then: "exception is thrown"
        JsonRPCStatusException e = thrown()
        e.message == "Invalid amount"
        e.responseJson.error.code == -3
        // TODO: Verify that blockchain state didn't change
    }

    @Ignore("Can't have negative OmniValues -- need to rewrite this test using a lower-level send")
    def "When the amount to transfer is negative, Simple Sends are rejected by the RPC"() {
        // Note: We also need to submit via P2P and confirm these same invalid tx'es and make sure they are
        // treated as invalid by the Omni Core parser

        given: "a new, empty destination address"
        def fundedAddress = createFundedAddress(faucetBTC, faucetMSC)
        def toAddress = getNewAddress()

        when: "the amount to transfer is zero"
        omniSend(fundedAddress, toAddress, MSC, (-1.0).divisible)
        // TODO: Test sending a negative amount of coins?
        // TODO: Check that the *right type* of exceptions are thrown
        // Currently it seems they're all 500s

        then: "exception is thrown"
        JsonRPCStatusException e = thrown()
        e.message == "Invalid amount"
        e.responseJson.error.code == -3
        // TODO: Verify that blockchain state didn't change
    }

    def "When the sending address has zero coins in its available balance for the specified currency identifier are rejected by the RPC"() {
        // Note: We also need to submit via P2P and confirm these same invalid tx'es and make sure they are
        // treated as invalid by the Omni Core parser

        given: "an empty source address and a new, empty destination address"
        def emptyAddress = getNewAddress()
        def toAddress = getNewAddress()

        when: "the sending address has zero coins in its available balance for the specified currency identifier"
        def txid = client.omniSend(emptyAddress, toAddress, MSC, 1.divisible)

        then: "exception is thrown"
        JsonRPCStatusException e = thrown()
    }

    def "When the amount to transfer exceeds the number owned and available by the sending address are rejected by the RPC"() {
        // Note: We also need to submit via P2P and confirm these same invalid tx'es and make sure they are
        // treated as invalid by the Omni Core parser

        given: "a new, empty destination address"
        def fundedAddress = createFundedAddress(faucetBTC, 1.divisible)
        def toAddress = getNewAddress()

        when: "the amount to transfer exceeds the number owned and available by the sending address"
        omniSend(fundedAddress, toAddress, MSC, 1.00000001.divisible)

        then: "exception is thrown"
        JsonRPCStatusException e = thrown()
    }

    def "When the specified currency identifier is non-existent are rejected by the RPC"() {
        // Note: We also need to submit via P2P and confirm these same invalid tx'es and make sure they are
        // treated as invalid by the Omni Core parser

        given: "a new, empty destination address"
        def fundedAddress = createFundedAddress(faucetBTC, 1.divisible)
        def toAddress = getNewAddress()

        when: "the specified currency identifier is non-existent"
        omniSend(richAddress, toAddress, nonExistentCurrencyID, 1.divisible)

        then: "exception is thrown"
        JsonRPCStatusException e = thrown()
        e.message == "Property identifier does not exist"
        e.responseJson.error.code == -8
    }

//    def "TODO: test currencies other than MSC - when implemented"() {}
//    def "TODO: test savings address -- when implemented"() {}
//    def "TODO: test invalid transaction version -- raw"() {}
//    def "TODO: test invalid transaction type - raw"() {}
//    def "TODO: generate other invalid/edge transactions and submit via P2P and/or confirm in a bock -- raw"() {}
//    def "TODO: double spend -- raw"() {}

}
