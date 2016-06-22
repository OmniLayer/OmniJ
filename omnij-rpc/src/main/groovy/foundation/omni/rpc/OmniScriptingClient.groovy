package foundation.omni.rpc;

import com.msgilligan.bitcoinj.rpc.DynamicRPCFallback;
import com.msgilligan.bitcoinj.rpc.bitcoind.BitcoinConfFile;

import java.io.IOException;

/**
 * Omni RPC client for scripting
 * No args constructor reads bitcoin.conf
 * Allows dynamic methods to access new RPCs
 */
public class OmniScriptingClient extends OmniExtendedClient implements DynamicRPCFallback {

    public OmniScriptingClient() {
        super(BitcoinConfFile.readDefaultConfig().getRPCConfig());
    }
}
