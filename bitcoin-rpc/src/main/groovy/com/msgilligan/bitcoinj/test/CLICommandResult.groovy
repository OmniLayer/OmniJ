package com.msgilligan.bitcoinj.test

/**
 * Holds all results of a command (after a test) including: status code, stdout as a string,
 * and stderr as a string.
 */
class CLICommandResult {
    Integer status
    String  output
    String  error
}
