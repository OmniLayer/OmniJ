package foundation.omni.tx;

/**
 * Methods to add and remove sequence numbers from byte arrays.
 *
 * For use in building Omni Protocol transactions.
 *
 * @author msgilligan
 * @author dexX7
 *
 */
public class SequenceNumbers {
    private static final int chunkDataSize = 30;
    private static final int maxChunks = Byte.MAX_VALUE;    // TODO: Should this be lower?

    /**
     * Insert sequence numbers into byte array, starting with 0x01.
     *
     * @param input input byte array
     * @return byte array with sequence numbers inserted
     */
    public static byte[] add(final byte[] input) {
        return add(input, 0x01);    // Use default starting sequence number
    }

    /**
     * Insert sequence numbers into byte array, starting with <code>startSeqNum</code>.
     *
     * @param input input byte array
     * @param startSeqNum starting sequence number
     * @return byte array with sequence numbers inserted
     */
    public static byte[] add(final byte[] input, final int startSeqNum) {
        int nFullChunks = input.length / chunkDataSize;                    // number of full chunks
        int nPartialChunks = (input.length % chunkDataSize == 0) ? 0 : 1;  // number of partial chunks
        int nChunks = nFullChunks + nPartialChunks;

        byte[] output = new byte[input.length + nChunks];

        if (nChunks + startSeqNum > maxChunks ) {
            throw new IllegalArgumentException("Too many chunks");
        }

        int outPos = 0;

        for (int n = 0; n < nChunks ; n++) {
            int seqNum = n + startSeqNum;
            int inPos = n * chunkDataSize;

            int length = Math.min(chunkDataSize, input.length - inPos);
            output[outPos++] = (byte) seqNum;
            System.arraycopy(input, inPos, output, outPos, length);
            outPos += length;
        }

        return output;
    }

}
