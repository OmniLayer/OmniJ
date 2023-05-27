package foundation.omni.net;

import org.bitcoinj.base.Address;
import org.bitcoinj.base.AddressParser;
import org.bitcoinj.base.BitcoinNetwork;
import org.bitcoinj.base.Coin;
import org.bitcoinj.base.DefaultAddressParser;
import org.bitcoinj.base.LegacyAddress;
import org.bitcoinj.base.Monetary;
import org.bitcoinj.base.Network;
import org.bitcoinj.base.SegwitAddress;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.bitcoinj.base.Coin.COIN;

/**
 * This should only be used for constructing addresses with bitcoinj
 */
public enum OmniNetwork implements Network {
    MAINNET("org.omnilayer.mainnet"),
    TESTNET("org.omnilayer.testnet"),
    SIGNET("org.omnilayer.signet"),
    REGTEST("org.omnilayer.regtest");

    /**
     * Scheme part for Bitcoin URIs.
     */
    public static final String BITCOIN_SCHEME = "bitcoin";
    public static final AddressParser addressParser;
    static {
        addressParser = new DefaultAddressParser(List.of(BitcoinNetwork.MAINNET, BitcoinNetwork.TESTNET, OmniNetwork.MAINNET, OmniNetwork.TESTNET),
                List.of(BitcoinNetwork.MAINNET, BitcoinNetwork.TESTNET, OmniNetwork.MAINNET, OmniNetwork.TESTNET));
    }

    /**
     * The maximum number of coins to be generated
     */
    private static final long MAX_COINS = 21_000_000;

    /**
     * The maximum money to be generated
     */
    public static final Coin MAX_MONEY = COIN.multiply(MAX_COINS);

    /** The ID string for the main, production network where people trade things. */
    public static final String ID_MAINNET = MAINNET.id();
    /** The ID string for the testnet. */
    public static final String ID_TESTNET = TESTNET.id();
    /** The ID string for the signet. */
    public static final String ID_SIGNET = SIGNET.id();
    /** The ID string for regtest mode. */
    public static final String ID_REGTEST = REGTEST.id();
    /** The ID string for the Unit test network -- there is no corresponding {@code enum}. */
    public static final String ID_UNITTESTNET = "org.omnilayer.unittest";

    private final String id;

    // All supported names for this BitcoinNetwork
    private final List<String> allNames;

    // Maps from names and alternateNames to BitcoinNetwork
    private static final Map<String, OmniNetwork> stringToEnum = mergedNameMap();

    OmniNetwork(String networkId, String... alternateNames) {
        this.id = networkId;
        this.allNames = combine(this.toString(), alternateNames);
    }

    /**
     * Return the canonical, lowercase, user-facing {@code String} for an {@code enum}
     * @return canonical lowercase value
     */
    @Override
    public String toString() {
        return name().toLowerCase(Locale.ROOT);
    }

    /**
     * Return the network ID string (previously specified in {@code NetworkParameters})
     *
     * @return The network ID string
     */
    @Override
    public String id() {
        return id;
    }

    /**
     * Header byte of base58 encoded legacy P2PKH addresses for this network.
     * @return header byte as an {@code int}.
     * @see LegacyAddress.AddressHeader
     */
    public int legacyAddressHeader() {
        //return LegacyAddress.AddressHeader.ofNetwork(this).headerByte();
        // TODO: Finalize this as either Omni or Bitcoin
        switch(this) {
            case MAINNET:
                return 115;
            case TESTNET:
                return 115;
            case SIGNET:
                return 115;
            case REGTEST:
                return 115;
        }
        throw new IllegalStateException();
    }

    /**
     * Header byte of base58 encoded legacy P2SH addresses for this network.
     * @return header byte as an {@code int}.
     * @see LegacyAddress.P2SHHeader
     */
    public int legacyP2SHHeader() {
        //return LegacyAddress.P2SHHeader.ofNetwork(this).headerByte();
        // TODO: Finalize this as either Omni or Bitcoin
        switch(this) {
            case MAINNET:
                return 58;
            case TESTNET:
                return 58;
            case SIGNET:
                return 58;
            case REGTEST:
                return 58;
        }
        throw new IllegalStateException();
    }

    /**
     * Return the standard Bech32 {@link SegwitAddress.SegwitHrp} (as a {@code String}) for
     * this network.
     * @return The HRP as a (lowercase) string.
     */
    public String segwitAddressHrp() {
        //return LegacyAddress.P2SHHeader.ofNetwork(this).headerByte();
        switch(this) {
            case MAINNET:
                return "o";
            case TESTNET:
                return "to";
            case SIGNET:
                return "to";
            case REGTEST:
                return "ocrt";
        }
        throw new IllegalStateException();
    }

    private static final Address ExodusAddress = LegacyAddress.fromBase58("1EXoDusjGwvnjZUyKkxZ4UHEf77z6A5S4P", BitcoinNetwork.MAINNET);
    private static final Address TestNetExodusAddress = LegacyAddress.fromBase58("mpexoDuSkGGqvqrkrjiFng38QPkJQVFyqv", BitcoinNetwork.TESTNET);
    private static final Address MoneyManAddress = LegacyAddress.fromBase58("moneyqMan7uh8FqdCA2BV5yZ8qVrc9ikLP", BitcoinNetwork.TESTNET);

    public Address exodusAddress() {
        switch(this) {
            case MAINNET:
                return ExodusAddress;
            case TESTNET:
                return TestNetExodusAddress;
            case SIGNET:
                throw new UnsupportedOperationException(); // not defined yet
            case REGTEST:
                return TestNetExodusAddress;
        }
        throw new IllegalStateException();
    }

    public Address moneyManAddress() {
        switch(this) {
            case MAINNET:
                throw new UnsupportedOperationException(); // No MoneyMan address on MainNet
            case TESTNET:
                return MoneyManAddress;
            case SIGNET:
                throw new UnsupportedOperationException(); // not defined yet
            case REGTEST:
                return MoneyManAddress;
        }
        throw new IllegalStateException();
    }

    public BitcoinNetwork bitcoinNetwork() {
        switch(this) {
            case MAINNET:
                return BitcoinNetwork.MAINNET;
            case TESTNET:
                return BitcoinNetwork.TESTNET;
            case SIGNET:
                return BitcoinNetwork.SIGNET;
            case REGTEST:
                return BitcoinNetwork.REGTEST;
        }
        throw new IllegalStateException();
    }

    public static OmniNetwork of(Network bitcoinNetwork) {
        if (!(bitcoinNetwork instanceof BitcoinNetwork)) throw new IllegalArgumentException();
        switch((BitcoinNetwork) bitcoinNetwork) {
            case MAINNET:
                return OmniNetwork.MAINNET;
            case TESTNET:
                return OmniNetwork.TESTNET;
            case SIGNET:
                return OmniNetwork.SIGNET;
            case REGTEST:
                return OmniNetwork.REGTEST;
        }
        throw new IllegalStateException();
    }

    /**
     * The URI scheme for Bitcoin.
     * @see <a href="https://github.com/bitcoin/bips/blob/master/bip-0021.mediawiki">BIP 0021</a>
     * @return string containing the URI scheme
     */
    @Override
    public String uriScheme() {
        return BITCOIN_SCHEME;
    }

    @Override
    public boolean hasMaxMoney() {
        return true;
    }

    @Override
    public Coin maxMoney() {
        return MAX_MONEY;
    }

    @Override
    public boolean exceedsMaxMoney(Monetary amount) {
        if (amount instanceof Coin) {
            return ((Coin) amount).compareTo(MAX_MONEY) > 0;
        } else {
            throw new IllegalArgumentException("amount must be a Coin type");
        }
    }

    /**
     * Find the {@code BitcoinNetwork} from a name string, e.g. "mainnet", "testnet" or "signet".
     * A number of common alternate names are allowed too, e.g. "main" or "prod".
     * @param nameString A name string
     * @return An {@code Optional} containing the matching enum or empty
     */
    public static Optional<OmniNetwork> fromString(String nameString) {
        return Optional.ofNullable(stringToEnum.get(nameString));
    }

    /**
     * Find the {@code BitcoinNetwork} from an ID String
     * <p>
     * Note: {@link #ID_UNITTESTNET} is not supported as an enum
     * @param idString specifies the network
     * @return An {@code Optional} containing the matching enum or empty
     */
    public static Optional<OmniNetwork> fromIdString(String idString) {
        return Arrays.stream(values())
                .filter(n -> n.id.equals(idString))
                .findFirst();
    }

    // Create a Map that maps name Strings to networks for all instances
    private static Map<String, OmniNetwork> mergedNameMap() {
        return Stream.of(values())
                .collect(HashMap::new,                  // Supply HashMaps as mutable containers
                        OmniNetwork::accumulateNames,    // Accumulate one network into hashmap
                        Map::putAll);                       // Combine two containers
    }

    // Add allNames for this Network as keys to a map that can be used to find it
    private static void accumulateNames(Map<String, OmniNetwork> map, OmniNetwork net) {
        net.allNames.forEach(name -> map.put(name, net));
    }

    // Combine a String and an array of String and return as an unmodifiable list
    private static List<String> combine(String canonical, String[] alternateNames) {
        List<String> temp = new ArrayList<>();
        temp.add(canonical);
        temp.addAll(Arrays.asList(alternateNames));
        return Collections.unmodifiableList(temp);
    }

}
