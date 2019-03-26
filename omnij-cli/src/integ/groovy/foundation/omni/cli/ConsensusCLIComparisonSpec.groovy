package foundation.omni.cli

import foundation.omni.rpc.test.TestServers
import org.consensusj.jsonrpc.cli.test.CLICommandResult
import org.consensusj.bitcoin.cli.test.CLITestSupport
import spock.lang.Ignore
import spock.lang.Specification


/**
 * Comparisons tests using the tool on MainNet
 */
@Ignore("Disabled for now since the tests in this package are run in RegTest mode")
class ConsensusCLIComparisonSpec extends Specification implements CLITestSupport {

    def "comparison"() {
        setup:
        TestServers testServers = TestServers.getInstance()
        def rpcHost = '127.0.0.1'
        def rpcUser = testServers.rpcTestUser
        def rpcPass = testServers.rpcTestPassword
        String remoteUser = URLEncoder.encode(testServers.stableOmniRpcUser, "UTF-8")
        String remotePass = URLEncoder.encode(testServers.stableOmniRpcPassword, "UTF-8")
        String hostname = testServers.stableOmniRpcHost
        URI remoteURI = "https://${remoteUser}:${remotePass}@${hostname}:8332".toURI()

        def remoteCoreURI = testServers.getStablePublicMainNetURI()

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
    protected CLICommandResult command(String line) {
        String[] args = parseCommandLine(line)     // Parse line into separate args

        // Run the command
        ConsensusCLI cli = new ConsensusCLI(args)
        return runCommand(cli)
    }

}