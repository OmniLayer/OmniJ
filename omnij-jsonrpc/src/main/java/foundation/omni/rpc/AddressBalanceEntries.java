package foundation.omni.rpc;

import org.bitcoinj.core.Address;

import java.util.TreeMap;

/**
 * A sorted Map of BalanceEntry objects
 */
public class AddressBalanceEntries extends TreeMap<Address, BalanceEntry> {}
