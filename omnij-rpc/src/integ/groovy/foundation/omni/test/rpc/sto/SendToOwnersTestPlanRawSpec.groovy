package foundation.omni.test.rpc.sto

import foundation.omni.CurrencyID
import org.bitcoinj.core.Address
import com.msgilligan.bitcoinj.BTC
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
    def executeSendToOwners(Address address, CurrencyID currency, def propertyType, BigDecimal amount, def exceptional=false) {
        BigDecimal numberOfTokens = amount

        if (propertyType == PropertyType.DIVISIBLE) {
            numberOfTokens = BTC.btcToSatoshis(numberOfTokens)
        }

        def rawTxHex = createSendToOwnersHex(currency, numberOfTokens.longValue());
        def txid = omniSendRawTx(address, rawTxHex)
        return txid
    }

}
