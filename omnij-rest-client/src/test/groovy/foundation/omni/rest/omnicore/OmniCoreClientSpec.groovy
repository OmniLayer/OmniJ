package foundation.omni.rest.omnicore

import com.msgilligan.bitcoinj.rpc.RpcURI;
import org.bitcoinj.params.MainNetParams
import spock.lang.Ignore;
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static foundation.omni.CurrencyID.*

/**
 *
 */
@Ignore("Requires a local Omni Core setup and running")
public class OmniCoreClientSpec extends Specification {
    @Shared
    def client

    def "get block height" () {
        when:
        def height = client.currentBlockHeight()

        then: "height is a reasonable MainNet block height"
        height > 400000
    }

    @Unroll
    def "we can get consensus info for currency: #currency"() {
        when: "we get data"
        def balances = client.getConsensusForCurrency(currency)

        then: "something is there"
        balances.size() >= 1

        where:
        currency << [OMNI, TOMNI, MAID, USDT, AGRS, EURT, SEC]
    }


    def setup() {
        client = new OmniCoreClient(MainNetParams.get(),
                RpcURI.getDefaultMainNetURI(),
                "bitcoinrpc",
                "pass")
    }

}
