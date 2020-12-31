package foundation.omni.address;

import org.bitcoinj.core.Bech32;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.SegwitAddress;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.Networks;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Convert between Bitcoin and Omni Bech32 Segwit addresses
 * Adds Omni networks to bitcoinj's {@link Networks} registry
 * Bitcoin: bc1xxxx
 * Omni: om1xxxx
 */
public class OmniSegwitAddressConverter {
    /**
     * Default set of Omni Networks for Omni Address parsing
     * (We don't include RegTest by default because RegTest and TestNet addresses use the same
     * prefix in Base58 format)
     */
    public static final Set<NetworkParameters> defaultOmniNetworks =
            unmodifiableSet(OmniAddressMainNetParams.get(), OmniAddressTestNetParams.get());

    static {
        // TODO: Maybe we don't want to modify bitcoinj's list of networks unless explicitly configured?
        Networks.register(defaultOmniNetworks);
    }

    public static SegwitAddress btcToOmni(SegwitAddress btcAddress) {
        NetworkParameters omniParams = btcParamsToOmniParams(btcAddress.getParameters());
        Bech32.Bech32Data btcBech32Data = Bech32.decode(btcAddress.toBech32());
        String omniAddressString = Bech32.encode(omniParams.getSegwitAddressHrp(), btcBech32Data.data);
        return SegwitAddress.fromBech32(omniParams, omniAddressString);
    }

    public static SegwitAddress btcToOmni(String btcAddressString) {
        SegwitAddress btcAddress = SegwitAddress.fromBech32(null, btcAddressString);
        return btcToOmni(btcAddress);
    }

    public static SegwitAddress btcToOmni(NetworkParameters btcParams, String btcAddressString) {
        SegwitAddress btcAddress = SegwitAddress.fromBech32(btcParams, btcAddressString);
        return btcToOmni(btcAddress);
    }

    public static SegwitAddress omniToBtc(SegwitAddress omniAddress) {
        NetworkParameters btcParams = omniParamsToBtcParams(omniAddress.getParameters());
        Bech32.Bech32Data btcBech32Data = Bech32.decode(omniAddress.toBech32());
        String btcAddressString = Bech32.encode(btcParams.getSegwitAddressHrp(), btcBech32Data.data);
        return SegwitAddress.fromBech32(btcParams, btcAddressString);
    }

    public static SegwitAddress omniToBtc(String omniAddressString) {
        SegwitAddress btcAddress = SegwitAddress.fromBech32(null, omniAddressString);
        return omniToBtc(btcAddress);
    }

    public static SegwitAddress omniToBtc(NetworkParameters btcParams, String btcAddressString) {
        SegwitAddress btcAddress = SegwitAddress.fromBech32(btcParams, btcAddressString);
        return omniToBtc(btcAddress);
    }

    static private NetworkParameters btcParamsToOmniParams(NetworkParameters btcParams) {
        switch (btcParams.getId()) {
            case NetworkParameters.ID_MAINNET:
                return OmniAddressMainNetParams.get();
            case NetworkParameters.ID_TESTNET:
                return OmniAddressTestNetParams.get();
            case NetworkParameters.ID_REGTEST:
                return OmniAddressRegTestParams.get();
            default:
                throw new IllegalArgumentException("Unspported network");
        }
    }

    static private NetworkParameters omniParamsToBtcParams(NetworkParameters omniParams) {
        switch (omniParams.getId()) {
            case NetworkParameters.ID_MAINNET:
                return MainNetParams.get();
            case NetworkParameters.ID_TESTNET:
                return TestNet3Params.get();
            case NetworkParameters.ID_REGTEST:
                return RegTestParams.get();
            default:
                throw new IllegalArgumentException("Unspported network");
        }
    }

    // Create an unmodifiable set of NetworkParameters from an array/varargs
    private static Set<NetworkParameters> unmodifiableSet(NetworkParameters... ts) {
        return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(ts)));
    }

}
