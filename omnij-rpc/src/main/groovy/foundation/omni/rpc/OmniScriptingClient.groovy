package foundation.omni.rpc;

import org.consensusj.jsonrpc.DynamicRPCFallback;
import com.msgilligan.bitcoinj.rpc.bitcoind.BitcoinConfFile;

import java.io.IOException;

/**
 * Omni RPC client for scripting
 * No args constructor reads bitcoin.conf
 * Allows dynamic methods to access new RPCs
 */
public class OmniScriptingClient extends OmniClient implements DynamicRPCFallback {

    public OmniScriptingClient() {
        super(BitcoinConfFile.readDefaultConfig().getRPCConfig());
    }
}
