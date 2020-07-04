package foundation.omni.test.rpc.reorgs

import foundation.omni.OmniDivisibleValue
import org.bitcoinj.core.Coin
import org.consensusj.jsonrpc.JsonRpcException
import foundation.omni.BaseRegTestSpec
import org.bitcoinj.core.Sha256Hash
import org.junit.internal.AssumptionViolatedException

abstract class BaseReorgSpec extends BaseRegTestSpec {

    static protected Coin startBTC = 0.1.btc
    static protected OmniDivisibleValue startOMNI = 0.2.divisible

    def setupSpec() {
        try {
            clearMemPool()
        } catch(JsonRpcException ignored) {
            throw new AssumptionViolatedException('The client has no "clearmempool" command')
        }
    }

    Sha256Hash generateAndGetBlockHash() throws AssumptionViolatedException
    {
        List<Sha256Hash> result = generateBlocks(1)
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
