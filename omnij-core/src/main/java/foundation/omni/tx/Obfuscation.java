package foundation.omni.tx;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Sha256Hash;

import javax.xml.bind.DatatypeConverter;

/**
 * Omni Class B Obfuscation
 *
 * @author msgilligan
 * @author dexX7
 */
public class Obfuscation {


    public static byte[] obfuscate(final byte[] input, Address address) {
        return obfuscate(input, address.toString());
    }

    public static byte[] obfuscate(final byte[] input, final String seed) {
        int nFullChunks = input.length / EncodingClassB.chunkSize;                    // number of full chunks
        int nPartialChunks = (input.length % EncodingClassB.chunkSize == 0) ? 0 : 1;  // number of partial chunks
        int nChunks = nFullChunks + nPartialChunks;

        String strHash = seed;

        byte[] output = new byte[input.length];
        byte[] curChunk = new byte[EncodingClassB.chunkSize];

        int pos = 0;

        for (int n = 0; n < nChunks ; n++) {
            pos = n * EncodingClassB.chunkSize;
            int length = Math.min(EncodingClassB.chunkSize, input.length - pos);

            // (Unnecessarily) copy an input chunk to curChunk
            System.arraycopy(input, pos, curChunk, 0, length);

            // Calculate current hash and save for next iteration
            strHash = upperSha256(seed);

            // XOR
            byte[] encChunk = xorHashMix(strHash, curChunk);

            // (Unnecessarily) copy into output
            System.arraycopy(encChunk, 0, output, pos, length );
        }
        return output;
    }

    public static String upperSha256(String string) {
        return Sha256Hash.create(string.getBytes()).toString().toUpperCase();
    }

    public static byte[] xorHashMix(String string, byte[] bytes) {
        assert(bytes.length == 31); // Should we require a Sha256Hash here?
        byte[] strBytes = hexToBinary(string);
        assert(strBytes.length == 32);

        byte[] output = new byte[31];

        int n = 0;
        for (byte b : bytes) {
            output[n] = (byte) (b ^ strBytes[n++]);
        }
        return output;
    }

    static byte[] hexToBinary(String hexString) {
        return DatatypeConverter.parseHexBinary(hexString);
    }
}
