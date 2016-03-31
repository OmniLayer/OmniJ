package foundation.omni.rest.omniwallet

import static foundation.omni.CurrencyID.*
import foundation.omni.net.OmniMainNetParams
import org.bitcoinj.core.Address
import spock.lang.Specification


/**
 *
 */
class OmniwalletClientSpec extends Specification {
    final Address exodusAddress = OmniMainNetParams.get().exodusAddress;
    final Address testAddr = new Address(null, "19ZbcHED8F6u5Wr5gp97KMVNvKV8HUrmeu")

    def "load testAddr balance"() {
        given:
        def client = new OmniwalletClient()

        when:
        def balances = client.balancesForAddress(testAddr)

        then:
        balances != null
        balances[0].symbol == "SP31"
        balances[0].id == USDT
        balances[0].value >= 0
        balances[1].symbol == "BTC"
        balances[1].id == BTC
        balances[1].value.numberValue() >= 0
    }

    def "load exodusAddress balance"() {
        given:
        def client = new OmniwalletClient()

        when:
        def balances = client.balancesForAddress(exodusAddress)

        then:
        balances != null
        balances[0].symbol == "BTC"
        balances[0].id == BTC
        balances[0].value.numberValue() >= 0
        balances[1].symbol == "MSC"
        balances[1].id == MSC
        balances[1].value.numberValue() >= 0
        balances[2].symbol == "TMSC"
        balances[2].id == TMSC
        balances[2].value.numberValue() >= 0
    }

    def "load balances of addresses with single address"() {
        given:
        def client = new OmniwalletClient()

        when:
        def balances = client.balancesForAddresses([testAddr])

        then:
        balances != null
        balances[testAddr][USDT].numberValue() >= 0
        balances[testAddr][BTC].numberValue() >= 0
    }

    def "load balances of addresses with multiple addresses"() {
        given:
        def client = new OmniwalletClient()

        when:
        def balances = client.balancesForAddresses([testAddr, exodusAddress])

        then:
        balances != null
        balances[testAddr][USDT].numberValue() >= 0
        balances[testAddr][BTC].numberValue() >= 0
        balances[exodusAddress][MSC].numberValue() >= 0
        balances[exodusAddress][TMSC].numberValue() >= 0
        balances[exodusAddress][BTC].numberValue() >= 0
    }
}
