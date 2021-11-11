package foundation.omni.netapi;

import org.bitcoinj.core.Address;
import org.consensusj.bitcoin.json.pojo.ChainTip;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Common interface for Omni Core JSON-RPC and Omniwallet for fetching balances for addresses
 */
public interface OmniBalanceService {
    /**
     * Get balances for multiple addresses
     * @param addresses List of addresses to query
     * @return A map of maps containing each property balance for each address
     * @throws InterruptedException something went wrong
     * @throws IOException an I/O exception or API transport error occured
     */
    OmniJBalances balancesForAddresses(List<Address> addresses) throws InterruptedException, IOException;

    /**
     * Get balances for multiple addresses asynchronously
     * @param addresses List of addresses to query
     * @return A future, for a map of maps containing each property balance for each address
     */
    CompletableFuture<OmniJBalances> balancesForAddressesAsync(List<Address> addresses);

    /**
     * Get balances for a single addresses
     * @param address Single address to query
     * @return a map of currency IDs to balances
     * @throws InterruptedException something went wrong
     * @throws IOException an I/O exception or API transport error occured
     */
    WalletAddressBalance balancesForAddress(Address address) throws InterruptedException, IOException;

    /**
     * Get balances for a single addresses asynchronously
     * @param address Single address to query
     * @return a future for a map of currency IDs to balances
     */
    CompletableFuture<WalletAddressBalance>balancesForAddressAsync(Address address);

    /**
     * Return current ChainTip
     * @return "active" ChainTip
     */
    CompletableFuture<ChainTip> getActiveChainTip();
}
