package foundation.omni.json.pojo;

import foundation.omni.BalanceEntry;
import org.bitcoinj.core.Address;

import java.util.TreeMap;

/**
 * A sorted Map of BalanceEntry objects
 */
public class AddressBalanceEntries extends TreeMap<Address, BalanceEntry> {}
