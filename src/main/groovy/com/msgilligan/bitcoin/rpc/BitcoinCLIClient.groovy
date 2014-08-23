package com.msgilligan.bitcoin.rpc

import groovy.transform.CompileStatic
import org.mastercoin.rpc.MastercoinCLIClient

/**
 * Bitcoin JSON-RPC client with method names that exactly match wire and CLI names.
 *
 * Currently all functionality that should be here is in
 * org.mastercoin.rpc.MastercoinCLIClient.
 * (Until I figure out a good way of using traits to split functionality up logically.)
 *
 */
@CompileStatic
class BitcoinCLIClient extends MastercoinCLIClient {

    BitcoinCLIClient(URL server, String rpcuser, String rpcpassword) {
        super(server, rpcuser, rpcpassword)
    }
}
