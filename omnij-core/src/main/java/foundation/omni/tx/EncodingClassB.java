package foundation.omni.tx;

/**
 * Constants for Omni Class B Transaction encoding
 */
public class EncodingClassB {
    static final int chunkDataSize = 30;            // Data bytes per chunk
    static final int chunkSize = chunkDataSize + 1; // Chunk size data + seq byte
    static final int pubKeySize = 32;
    static final int prefixPubKeySize = pubKeySize + 1;
}
