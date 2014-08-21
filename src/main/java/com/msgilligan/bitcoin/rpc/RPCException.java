package com.msgilligan.bitcoin.rpc;

/**
 * Exception to throw upon RPC API error (needs work)
 */
public class RPCException extends Exception {

    public RPCException(String message) {
        super(message);
    }

    public RPCException(String message, Throwable cause) {
        super(message, cause);
    }

}
