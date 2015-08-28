package com.msgilligan.bitcoinj.cli

import com.msgilligan.bitcoinj.test.CLICommandResult
import com.msgilligan.bitcoinj.test.CLITestSupport
import spock.lang.Specification

/**
 * Integration Spec for GetBlockCount command-line tool
 * Assumes bitcoind running on localhost in RegTest mode.
 */
class GetBlockCountSpec extends Specification implements CLITestSupport {

    def "run against local RegTest RPC"() {
        when:
        def result = command "-regtest -rpcuser=${rpcUser} -rpcpassword=${rpcPassword} -rpcwait"

        then:
        result.status == 0
        result.output.length() > 0
        result.output.startsWith("Block count: ")
    }

    /**
     * Helper method to create and run a command
     *
     * @param line The command args in a single string, separated by spaces
     * @return  status and output streams in Strings
     */
    protected CLICommandResult command(String line) {
        String[] args = parseCommandLine(line)     // Parse line into separate args

        // Run the command
        GetBlockCount cli = new GetBlockCount(args)
        return runCommand(cli)
    }

}