package foundation.omni.rest.omniwallet

import foundation.omni.net.OmniMainNetParams
import foundation.omni.rest.omniwallet.json.OmniwalletAddressBalance
import okhttp3.HttpUrl
import org.bitcoinj.core.Address
import org.bitcoinj.core.LegacyAddress
import retrofit2.Response
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.CompletableFuture

import static foundation.omni.CurrencyID.*

/**
 * Tests rate-limiting error response of service.balancesForAddresses inside the client
 */
@Ignore("Functional STRESS test")
class OmniwalletClientServiceRateLimitSpec extends Specification {
    final Address exodusAddress = OmniMainNetParams.get().exodusAddress;
    final Address testAddr = LegacyAddress.fromBase58(null, "19ZbcHED8F6u5Wr5gp97KMVNvKV8HUrmeu")

    @Shared OmniwalletClient client


    def "repeatedly load balances of addresses with multiple addresses and see if can cause a rate-limit error"() {
        given:
        def numberOfRequests = 1000
        def futures = new CompletableFuture<Response<Map<Address, OmniwalletAddressBalance>>>[numberOfRequests]

        when: "we send numberOfRequests async request and wait for them to finish"
        for (i in 0..<numberOfRequests) {
            futures[i] = client.service
                    .balancesForAddresses([testAddr, exodusAddress])
        }

        and: "we wait for them all to complete"
        CompletableFuture.allOf(futures);

        then: "we should see at least one rate-limit error show up for BTC"
        futures.any { response ->
            response.get().body()
                    .get(exodusAddress)
                    .balance
                    .findAll{it.id == BTC}
                    .any{it.error}
        }
    }

    def setup() {
        HttpUrl baseURL = HttpUrl.parse("https://staging.omniwallet.org")
        boolean debug = true
        client = new OmniwalletClient(baseURL, true)
    }

}
