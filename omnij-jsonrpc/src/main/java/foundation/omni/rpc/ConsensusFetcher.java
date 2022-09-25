package foundation.omni.rpc;

import foundation.omni.BalanceEntry;
import foundation.omni.CurrencyID;
import foundation.omni.json.pojo.ConsensusSnapshot;
import foundation.omni.json.pojo.OmniPropertyInfo;
import org.bitcoinj.core.Address;

import java.util.List;
import java.util.SortedMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

/**
 * Interface implemented by all consensus fetching tools.
 * Should really be in the package foundation.omni.consensus, but is here as a workaround
 * to what was once a Groovy joint-compilation issue.
 * TODO: Consolidate with other Consensus-related classes now that joint compilation issue is gone.
 */
public interface ConsensusFetcher {
    /**
     * Fetch the current block height
     *
     * @return A future for the current block height of the remote consensus server
     */
    CompletableFuture<Integer> currentBlockHeightAsync();

    /**
     * Get a list of smart properties <b>asynchronously</b>.
     *
     * @return A future list of property objects
     */
    CompletableFuture<List<OmniPropertyInfo>> listSmartProperties();

    /**
     * Fetch a consensus balance-map for a currency.
     *
     * @param currencyID The currency to fetch a balance map for
     * @return A map of balances sorted by address
     * @throws InterruptedException if something went wrong
     * @throws ExecutionException if something went wrong
     */
    default SortedMap<Address, BalanceEntry> getConsensusForCurrency(CurrencyID currencyID) throws InterruptedException, ExecutionException {
        return getConsensusForCurrencyAsync(currencyID).get();
    }

    /**
     * Asynchronously get a sorted address-balance map for currency
     *
     * @param currencyID the currency.
     * @return a future for the address-balance map.
     */
    CompletableFuture<SortedMap<Address, BalanceEntry>> getConsensusForCurrencyAsync(CurrencyID currencyID);
    
    /**
     * Fetch a consensus snapshot for a currencyID
     *
     * @param currencyID The currency to get consensus data for
     * @return Consensus data for all addresses owning currencyID
     * @throws InterruptedException if something went wrong
     * @throws ExecutionException if something went wrong
     */
    default ConsensusSnapshot getConsensusSnapshot(CurrencyID currencyID) throws InterruptedException, ExecutionException {
        return getConsensusSnapshotAsync(currencyID).get();
    }

    /**
     * Get a ConsensusSnapshot asynchronously
     *
     * Checks blockheight before and after and retries up to twice if snapshot has indeterminate blockheight.
     *
     * @param currencyID Currency ID for snapshot
     * @return A future for the snapshot
     */
    default CompletableFuture<ConsensusSnapshot> getConsensusSnapshotAsync(CurrencyID currencyID) {
        return currentBlockHeightAsync()
                .thenCompose(beforeHeight -> attemptConsensusSnapshotAsync(currencyID, beforeHeight))
                .handle(this::completeOrRetryConsensusSnapshot).thenCompose(Function.identity())
                .handle(this::completeOrRetryConsensusSnapshot).thenCompose(Function.identity());
    }

    /**
     * Handler that will either forward a valid ConsensusSnapshot via {@link CompletableFuture#completedFuture} or
     * will start a retry fetch attempt at return a {@code CompletableFuture<ConsensusSnapshot>} for the retry.
     * This method is meant to be used in {@link CompletableFuture#handle}.
     * 
     * @param snapshot A candidate ConsensusSnapshot, if not `null` this must be a valid/successful ConsensusSnapshot
     * @param throwable A throwable that is either {@link IndeterminateSnapshotException} signifying a retry or any
     *                  other type of {@code Exception} which will be forwarded.
     * @return a completed future for a ConsensusSnapshot or a future for a retry attempt at a ConsensusSnapshot
     */
    default CompletableFuture<ConsensusSnapshot> completeOrRetryConsensusSnapshot(ConsensusSnapshot snapshot, Throwable throwable) {
        CompletableFuture<ConsensusSnapshot> completedOrRetry;
        if (snapshot != null) {
            // If we were given a ConsensusSnapshot then we are done return a `completedFuture`.
            completedOrRetry = CompletableFuture.completedFuture(snapshot);
        } else if (throwable instanceof IndeterminateSnapshotException) {
            // If throwable is an IndeterminateSnapshotException, we need to try again.
            IndeterminateSnapshotException e = (IndeterminateSnapshotException) throwable;
            completedOrRetry =  attemptConsensusSnapshotAsync(e.snapshot.getCurrencyID(), e.snapshot.getBlockHeight());
        } else {
            // Otherwise throwable is passed on with an already `completeExceptionally` future.
            completedOrRetry = new CompletableFuture<>();
            completedOrRetry.completeExceptionally(throwable);
        }
        return completedOrRetry;
    }

    /**
     * Contains the indeterminate snapshot with the *after* blockHeight
     */
    class IndeterminateSnapshotException extends Exception {
        public final ConsensusSnapshot snapshot;

        IndeterminateSnapshotException(ConsensusSnapshot snapshot) {
            this.snapshot = snapshot;
        }
    }

    /**
     * Asynchronously get a ConsensusSnapshot. If the blockheight after fetching the snapshot hasn't changed
     * from {@code beforeHeight} parameter, return the ConsensusSnapshot else return and Exception.
     * <p>
     * Compose balance map entries with a currentBlockHeightAsync to produce a candidate ConsensusSnapshot.
     *
     * @param currencyID The currency ID to query
     * @param beforeHeight blockHeight immediately before this attempt
     * @return A future that will return a ConsensusSnapshot (if block doesn't change) or an exception otherwise.
     */
    default CompletableFuture<ConsensusSnapshot> attemptConsensusSnapshotAsync(CurrencyID currencyID, int beforeHeight) {
        CompletableFuture<ConsensusSnapshot> snapshotAttempt = new CompletableFuture<>();
        getConsensusForCurrencyAsync(currencyID)
                .thenCompose(entries -> currentBlockHeightAsync().thenApply(afterHeight -> createSnapshot(currencyID, afterHeight, entries)))
                .whenComplete(((snapshot, throwable) -> {
                    if ((snapshot != null) && (snapshot.getBlockHeight() == beforeHeight)) {
                        snapshotAttempt.complete(snapshot);
                    }
                    else if (snapshot !=null) {
                        snapshotAttempt.completeExceptionally(new IndeterminateSnapshotException(snapshot));
                    } else {
                        snapshotAttempt.completeExceptionally(throwable);
                    }
                }
                ));
        return snapshotAttempt;
    }

    /**
     * Create a ConsensusSnapshot record. Fills in client-specific information.
     *
     * @param id Currency ID
     * @param blockHeight blockheight
     * @param entries a map of BalanceEntry objects
     * @return ConsensusSnapshot
     */
    ConsensusSnapshot createSnapshot(CurrencyID id, int blockHeight, SortedMap<Address, BalanceEntry> entries);
}
