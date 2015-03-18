package com.msgilligan.bitcoin.cli

import com.msgilligan.bitcoin.rpc.test.TestServers
import spock.lang.Specification

import java.nio.charset.StandardCharsets


/**
 * Base Spec for testing CLICommand subclasses
 */
abstract class BaseCLISpec extends Specification {

    static final protected String rpcUser = TestServers.rpcTestUser
    static final protected String rpcPassword = TestServers.rpcTestPassword

    protected String[] parseCommandLine(String line) {
        String[] args = line.split(' ')     // (Overly?) simple parsing of string into args[]
        return args
    }

    /**
     * Run a command and capture status and output
     *
     * @param command
     * @return
     */
    protected CommandResult runCommand(CliCommand command) {
        // Setup CommandResult to capture status and streams
        CommandResult result = new CommandResult()
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

    protected class CommandResult {
        Integer status
        String  output
        String  error
    }

}