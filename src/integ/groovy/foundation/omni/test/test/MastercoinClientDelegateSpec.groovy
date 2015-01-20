package foundation.omni.test.test

import com.msgilligan.bitcoin.rpc.RPCURL
import foundation.omni.BaseMainNetSpec
import foundation.omni.rpc.MastercoinClient
import foundation.omni.rpc.MastercoinClientDelegate
import spock.lang.Specification


/**
 * User: sean
 * Date: 7/24/14
 * Time: 8:16 AM
 */
class MastercoinClientDelegateSpec extends Specification implements MastercoinClientDelegate {

    {
        client = new MastercoinClient(RPCURL.defaultRegTestURL, BaseMainNetSpec.rpcuser, BaseMainNetSpec.rpcpassword)
    }

//    void setupSpec() {
//        this.setClient(new MastercoinClient(RPCURL.defaultMainNetURL, BaseMainNetSpec.rpcuser, BaseMainNetSpec.rpcpassword))
//        assert client != null
//    }

    def "get block count"() {
        expect:
        client != null
        blockCount > 0
    }
}