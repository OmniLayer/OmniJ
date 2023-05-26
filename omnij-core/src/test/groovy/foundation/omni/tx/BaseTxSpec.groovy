package foundation.omni.tx

import foundation.omni.net.OmniNetworkParameters
import org.bitcoinj.base.Address
import org.bitcoinj.crypto.ECKey
import org.bitcoinj.base.LegacyAddress
import org.bitcoinj.params.MainNetParams
import org.bouncycastle.util.encoders.Hex
import spock.lang.Specification


/**
 * Abstract Base Class for Tx test Specifications
 */
abstract class BaseTxSpec extends Specification {
    static final netParams = MainNetParams.get()
    static final omniParams = OmniNetworkParameters.fromBitcoinNetwork(netParams.network())

    static final BigInteger NotSoPrivatePrivateKey = new BigInteger(1, Hex.decode("180cb41c7c600be951b5d3d0a7334acc7506173875834f7a6c4c786a28fcbb19"));
    static final senderKey = ECKey.fromPrivate(NotSoPrivatePrivateKey, false)
    static final Address senderAddr = LegacyAddress.fromKey(netParams, senderKey)

    static public byte[] hex(String string) {
        return string.decodeHex()
    }

}