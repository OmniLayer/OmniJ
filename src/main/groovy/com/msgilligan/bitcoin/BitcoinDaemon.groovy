package com.msgilligan.bitcoin

/**
 * User: sean
 * Date: 7/27/14
 * Time: 12:47 AM
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
