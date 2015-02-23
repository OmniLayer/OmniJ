package com.msgilligan.bitcoin.cli

/**
 * Integration Spec for GetBlockCount command-line tool
 * Assumes bitcoind running on localhost in RegTest mode.
 */
class GetBlockCountSpec extends BaseCLISpec {

    def "run against local RegTest RPC"() {
        when:
        def result = command '-regtest -rpcwait'

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
    protected CommandResult command(String line) {
        String[] args = parseCommandLine(line)     // Parse line into separate args

        // Run the command
        GetBlockCount cli = new GetBlockCount(args)
        return runCommand(cli)
    }

}