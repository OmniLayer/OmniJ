package foundation.omni.cli

import com.msgilligan.bitcoin.test.CLICommandResult
import com.msgilligan.bitcoin.test.CLITestSupport
import foundation.omni.rpc.test.TestServers
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification


/**
 * Integration test of ConsensusCLI in RegTest mode
 * Assumes bitcoind running on localhost in RegTest mode.
 */
class ConsensusCLISpec extends Specification implements CLITestSupport {


    def "help option"() {
        when:
        def result = command '-?'

        then:
        result.status == 1
        result.output.length() > 0
        result.error.length() == 0
    }

    def "fetch MSC consensus to stdout"() {
        when:
        def result = command "-regtest -rpcuser=${rpcUser} -rpcpassword=${rpcPassword} -rpcwait -property 1"

        then:
        result.status == 0
        result.output.length() >= 0
        result.error.length() == 0
    }

    def "no currency specified, should output help"() {
        when:
        def result = command '-regtest -rpcwait'

        then:
        result.status == 1
        result.output.length() > 0
        result.error.length() == 0
    }

    def "fetch MSC consensus to stdout with rpcconnect option"() {
        when:
        def result = command "-regtest -rpcuser=${rpcUser} -rpcpassword=${rpcPassword} -rpcwait -property=1 -rpcconnect=127.0.0.1"

        then:
        result.status == 0
        result.output.length() >= 0
        result.error.length() == 0
    }


    def "fetch MSC consensus to stdout setting bad username & password"() {
        when:
        def result = command '-regtest -rpcwait -rpcuser=x -rpcpassword=y -property=1'

        then:
        result.status == 1
        result.output.length() == 0
        result.error.length() > 0
        result.error == "JSON-RPC Exception: Authorization Required\n"
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
        ConsensusCLI cli = new ConsensusCLI(args)
        return runCommand(cli)
    }

}