package com.msgilligan.bitcoin.rpc

import groovy.transform.CompileStatic
import foundation.omni.rpc.OmniCLIClient

/**
 * Bitcoin JSON-RPC client with method names that exactly match wire and CLI names.
 *
 * Currently incomplete and unused. Should this extend BitcoinClient or wrap it?
 */
@CompileStatic
class BitcoinCLIClient extends BitcoinClient {

    BitcoinCLIClient(URI server, String rpcuser, String rpcpassword) {
        super(server, rpcuser, rpcpassword)
    }
}
