package foundation.omni.tx;

import org.bitcoinj.crypto.ECKey;

import java.util.ArrayList;
import java.util.Random;

/**
 * <p>Convert a stream of bytes to a list of ECKeys</p>
 *
 * <p>Currently Assumes stream length is a whole number multiple of EncodingClassB.chunkSize</p>
 *
 * @author msgilligan
 * @author dexX7
 */
public class PubKeyConversion {
    private static final int randSeed = 0x8987FEED; // TODO: Better seed
    private static Random rand = new Random(randSeed);     // TODO: Better generator?

    public static ArrayList<ECKey> convert(byte[] message) {
        ArrayList<ECKey> list = new ArrayList<>();
        byte[] chunk = new byte[EncodingClassB.chunkSize];
        int pos = 0;
        int bytesLeft = message.length;
        while (bytesLeft > 0) {
            int bytesToCopy = Math.min(bytesLeft, EncodingClassB.chunkSize);
            System.arraycopy(message, pos, chunk, 0, bytesToCopy);
            bytesLeft -= bytesToCopy;
            pos += bytesToCopy;
            ECKey key = createPubKey(chunk);
            list.add(key);
        }
        return list;
    }

    /**
     * Convert a 31-byte byte array to a valid public key
     *
     * @param input A 31-byte array to be encoded as an ECKey
     * @return A valid ECKey containing the input data
     */
    private static ECKey createPubKey(byte[] input) {
        if (input.length != EncodingClassB.chunkSize) {
            throw new IllegalArgumentException("invalid input length");
        }
        byte[] pub = new byte[EncodingClassB.prefixPubKeySize];
//        pub[0] = rand.nextBoolean() ? (byte) 0x02 : (byte) 0x03;        // prefix
        // Don't use random for now to make unit testing work -- TODO: Fix this
        pub[0] = (byte) 0x03;        // prefix
        System.arraycopy(input, 0, pub, 1, EncodingClassB.chunkSize);   // Data

        return findValidKey(pub);
    }

    /**
     * Try nonce values for the last byte until a valid ECKey is found
     *
     * @param pub A candidate public key with the last byte not set
     * @return A valid public key
     */
    private static ECKey findValidKey(byte[] pub) {
        ECKey key;
        boolean valid;
        short nonce = 0;

        do {
            valid = true;
            pub[EncodingClassB.pubKeySize] = (byte) nonce++;
            try {
                // Create a Bouncy Castle ECPoint to make sure this pubkey is valid. As of bitcoinj 0.15.10,
                // ECKey itself is lazy and won't validate at construction, so we create the point first.
                key = ECKey.fromPublicOnly(ECKey.CURVE.getCurve().decodePoint(pub), true);
            } catch (IllegalArgumentException e) {
                valid = false;
                key = null;
            }
        } while (!valid && nonce < 256);

        if (nonce >= 256) {
            throw new RuntimeException("couldn't create valid key");
        }

        return key;
    }
}
