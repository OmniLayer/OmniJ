package foundation.omni.rest;

import org.bitcoinj.core.Address;

import java.io.IOException;
import java.util.List;

/**
 *
 */
public interface OmniBalanceService {
    OmniJBalances balancesForAddresses(List<Address> addresses) throws IOException;
    WalletAddressBalance balancesForAddress(Address address) throws IOException;
}
