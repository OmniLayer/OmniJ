package foundation.omni.rest;

import org.bitcoinj.core.Address;

import java.util.List;

/**
 *
 */
public interface OmniBalanceService {
    OmniJBalances balancesForAddresses(List<Address> addresses);
    WalletAddressBalance balancesForAddress(Address address);
}
