package foundation.omni.test.rpc.reorgs

import com.msgilligan.jsonrpc.JsonRPCException
import foundation.omni.BaseRegTestSpec
import org.bitcoinj.core.Sha256Hash
import org.junit.internal.AssumptionViolatedException

abstract class BaseReorgSpec extends BaseRegTestSpec {

    static protected BigDecimal startBTC = 0.1
    static protected BigDecimal startMSC = 0.2

    def setupSpec() {
        try {
            clearMemPool()
        } catch(JsonRPCException ignored) {
            throw new AssumptionViolatedException('The client has no "clearmempool" command')
        }
    }

    Sha256Hash generateAndGetBlockHash() throws AssumptionViolatedException
    {
        List<Sha256Hash> result = generate()
        if (result == null) {
            throw new AssumptionViolatedException('The client is not based on Bitcoin Core 0.10')
        }
        assert result.size() > 0
        return result[0]
    }

    Boolean checkTransactionValidity(Sha256Hash txid, Integer confirmationsLimit = 1)
    {
        try {
            def transaction = getRawTransactionInfo(txid)
            if (transaction.confirmations < confirmationsLimit) {
                return false
            }
        } catch(Exception ignored) {
            return false
        }
        try {
            def transaction = omniGetTransaction(txid)
            if (transaction.valid != true) {
                return false
            }
            if (transaction.confirmations < confirmationsLimit) {
                return false
            }
        } catch(Exception ignored) {
            return false
        }

        return true
    }

}
