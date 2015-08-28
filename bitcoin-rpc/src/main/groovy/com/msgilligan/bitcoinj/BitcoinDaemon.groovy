package com.msgilligan.bitcoinj

/**
 * Wrapper class for starting and stopping bitcoind
 */
class BitcoinDaemon {
    Process process

    BitcoinDaemon(String command) {
        process = command.execute()
    }

    void stop() {
        process.waitForOrKill(1)
    }


}
