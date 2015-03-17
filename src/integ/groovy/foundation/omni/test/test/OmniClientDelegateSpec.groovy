package foundation.omni.test.test

import com.msgilligan.bitcoin.rpc.RPCURI
import foundation.omni.rpc.OmniClient
import foundation.omni.rpc.OmniClientDelegate
import foundation.omni.rpc.test.TestServers
import spock.lang.Specification


/**
 * Test that implementing the OmniClientDelegate trait works.
 *
 * TODO: Should we keep this test around?
 */
class OmniClientDelegateSpec extends Specification implements OmniClientDelegate {

    {
        client = new OmniClient(RPCURI.defaultRegTestURI, TestServers.rpcTestUser, TestServers.rpcTestPassword)
    }

    def "get block count"() {
        expect:
        client != null
        blockCount > 0
    }
}