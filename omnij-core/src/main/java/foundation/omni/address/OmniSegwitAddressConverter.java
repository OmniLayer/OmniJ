package foundation.omni.address;

import foundation.omni.net.OmniNetwork;
import org.bitcoinj.base.Bech32;
import org.bitcoinj.base.BitcoinNetwork;
import org.bitcoinj.base.SegwitAddress;

/**
 * Convert between Bitcoin and Omni Bech32 Segwit addresses
 * Bitcoin: bc1xxxx
 * Omni: om1xxxx
 */
class OmniSegwitAddressConverter {

    public static SegwitAddress btcToOmni(SegwitAddress btcAddress) {
        OmniNetwork omniNet = btcNetworkToOmniNetwork((BitcoinNetwork) btcAddress.network());
        Bech32.Bech32Data btcBech32Data = Bech32.decode(btcAddress.toBech32());
        String omniAddressString = Bech32.encode(Bech32.Encoding.BECH32, omniNet.segwitAddressHrp(), btcBech32Data.data);
        return (SegwitAddress) OmniNetwork.addressParser.parseAddress(omniAddressString, omniNet);
    }

    public static SegwitAddress btcToOmni(String btcAddressString) {
        SegwitAddress btcAddress = (SegwitAddress) OmniNetwork.addressParser.parseAddressAnyNetwork(btcAddressString);
        return btcToOmni(btcAddress);
    }

    public static SegwitAddress omniToBtc(SegwitAddress omniAddress) {
        BitcoinNetwork btcNet = omniNetworkToBtcNetwork((OmniNetwork) omniAddress.network());
        Bech32.Bech32Data btcBech32Data = Bech32.decode(omniAddress.toBech32());
        String btcAddressString = Bech32.encode(Bech32.Encoding.BECH32, btcNet.segwitAddressHrp(), btcBech32Data.data);
        return (SegwitAddress) OmniNetwork.addressParser.parseAddress(btcAddressString, btcNet);
    }

    public static SegwitAddress omniToBtc(String omniAddressString) {
        SegwitAddress omniAddress = (SegwitAddress) OmniNetwork.addressParser.parseAddressAnyNetwork(omniAddressString);
        return omniToBtc(omniAddress);
    }

    static private OmniNetwork btcNetworkToOmniNetwork(BitcoinNetwork network) {
        switch (network) {
            case MAINNET:
                return OmniNetwork.MAINNET;
            case TESTNET:
                return OmniNetwork.TESTNET;
            case REGTEST:
                return OmniNetwork.REGTEST;
            default:
                throw new IllegalArgumentException("Unspported network");
        }
    }

    static private BitcoinNetwork omniNetworkToBtcNetwork(OmniNetwork network) {
        switch (network) {
            case MAINNET:
                return BitcoinNetwork.MAINNET;
            case TESTNET:
                return BitcoinNetwork.TESTNET;
            case REGTEST:
                return BitcoinNetwork.REGTEST;
            default:
                throw new IllegalArgumentException("Unspported network");
        }
    }
}
