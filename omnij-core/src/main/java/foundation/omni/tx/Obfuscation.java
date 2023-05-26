package foundation.omni.tx;

import org.bitcoinj.base.Address;
import org.bitcoinj.base.Sha256Hash;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Omni Class B Obfuscation
 *
 * @author msgilligan
 * @author dexX7
 */
public class Obfuscation {


    /**
     * Obfuscate an message (Omni Transaction)
     * @param input message to obfuscate
     * @param seedAddress Bitcoin address used to create obfuscation seed string
     * @return obfuscated message
     */
    public static byte[] obfuscate(final byte[] input, Address seedAddress) {
        return obfuscate(input, seedAddress.toString());
    }

    /**
     * Obfuscate an message (Omni Transaction)
     * @param input message to obfuscate
     * @param seed (Is this always a Bitcoin address?)
     * @return obfuscated message
     */
    public static byte[] obfuscate(final byte[] input, final String seed) {
        int nFullChunks = input.length / EncodingClassB.chunkSize;                    // number of full chunks
        int nPartialChunks = (input.length % EncodingClassB.chunkSize == 0) ? 0 : 1;  // number of partial chunks
        int nChunks = nFullChunks + nPartialChunks;

        String strHash = seed;

        byte[] output = new byte[input.length];
        byte[] curChunk = new byte[EncodingClassB.chunkSize];

        int pos;

        for (int n = 0; n < nChunks ; n++) {
            pos = n * EncodingClassB.chunkSize;
            int length = Math.min(EncodingClassB.chunkSize, input.length - pos);

            // (Unnecessarily) copy an input chunk to curChunk
            Arrays.fill(curChunk, (byte) 0);  // Zero it out first
            System.arraycopy(input, pos, curChunk, 0, length);

            // Calculate current hash and save for next iteration
            strHash = upperSha256(strHash);

            // XOR
            byte[] encChunk = xorHashMix(strHash, curChunk);

            // (Unnecessarily) copy into output
            System.arraycopy(encChunk, 0, output, pos, length );
        }
        return output;
    }

    private static String upperSha256(String string) {
        return Sha256Hash.of(string.getBytes(StandardCharsets.UTF_8)).toString().toUpperCase();
    }

    /**
     * XOR hash
     * @param string Hex string
     * @param bytes binary to xor
     * @return xor'ed result
     */
    private static byte[] xorHashMix(String string, byte[] bytes) {
        byte[] strBytes = RawTxBuilder.hexToBinary(string);
        return xor(strBytes, bytes);
    }

    /**
     * Exclusive OR of two byte-arrays
     *
     * Inputs not required to be of equal length, extra bytes are copied from longer array
     *
     * @param lhs Left hand byte array
     * @param rhs Right hand byte array
     * @return Byte array equal in length to the longer input
     */
     static byte[] xor(final byte[] lhs, final byte[] rhs) {
        final byte[] longer = (lhs.length > rhs.length) ? lhs : rhs;
        final byte[] shorter = (lhs.length > rhs.length) ? rhs : lhs;
        final byte[] output = new byte[longer.length];

        for (int n = 0; n < longer.length; n++) {
            if (n < shorter.length) {
                output[n] = (byte) (longer[n] ^ shorter[n]);
            } else {
                output[n] = longer[n];
            }
        }
        return output;
    }

}
