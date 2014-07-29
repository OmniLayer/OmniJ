package org.mastercoin.rpc

import com.msgilligan.bitcoin.rpc.RPCURL
import org.mastercoin.BaseMainNetSpec
import spock.lang.Ignore
import spock.lang.Specification


/**
 * User: sean
 * Date: 7/24/14
 * Time: 8:16 AM
 */
@Ignore
class MastercoinClientDelegateSpec extends Specification implements MastercoinClientDelegate {

    void setupSpec() {
        this.setClient(new MastercoinClient(RPCURL.defaultMainNetURL, BaseMainNetSpec.rpcuser, BaseMainNetSpec.rpcpassword))
        assert client != null
    }

    def "get block count"() {
        expect:
        this.getClient() != null
        client.getBlockCount() > 0
    }
}