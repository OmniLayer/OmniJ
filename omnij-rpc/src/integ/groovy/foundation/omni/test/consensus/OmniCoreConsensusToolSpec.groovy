package foundation.omni.test.consensus

import foundation.omni.consensus.OmniCoreConsensusTool
import foundation.omni.rpc.test.TestServers
import org.bitcoinj.base.BitcoinNetwork
import spock.lang.Ignore
import spock.lang.Specification


/**
 * Test Spec for OmniCoreConsensusTool
 */
@Ignore("Remote core server is down")
class OmniCoreConsensusToolSpec extends Specification {
    static final private TestServers testServers = TestServers.instance

    def "Can access RPC given a URI and get block height"() {
        setup:
        String user = testServers.stableOmniRpcUser
        String pass = testServers.stableOmniRpcPassword
        String encUser = URLEncoder.encode(user, "UTF-8")
        String encPass = URLEncoder.encode(pass, "UTF-8")
        String hostname = testServers.stableOmniRpcHost
        URI uri = "https://${encUser}:${encPass}@${hostname}:8332".toURI()

        OmniCoreConsensusTool fetcher = new OmniCoreConsensusTool(BitcoinNetwork.MAINNET, uri)

        when: "we get block height"
        def blockHeight = fetcher.currentBlockHeightAsync().get()

        then: "it looks reasonable"
        blockHeight > 323000  // Greater than a relatively recent main-net block
    }

}