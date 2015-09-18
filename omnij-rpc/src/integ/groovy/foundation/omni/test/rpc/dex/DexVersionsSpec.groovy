package foundation.omni.test.rpc.dex

import com.msgilligan.bitcoinj.rpc.JsonRPCStatusException
import foundation.omni.BaseRegTestSpec

/**
 * Transaction format tests for traditional DEx offer
 *
 * Traditional DEx orders with version 0 have an implicit action value,
 * based on the global state, whereby transactions with version 1 have an
 * explicit action value. Other versions are currently not valid.
 */
class DexVersionsSpec extends BaseRegTestSpec {

    def "DEx transactions with version 0 have no explicit action value"() {
        /*
            {
                "version": 0,
                "type_int": 20,
                "propertyid": 1,
                "amount": "0.40000000",currently
                "bitcoindesired": "0.80000000",
                "timelimit": 15,
                "feerequired": "0.00000100"
            }
        */
        def rawTx = "00000014000000010000000002625a000000000004c4b4000f0000000000000064"
        def actorAddress = createFundedAddress(0.001.btc, 0.5.divisible)

        when:
        def txid = omniSendRawTx(actorAddress, rawTx)
        generateBlock()

        and:
        def offerTx = omniGetTransaction(txid)

        then:
        offerTx.action == "new" // implicit
        offerTx.valid
    }

    def "DEx transactions with version 1 have an explicit action value"() {
        /*
            {
                "version": 1,
                "type_int": 20,
                "propertyid": 2,
                "amount": "0.10000000",
                "bitcoindesired": "0.10000000",
                "timelimit": 20,
                "feerequired": "0.00000000",
                "action": 1
            }
        */
        def rawTx = "00010014000000020000000000989680000000000098968014000000000000000001"
        def actorAddress = createFundedAddress(0.001.btc, 0.3.divisible)

        when:
        def txid = omniSendRawTx(actorAddress, rawTx)
        generateBlock()

        and:
        def offerTx = omniGetTransaction(txid)

        then:
        offerTx.action == "new" // explicit
        offerTx.valid
    }

    def "DEx transactions with version 1 must be sent with action value"() {
        /*
            {
                "version": 1,
                "type_int": 20,
                "propertyid": 1,
                "amount": "0.30000000",
                "bitcoindesired": "0.30000000",
                "timelimit": 7,
                "feerequired": "0.00000050"
            }
        */
        def rawTx = "00010014000000010000000001c9c3800000000001c9c380070000000000000032"
        def actorAddress = createFundedAddress(0.001.btc, 0.3.divisible)

        when:
        def txid = omniSendRawTx(actorAddress, rawTx)
        generateBlock()

        and:
        omniGetTransaction(txid)

        then:
        thrown(JsonRPCStatusException) // No Omni transaction
    }

    def "DEx transactions with versions greater than 1 are currently not valid"() {
        /*
            {
                "version": 2,
                "type_int": 20,
                "propertyid": 2,
                "amount": "0.00000001",
                "bitcoindesired": "0.00000001",
                "timelimit": 255,
                "feerequired": "0.00000001",
                "action": 1
            }
        */
        def rawTx = "000200140000000100000000000000010000000000000001ff000000000000000101"
        def actorAddress = createFundedAddress(0.001.btc, 0.001.divisible)

        when:
        def txid = omniSendRawTx(actorAddress, rawTx)
        generateBlock()

        and:
        def offerTx = omniGetTransaction(txid)

        then:
        !offerTx.valid
    }
}
