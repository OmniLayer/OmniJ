package foundation.omni.netapi.omnicore

import foundation.omni.CurrencyID
import foundation.omni.net.OmniRegTestParams
import foundation.omni.netapi.OmniJBalances
import foundation.omni.netapi.WalletAddressBalance
import org.bitcoinj.core.Address
import org.bitcoinj.params.RegTestParams
import org.consensusj.bitcoin.rpc.RpcURI
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

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

    def "can get mocked up combined btc/omni balances for regTestMiningAddress"() {
        given:
        Address address = client.getRxOmniClient().getRegTestMiningAddress()

        when:
        WalletAddressBalance wab = client.balancesForAddressAsync(address).get()

        then:
        wab != null
    }

    def "can get balances for a list of addresses"() {
        given:
        List<Address> addresses = client.getWalletAddresses()

        when:
        OmniJBalances balances = client.balancesForAddressesAsync(addresses).get()

        then:
        balances != null

        cleanup:
        balances.forEach((address, wab) -> {
            def btc = wab.get(CurrencyID.BTC)
            if (btc != null && (btc.balance.willetts > 0 || btc.reserved.willetts > 0)) {
               wab.forEach((id, entry) -> {
                    println("$id: $entry")
               })
            }
        })
    }

    def setup() {
        client = new OmniCoreClient(RegTestParams.get(),
                RpcURI.getDefaultRegTestURI(),
                "bitcoinrpc",
                "pass")
    }
}
