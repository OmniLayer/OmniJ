package foundation.omni.netapi.omniwallet

import foundation.omni.netapi.omniwallet.WalletBackupFile
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

/**
 *
 */
@Ignore
class OmniwalletAddressListBalanceFetchSpec extends Specification {
    static final URL liveURL = "https://www.omniwallet.org/".toURL()
    static final URL stageURL = "https://staging.omniwallet.org/".toURL()
    static final testFilePath = "foundation/omni/rest/omniwallet/"
    static final testFileName = "sample_address_list.json"
    
    @Shared
    def addressL
    def "get block height"() {
        given:
        def client = new SimpleGroovyRestClient(liveURL)

        when:
        def result = client.getJson("/v1/system/revision.json")

        then: "height is a reasonable MainNet block height"
        result.last_block > 466600
    }

    def "compare balances from json address file"() {
        given:
        def liveClient = new OmniAdHocClient(liveURL)
        def stageClient = new OmniAdHocClient(stageURL)
        def path =  testFilePath + testFileName
        def file = new File(ClassLoader.getSystemResource(path).toURI())
        def addressFile = WalletBackupFile.read(file)
        def liveBalances = [:]
        def stageBalances = [:]

        when:
        addressFile.addresses.each { it ->
            liveBalances[it.address] = liveClient.v1AddressQuery(it.address)
        }
        addressFile.addresses.each { it ->
            stageBalances[it.address] = stageClient.v2AddressQuery(it.address)
        }

        then:
        liveBalances == stageBalances
    }
    
    class OmniAdHocClient extends SimpleGroovyRestClient {

        OmniAdHocClient(URL baseURL) {
            super(baseURL)
        }

        def v1AddressQuery(String address) {
            def balanceObj = postForm("/v1/address/addr/", [ addr: address ]).balance
            def btcBalance = findBtc(balanceObj)
            return btcBalance.value
        }

        def v2AddressQuery(String address) {
            def balanceObj = postForm("/v2/address/addr/", [ addr: address ])[address].balance
            def btcBalance = findBtc(balanceObj)
            return btcBalance.value
        }

        def findBtc(List bo) {
            return bo.find { it.symbol == "BTC"};
        }
    }
}
