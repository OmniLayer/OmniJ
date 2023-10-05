package foundation.omni.netapi.omniwallet;

import foundation.omni.netapi.OmniBalanceService;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import org.consensusj.bitcoin.json.pojo.ChainTip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOError;

/**
 *
 */
public interface RxOmniWalletClient extends OmniBalanceService {
    /* private */ Logger log = LoggerFactory.getLogger(RxOmniWalletClient.class);

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

    private void logSuccess(Object result) {
        log.debug("RPC call returned: {}", result);
    }

    private void logError(Throwable throwable) {
        log.error("Exception in RPCCall", throwable);
    }

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
