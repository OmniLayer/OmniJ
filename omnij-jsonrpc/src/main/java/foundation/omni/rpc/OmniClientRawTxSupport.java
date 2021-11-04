package foundation.omni.rpc;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Sha256Hash;
import org.consensusj.jsonrpc.JsonRpcException;

import java.io.IOException;

/**
 * Methods needed to support {@link foundation.omni.rpc.test.OmniTestClientMethods} interface.
 */
public interface OmniClientRawTxSupport {
    Sha256Hash omniSendRawTx(Address fromAddress, String rawTxHex) throws JsonRpcException, IOException;
    Sha256Hash omniSendRawTx(Address fromAddress, String rawTxHex, Address referenceAddress) throws JsonRpcException, IOException;
}
