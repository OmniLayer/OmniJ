package com.msgilligan.bitcoin.rpc

import groovy.transform.CompileStatic
import foundation.omni.rpc.MastercoinCLIClient

/**
 * Bitcoin JSON-RPC client with method names that exactly match wire and CLI names.
 *
 *
 */
@CompileStatic
class BitcoinCLIClient extends BitcoinClient implements BitcoinCLIAPI {

    BitcoinCLIClient(URL server, String rpcuser, String rpcpassword) {
        super(server, rpcuser, rpcpassword)
    }
}
