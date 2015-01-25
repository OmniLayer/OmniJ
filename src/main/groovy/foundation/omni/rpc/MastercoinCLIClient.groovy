package foundation.omni.rpc

import com.google.bitcoin.core.Address
import com.google.bitcoin.core.Sha256Hash
import com.google.bitcoin.core.Transaction
import com.msgilligan.bitcoin.rpc.BitcoinCLIAPI
import groovy.transform.CompileStatic

/**
 * Bitcoin and Mastercoin JSON-RPC client that closely mirrors JSON-RPC API.
 *
 * <p>
 * A JSON-RPC client for Bitcoin/Mastercoin with JVM method names that match
 * the RPC method names and bitcoin-cli method names exactly.
 * (in other words, all lowercase and underscores)
 *
 * <p>
 * We use Groovy defaults to set automatic parameters to <code>null</code>. This is better
 * than using Java varargs because it allows us to specify a unique type for each parameter.
 * We set parameters to <code>null</code>, not the default values in the RPC API
 * because we want the server to choose the defaults, not our client. A <code>null</code>
 * parameter is not sent in the RPC request.
 *
 */
@CompileStatic
class MastercoinCLIClient extends MastercoinClient implements BitcoinCLIAPI {

    MastercoinCLIClient(URL server, String rpcuser, String rpcpassword) {
        super(server, rpcuser, rpcpassword)
    }


}
