package foundation.omni.test.rpc.basic

import com.msgilligan.bitcoin.rpc.JsonRPCStatusException
import foundation.omni.BaseRegTestSpec
import foundation.omni.CurrencyID
import spock.lang.Unroll

import static foundation.omni.CurrencyID.*

class SimpleSendSpec extends BaseRegTestSpec {
    // Need to make sure these variables are set up with values that match their names
    static final CurrencyID nonExistentCurrencyID = new CurrencyID(293487L)
    def addressWith1MSC = getNewAddress() // Get an address with a balance of 1 MSC
    def richAddress = getNewAddress()  // Should be an address we know has a > 0 balance,
                                              //      ... otherwise it will fail
                                              // We need to seed the address with coins
    final static BigDecimal faucetBTC = 10.0
    final static BigDecimal faucetMSC = 1000.0

    @Unroll
    def "Can simple send #amount MSC from one address to another"() {
        setup:
        def faucetAddress = createFundedAddress(faucetBTC, fundingMSC)

        when: "we send MSC"
        def startBalance = getbalance_MP(faucetAddress, MSC).balance
        def toAddress = getNewAddress()
        send_MP(faucetAddress, toAddress, MSC, amount)

        and: "a block is generated"
        generateBlock()
        def endBalance = getbalance_MP(faucetAddress, MSC).balance

        then: "the toAddress has the correct MSC balance and source address is reduced by right amount"
        amount == getbalance_MP(toAddress, MSC).balance
        endBalance == startBalance - amount

        where:
        fundingMSC | amount
        1000.0     | 1.0
        1.0        | 0.12345678
        0.000001   | 0.00000001
    }

    def "When the amount to transfer is zero Simple Sends are rejected by the RPC"() {
        // Note: We also need to submit via P2P and confirm these same invalid tx'es and make sure they are
        // treated as invalid by the Master Core parser
        given: "a new, empty destination address"
        def fundedAddress = createFundedAddress(faucetBTC, faucetMSC)
        def toAddress = getNewAddress()

        when: "the amount to transfer is zero"
        send_MP(fundedAddress, toAddress, MSC, 0)
        // TODO: Test sending a negative amount of coins?
        // TODO: Check that the *right type* of exceptions are thrown
        // Currently it seems they're all 500s

        then: "exception is thrown"
        JsonRPCStatusException e = thrown()
        e.message == "Invalid amount"
        e.responseJson.error.code == -3
        // TODO: Verify that blockchain state didn't change
    }

    def "When the amount to transfer is negative, Simple Sends are rejected by the RPC"() {
        // Note: We also need to submit via P2P and confirm these same invalid tx'es and make sure they are
        // treated as invalid by the Master Core parser

        given: "a new, empty destination address"
        def fundedAddress = createFundedAddress(faucetBTC, faucetMSC)
        def toAddress = getNewAddress()

        when: "the amount to transfer is zero"
        send_MP(fundedAddress, toAddress, MSC, -1.0)
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
        // treated as invalid by the Master Core parser

        given: "an empty source address and a new, empty destination address"
        def emptyAddress = getNewAddress()
        def toAddress = getNewAddress()

        when: "the sending address has zero coins in its available balance for the specified currency identifier"
        def txid = client.send_MP(emptyAddress, toAddress, MSC, 1.0)

        then: "exception is thrown"
        JsonRPCStatusException e = thrown()
        e.message == "Not enough funds in user address"
        e.responseJson.error.code == -1
    }

    def "When the amount to transfer exceeds the number owned and available by the sending address are rejected by the RPC"() {
        // Note: We also need to submit via P2P and confirm these same invalid tx'es and make sure they are
        // treated as invalid by the Master Core parser

        given: "a new, empty destination address"
        def fundedAddress = createFundedAddress(faucetBTC, 1.0)
        def toAddress = getNewAddress()

        when: "the amount to transfer exceeds the number owned and available by the sending address"
        send_MP(fundedAddress, toAddress, MSC, 1.00000001)

        then: "exception is thrown"
        JsonRPCStatusException e = thrown()
        e.message == "Not enough funds in user address"
        e.responseJson.error.code == -1
    }

    def "When the specified currency identifier is non-existent are rejected by the RPC"() {
        // Note: We also need to submit via P2P and confirm these same invalid tx'es and make sure they are
        // treated as invalid by the Master Core parser

        given: "a new, empty destination address"
        def fundedAddress = createFundedAddress(faucetBTC, 1.0)
        def toAddress = getNewAddress()

        when: "the specified currency identifier is non-existent"
        send_MP(richAddress, toAddress, nonExistentCurrencyID, 1.0)

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
