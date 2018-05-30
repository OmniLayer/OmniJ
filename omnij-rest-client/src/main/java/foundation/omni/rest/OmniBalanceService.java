package foundation.omni.rest;

import org.bitcoinj.core.Address;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 *
 */
public interface OmniBalanceService {
    OmniJBalances balancesForAddresses(List<Address> addresses) throws InterruptedException, IOException;
    CompletableFuture<OmniJBalances> balancesForAddressesAsync(List<Address> addresses);
    WalletAddressBalance balancesForAddress(Address address) throws InterruptedException, IOException;
}
