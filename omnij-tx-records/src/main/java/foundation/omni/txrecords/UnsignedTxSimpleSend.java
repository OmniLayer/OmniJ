package foundation.omni.txrecords;

import org.bitcoinj.core.Address;
import org.consensusj.bitcoinj.signing.TransactionInputData;

import java.util.List;

/**
 * A Simple Send Payload plus fromAddress, inputs, and changeAddress
 */
public record UnsignedTxSimpleSend(Address fromAddress, List<TransactionInputData> inputs, TransactionRecords.SimpleSend payload, Address changeAddress) {
}
