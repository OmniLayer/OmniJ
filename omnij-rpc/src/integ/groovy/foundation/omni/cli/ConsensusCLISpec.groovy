package foundation.omni.cli

import foundation.omni.rpc.test.TestServers
import spock.lang.Ignore
import spock.lang.Specification


/**
 * Integration test of ConsensusCLI in RegTest mode
 * Assumes bitcoind running on localhost in RegTest mode.
 */
@Ignore("Need to refactor functions from BaseCLISpec into a class or trait")
class ConsensusCLISpec extends Specification {

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
        def result = command "-regtest -rpcuser=${rpcUser} -rpcpassword=${rpcPassword} -rpcwait -property=1 -rpcconnect=127.0.0.1"

        then:
        result.status == 0
        result.output.length() > 0
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

    @Ignore("Too slow")
    def "comparison"() {
        setup:
        def rpcHost = '127.0.0.1'
        def rpcUser = TestServers.rpcTestUser
        def rpcPass = TestServers.rpcTestPassword
        String remoteUser = URLEncoder.encode(TestServers.stableOmniRpcUser, "UTF-8")
        String remotePass = URLEncoder.encode(TestServers.stableOmniRpcPassword, "UTF-8")
        String hostname = TestServers.stableOmniRpcHost
        URI remoteURI = "https://${remoteUser}:${remotePass}@${hostname}:8332".toURI()

        def remoteCoreURI = TestServers.getStablePublicMainNetURI()

        when:
        def result = command "-compare -rpcconnect=${rpcHost} -rpcssl -rpcuser=${rpcUser} -rpcpassword=${rpcPass} -core=${remoteURI} -p 1"

        then:
        result.status == 0
        result.output.length() >= 0
        result.error.length() == 0
    }

    /**
     * Helper method to create and run a command
     *
     * @param line The command args in a single string, separated by spaces
     * @return  status and output streams in Strings
     */
//    protected CommandResult command(String line) {
//        String[] args = parseCommandLine(line)     // Parse line into separate args
//
//        // Run the command
//        ConsensusCLI cli = new ConsensusCLI(args)
//        return runCommand(cli)
//    }

}