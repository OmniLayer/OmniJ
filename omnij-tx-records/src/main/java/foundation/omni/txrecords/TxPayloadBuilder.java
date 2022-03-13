package foundation.omni.txrecords;

import foundation.omni.tx.PayloadBuilder;
import foundation.omni.tx.Transactions;

import static foundation.omni.tx.Transactions.TransactionType.SIMPLE_SEND;

/**
 *
 */
public class TxPayloadBuilder {
    private static final int SIZE_VERSIONTYPE = 4;
    private static final int SIZE_32 = 4;
    private static final int SIZE_64 = 8;

    public static byte[] simpleSend(TransactionRecords.SimpleSend parms) {
        return PayloadBuilder.create(SIMPLE_SEND)
                .putInt32(parms.currencyId())
                .putInt64(parms.amount())
                .bytes();

    }

    public static byte[] buildPayload(Transactions.OmniTx txInfo) {
        return switch (txInfo.type()) {
            case SIMPLE_SEND -> simpleSend((TransactionRecords.SimpleSend) txInfo);
            case SEND_TO_OWNERS -> throw new IllegalArgumentException("unsupported tx type");
            default -> throw new IllegalArgumentException("unsupported tx type");
        };
    }
}
