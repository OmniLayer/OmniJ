package com.msgilligan.bitcoinj.rpc

/**
 * Use Groovy <code>methodMissing</code> to allow *any* JSON-RPC call to be made
 * as <code>client.rpcMethod(args)</code>. Note that calling a non-existent method
 * will result in an error from the server.
 *
 * The focus of RPC client development in bitcoin-spock has been strongly typed clients
 * the core of which is implemented in and can be used from pure Java. We also
 * believe that a strongly typed RPC client is the best choice for integration tests.
 * This client is provided for those looking for something simple, flexible, dynamic, and Groovy.
 *
 */
class DynamicRPCClient extends RPCClient {

    DynamicRPCClient(URI server, String rpcuser, String rpcpassword) {
        super(server, rpcuser, rpcpassword)
    }

    /**
     * Dynamically forward missing method calls to the server
     *
     * See http://groovy-lang.org/metaprogramming.html#_methodmissing
     *
     * @param name The JSON-RPC method name
     * @param args JSON-RPC arguments
     * @return an object containing the JSON-RPC response.result
     * @throws JsonRPCStatusException
     */
    def methodMissing(String name, def args) {
        Object result = this.send(name, args as List)
        return result
    }
}
