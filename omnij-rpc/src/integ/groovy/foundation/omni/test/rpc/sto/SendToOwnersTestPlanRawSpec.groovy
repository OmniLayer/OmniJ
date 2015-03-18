package foundation.omni.test.rpc.sto

import org.bitcoinj.core.Address
import com.msgilligan.bitcoin.BTC
import foundation.omni.PropertyType

/**
 * Data driven tests for the "send to owners" transaction type, whereby a raw transaction
 * is created and broadcasted to bypass the RPC interface
 */
class SendToOwnersTestPlanRawSpec extends SendToOwnersTestPlanSpec {

    /**
     * Executes the "send to owner" command by creating a raw transaction to bypass the RPC interface.
     */
    @Override
    def executeSendToOwners(Address address, def currency, def propertyType, def amount, def exceptional=false) {
        BigDecimal numberOfTokens = amount

        if (propertyType == PropertyType.DIVISIBLE) {
            numberOfTokens = BTC.btcToSatoshis(numberOfTokens)
        }

        def rawTxHex = createSendToOwnersHex(currency, numberOfTokens.longValue());
        def txid = sendrawtx_MP(address, rawTxHex)
        return txid
    }

}
