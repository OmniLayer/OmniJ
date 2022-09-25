package foundation.omni.netapi.omnicore


import foundation.omni.net.OmniRegTestParams
import foundation.omni.json.pojo.OmniJBalances
import foundation.omni.json.pojo.WalletAddressBalance
import org.bitcoinj.core.Address
import org.bitcoinj.core.LegacyAddress
import org.bitcoinj.params.RegTestParams
import org.consensusj.bitcoin.jsonrpc.RpcURI
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

import java.util.stream.Collectors

/**
 *
 */
@Ignore("Requires a local Omni Core RegTest server setup and running")
class OmniCoreClientRegTestSpec extends Specification  {
    @Shared
    OmniCoreClient client

    def "can get mocked up combined btc/omni balances"() {
        given:
        Address address = OmniRegTestParams.get().exodusAddress

        when:
        WalletAddressBalance wab = client.balancesForAddressAsync(address).get()

        then:
        wab != null
    }

    @Ignore
    def "can get mocked up combined btc/omni balances for regTestMiningAddress"() {
        given:
        Address address = client.getRxOmniClient().getRegTestMiningAddress()

        when:
        WalletAddressBalance wab = client.balancesForAddressAsync(address).get()

        then:
        wab != null
    }

    @Ignore
    def "can get balances for a list of addresses"() {
        given:
        List<Address> addresses = client.getWalletAddresses()

        when:
        OmniJBalances balances = client.balancesForAddressesAsync(addresses).get()

        then:
        balances != null
    }

    @Ignore
    def "can use OmniProxy to get mocked up combined btc/omni balances for regTestMiningAddress"() {
        given:
        Address address = client.getRxOmniClient().getRegTestMiningAddress()

        when:
        WalletAddressBalance wab = client.getRxOmniClient().omniProxyGetBalance(address);

        then:
        wab != null
    }

    @Ignore
    def "can use OmniProxy to get mocked up combined btc/omni balances for multiple addresses"() {
        given:
        def addresses = ['miwk3gsiqDuGZdCzm6W5mibc3SSkAz6QbB', 'mzG1g5CUCfS6hPoH63Gt7y98RWDnHqTYTe'].stream()
                .map(str -> LegacyAddress.fromBase58(null, str))
                .collect(Collectors.toList())

        when:
        OmniJBalances ojb = client.getRxOmniClient().omniProxyGetBalances(addresses);

        then:
        ojb != null
    }

    def setup() {
        boolean testOmniProxy = false
        if (testOmniProxy) {
        client = new OmniCoreClient(RegTestParams.get(),
                URI.create("http://localhost:8080"),
                "",
                "")
        } else {
            client = new OmniCoreClient(RegTestParams.get(),
                    RpcURI.getDefaultRegTestURI(),
                    "bitcoinrpc",
                    "pass")
        }
    }
}
