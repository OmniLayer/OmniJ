package com.msgilligan.bitcoinj.test

import com.msgilligan.bitcoinj.cli.CliCommand
import com.msgilligan.bitcoinj.rpc.test.TestServers

import java.nio.charset.StandardCharsets

/**
 * Support functions for Command Line tool Spock test specifications
 */
trait CLITestSupport {

    final TestServers testServers = TestServers.instance
    final String rpcUser = testServers.rpcTestUser
    final String rpcPassword = testServers.rpcTestPassword

    String[] parseCommandLine(String line) {
        String[] args = line.split(' ')     // (Overly?) simple parsing of string into args[]
        return args
    }

    /**
     * Run a command and capture status and output
     *
     * @param command
     * @return
     */
    CLICommandResult runCommand(CliCommand command) {
        // Setup CommandResult to capture status and streams
        CLICommandResult result = new CLICommandResult()
        InputStream is = new ByteArrayInputStream(new byte[0])
        ByteArrayOutputStream bos = new ByteArrayOutputStream()
        PrintStream cos = new PrintStream(bos)
        ByteArrayOutputStream bes = new ByteArrayOutputStream()
        PrintStream ces = new PrintStream(bes)

        // Run the command
        result.status = command.run(is, cos, ces)

        // Put output and error streams in strings
        String charset = StandardCharsets.UTF_8.toString()
        result.output = bos.toString(charset)
        result.error = bes.toString(charset)

        return result
    }
}