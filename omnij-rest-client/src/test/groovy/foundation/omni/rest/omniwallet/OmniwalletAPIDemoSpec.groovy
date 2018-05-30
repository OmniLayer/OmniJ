package foundation.omni.rest.omniwallet

import foundation.omni.OmniDivisibleValue
import foundation.omni.net.OmniMainNetParams
import spock.lang.Ignore
import spock.lang.Specification

/**
 * Simple Tests/Demo of Omniwallet REST API via SimpleGroovyRestClient
 * TODO: Verify that BTC balance objects include an `error` boolean
 */
@Ignore("This is really an integration test")
class OmniwalletAPIDemoSpec extends Specification {
    static final URL liveURL = "https://www.omniwallet.org/".toURL()
    static final URL stageURL = "https://staging.omniwallet.org/".toURL()
    static final String testAddr = '19ZbcHED8F6u5Wr5gp97KMVNvKV8HUrmeu'
    static final String exodusAddr = OmniMainNetParams.get().exodusAddress.toString()

    def "get block height"() {
        given:
        def client = new SimpleGroovyRestClient(stageURL)

        when:
        def result = client.getJson("/v1/system/revision.json")

        then: "height is a reasonable MainNet block height"
        result.last_block > 466600
    }

    def "get single address balance (www)"() {
        given:
        def client = new SimpleGroovyRestClient(stageURL)

        when:
        def result = client.postForm("/v1/address/addr/", [ addr: testAddr ])

        then: "Something is returned"
        result != null
        result.balance != null

        result.balance[0].value != null
        OmniDivisibleValue.checkValue(result.balance[0].value as BigDecimal)
        result.balance[0].divisible == true

        result.balance[1].value != null
        OmniDivisibleValue.checkValue(result.balance[1].value as BigDecimal)
        result.balance[1].divisible == true
    }

    def "get single address balance (staging)"() {
        given:
        def client = new SimpleGroovyRestClient(stageURL)

        when:
        def result = client.postForm("/v2/address/addr/", [ addr: testAddr ])

        then: "Something is returned"
        result != null
        result[testAddr].balance != null

        result[testAddr].balance[0].value != null
        result[testAddr].balance[0].divisible == true

        result[testAddr].balance[1].value != null
        result[testAddr].balance[1].divisible == true
    }

    def "get multiple address balance (staging)"() {
        given:
        def client = new SimpleGroovyRestClient(stageURL)

        when:
        def result = client.postForm("/v2/address/addr/", [
                addr: [testAddr, exodusAddr]
        ])

        then: "Something is returned"
        result != null

        result[testAddr].balance != null

        result[testAddr].balance[0].value != null
        result[testAddr].balance[0].divisible == true

        result[testAddr].balance[1].value != null
        result[testAddr].balance[1].divisible == true

        result[exodusAddr].balance != null

        result[exodusAddr].balance[0].value != null
        result[exodusAddr].balance[0].divisible == true

        result[exodusAddr].balance[1].value != null
        result[exodusAddr].balance[1].divisible == true

    }
}