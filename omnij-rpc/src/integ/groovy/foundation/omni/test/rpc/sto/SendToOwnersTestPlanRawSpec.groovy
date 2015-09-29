package foundation.omni.test.rpc.sto

//import com.msgilligan.bitcoinj.rpc.conversion.BitcoinMath
import foundation.omni.CurrencyID
import foundation.omni.OmniValue
import org.bitcoinj.core.Address

/**
 * Data driven tests for the "send to owners" transaction type, whereby a raw transaction
 * is created and broadcasted to bypass the RPC interface
 */
class SendToOwnersTestPlanRawSpec extends SendToOwnersTestPlanSpec {

    /**
     * Executes the "send to owner" command by creating a raw transaction to bypass the RPC interface.
     */
    @Override
    def executeSendToOwners(Address address, CurrencyID currency, OmniValue amount, Boolean exceptional=false) {
        def rawTxHex = createSendToOwnersHex(currency, amount);
        def txid = omniSendRawTx(address, rawTxHex)
        return txid
    }
}
