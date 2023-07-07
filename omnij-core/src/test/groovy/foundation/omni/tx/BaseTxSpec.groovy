package foundation.omni.tx

import foundation.omni.net.OmniNetworkParameters
import org.bitcoinj.base.Address
import org.bitcoinj.base.BitcoinNetwork
import org.bitcoinj.base.ScriptType
import org.bitcoinj.crypto.ECKey
import org.bouncycastle.util.encoders.Hex
import spock.lang.Specification


/**
 * Abstract Base Class for Tx test Specifications
 */
abstract class BaseTxSpec extends Specification {
    static final network = BitcoinNetwork.MAINNET
    static final omniParams = OmniNetworkParameters.fromBitcoinNetwork(network)

    static final BigInteger NotSoPrivatePrivateKey = new BigInteger(1, Hex.decode("180cb41c7c600be951b5d3d0a7334acc7506173875834f7a6c4c786a28fcbb19"));
    static final senderKey = ECKey.fromPrivate(NotSoPrivatePrivateKey, false)
    static final Address senderAddr = senderKey.toAddress(ScriptType.P2PKH, network)

    static public byte[] hex(String string) {
        return string.decodeHex()
    }

}