package foundation.omni.cli

import com.msgilligan.bitcoin.cli.BaseCLISpec
import spock.lang.Ignore


/**
 * Integration test of ConsensusCLI in RegTest mode
 * Assumes bitcoind running on localhost in RegTest mode.
 */
class ConsensusCLISpec extends BaseCLISpec {

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
        def result = command '-regtest -rpcwait -property 1'

        then:
        result.status == 0
        result.output.length() > 0
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
        def result = command '-regtest -rpcwait -property=1 -rpcconnect=127.0.0.1'

        then:
        result.status == 0
        result.output.length() > 0
        result.error.length() == 0
    }

    // This test fails if previous tests have been run, but succeeds if it
    // is run independently. The problem is probably that cookies aren't being
    // cleared between tests.
    @Ignore("This test fails if we previously authorized")
    def "fetch MSC consensus to stdout setting bad username & password"() {
        when:
        def result = command '-regtest -rpcwait -rpcuser=x -rpcpassword=y -property=1'

        then:
        result.status == 1
        result.output.length() > 0
        result.error.length() == 0
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
        ConsensusCLI cli = new ConsensusCLI(args)
        return runCommand(cli)
    }

}