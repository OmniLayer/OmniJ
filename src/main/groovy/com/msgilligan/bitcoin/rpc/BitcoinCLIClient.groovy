package com.msgilligan.bitcoin.rpc

import groovy.transform.CompileStatic
import foundation.omni.rpc.OmniCLIClient

/**
 * Bitcoin JSON-RPC client with method names that exactly match wire and CLI names.
 *
 * Currently all functionality that should be here is in
 * MastercoinCLIClient.
 * (Until I figure out a good way of using traits to split functionality up logically.)
 *
 */
@CompileStatic
class BitcoinCLIClient extends OmniCLIClient {

    BitcoinCLIClient(URL server, String rpcuser, String rpcpassword) {
        super(server, rpcuser, rpcpassword)
    }
}
