package com.msgilligan.bitcoinj.rpc;

/**
 *
 */
public class JsonRPCException extends Exception {

    public JsonRPCException(String message) {
        super(message);
    }

    public JsonRPCException(String message, Throwable cause) {
        super(message, cause);
    }

}
