package foundation.omni.netapi.omniwallet;

import foundation.omni.netapi.OmniBalanceService;
import foundation.omni.rpc.ConsensusFetcher;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import org.bitcoinj.core.Sha256Hash;
import org.consensusj.bitcoin.json.pojo.ChainTip;
import org.consensusj.bitcoin.rx.jsonrpc.PollingChainTipService;

import java.io.IOError;

/**
 *
 */
public interface RxOmniWalletClient extends OmniBalanceService {

    /**
     * Get the active chain tip if there is one (useful for polling clients)
     *
     * @return The active ChainTip if available (onSuccess) otherwise onComplete (if not available) or onError (if error occurred)
     */
    default Maybe<ChainTip> currentChainTipMaybe() {
        return pollChainTipOnce();
    }

    /**
     * Poll a method, ignoring {@link IOError}.
     * The returned {@link Maybe} will:
     * <ol>
     *     <li>Emit a value if successful</li>
     *     <li>Empty Complete on IOError</li>
     *     <li>Error out if any other Exception occurs</li>
     * </ol>
     *
     * @return A Maybe for the expected result type
     */
    default Maybe<ChainTip> pollChainTipOnce() {
        return getActiveChainTipSingle()
                .doOnSuccess(this::logSuccess)
                .doOnError(this::logError)
                .toMaybe()
                .onErrorComplete(this::isTransientError);    // Empty completion if IOError
    }

    void logError(Throwable throwable);

    void logSuccess(ChainTip chainTip);

    /**
     * Determine if error is transient and should be ignored
     *
     * TODO: Ignoring all IOError is too broad
     *
     * @param t Error thrown from calling an RPC method
     * @return true if the error is transient and can be ignored
     */
    private boolean isTransientError(Throwable t) {
        return t instanceof IOError;
    }

    /**
     * Wrap a block height call in a Single. (deferred, so "hot")
     * @return A Single for the block height
     */
    private Single<ChainTip> getActiveChainTipSingle() {
        return Single.defer(() -> Single.fromCompletionStage(getActiveChainTip()));
    }
}
