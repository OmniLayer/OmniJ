package foundation.omni.rest.omniwallet

import foundation.omni.net.OmniMainNetParams
import foundation.omni.rest.omniwallet.json.OmniwalletAddressBalance
import foundation.omni.rest.omniwallet.json.RevisionInfo
import okhttp3.HttpUrl
import org.bitcoinj.core.Address
import org.bitcoinj.core.LegacyAddress
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

import static foundation.omni.CurrencyID.BTC
import static foundation.omni.CurrencyID.BTC
import static foundation.omni.CurrencyID.OMNI
import static foundation.omni.CurrencyID.TOMNI
import static foundation.omni.CurrencyID.USDT

/**
 * Tests for the Retrofit "service" object inside the client
 */
@Ignore("Integration test")
class OmniwalletClientServiceSpec extends Specification {
    final Address exodusAddress = OmniMainNetParams.get().exodusAddress;
    final Address testAddr = LegacyAddress.fromBase58(null, "19ZbcHED8F6u5Wr5gp97KMVNvKV8HUrmeu")

    @Shared OmniwalletClient client

    def "Use getRevisionInfo to get block height" () {
        when:
        RevisionInfo revInfo = client.service.getRevisionInfo().get().body()

        then: "height is a reasonable MainNet block height"
        revInfo.lastBlock > 400000
    }

    def "load balances of addresses with multiple addresses"() {
        when:
        Map<Address, OmniwalletAddressBalance> balances = client.service
                .balancesForAddresses([testAddr, exodusAddress]).get().body()

        and: "we sort them to property id order"
        balances.each { key, value ->
            value.balance.sort { a, b -> a.id <=> b.id }
        }

        then:
        balances != null
        balances[testAddr].balance[0].id == BTC
        balances[testAddr].balance[0].value >= 0
        balances[testAddr].balance[0].error == false

        balances[exodusAddress].balance[0].id == BTC
        balances[exodusAddress].balance[0].value  >= 0
        balances[exodusAddress].balance[0].error == false

        balances[exodusAddress].balance[1].id == OMNI
        balances[exodusAddress].balance[1].value  >= 0
        balances[exodusAddress].balance[1].value != null

        balances[exodusAddress].balance[2].id == TOMNI
        balances[exodusAddress].balance[2].value  >= 0
        balances[exodusAddress].balance[2].value != null
    }

    def setup() {
        HttpUrl baseURL = HttpUrl.parse("https://staging.omniwallet.org")
        boolean debug = true
        client = new OmniwalletClient(baseURL, true)
    }

}
