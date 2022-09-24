package foundation.omni.rpc;

import org.consensusj.jsonrpc.groovy.DynamicRpcMethodFallback;

/**
 * Omni RPC client for scripting
 * No args constructor reads bitcoin.conf
 * Allows dynamic methods to access new RPCs
 * @deprecated Use {@code var dynamicClient = omniClient as DynamicRpcMethodFallback} if you need a dynamic client
 */
@Deprecated
class OmniScriptingClient extends OmniClient implements DynamicRpcMethodFallback {
    OmniScriptingClient() {
        super()
    }
}
