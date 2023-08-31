package foundation.omni.test.rpc.reorgs

import foundation.omni.OmniDivisibleValue
import org.bitcoinj.base.Coin
import org.consensusj.jsonrpc.JsonRpcException
import foundation.omni.BaseRegTestSpec
import org.bitcoinj.base.Sha256Hash
import org.junit.jupiter.api.Assumptions


abstract class BaseReorgSpec extends BaseRegTestSpec {

    static protected Coin startBTC = 0.1.btc
    static protected OmniDivisibleValue startOMNI = 0.2.divisible

    def setupSpec() {
        try {
            clearMemPool()
        } catch(JsonRpcException ignored) {
            Assumptions.abort('The client has no "clearmempool" command')
        }
    }

    Sha256Hash generateAndGetBlockHash()
    {
        List<Sha256Hash> result = generateBlocks(1)
        assert result.size() > 0
        return result[0]
    }

    Boolean checkTransactionValidity(Sha256Hash txid, Integer confirmationsLimit = 1)
    {
        try {
            var transaction = omniGetTransaction(txid)
            var txInfo = getRawTransactionInfo(txid)
            return transaction.valid &&
                    transaction.confirmations >= confirmationsLimit &&
                    txInfo.confirmations >= confirmationsLimit
        } catch(Exception ignored) {
            return false
        }
    }
}
