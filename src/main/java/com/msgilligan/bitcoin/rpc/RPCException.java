package com.msgilligan.bitcoin.rpc;

/**
 * User: sean
 * Date: 6/16/14
 * Time: 6:28 PM
 */
public class RPCException extends Exception {

    public RPCException(String message) {
        super(message);
    }

    public RPCException(String message, Throwable cause) {
        super(message, cause);
    }

}
