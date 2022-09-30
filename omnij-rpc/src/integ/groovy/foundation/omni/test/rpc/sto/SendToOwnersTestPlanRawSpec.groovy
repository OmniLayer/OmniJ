package foundation.omni.test.rpc.sto

//import org.consensusj.bitcoin.jsonrpc.conversion.BitcoinMath
import foundation.omni.CurrencyID
import foundation.omni.OmniValue
import foundation.omni.tx.RawTxBuilder
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
        RawTxBuilder builder = new RawTxBuilder()
        def rawTxHex = builder.createSendToOwnersHex(currency, amount);
        def txid = omniSendRawTx(address, rawTxHex)
        return txid
    }
}
