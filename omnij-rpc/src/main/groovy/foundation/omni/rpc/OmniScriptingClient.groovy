package foundation.omni.rpc;

import org.consensusj.jsonrpc.groovy.DynamicRpcMethodFallback;
import org.consensusj.bitcoin.jsonrpc.bitcoind.BitcoinConfFile;

import java.io.IOException;

/**
 * Omni RPC client for scripting
 * No args constructor reads bitcoin.conf
 * Allows dynamic methods to access new RPCs
 */
public class OmniScriptingClient extends OmniClient implements DynamicRpcMethodFallback {

    public OmniScriptingClient() {
        super(BitcoinConfFile.readDefaultConfig().getRpcConfig());
    }
}
