package foundation.omni.rest.omniwallet

import foundation.omni.CurrencyID
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
        balances[0].id == new CurrencyID(31)
        balances[0].value >= 0
        balances[1].symbol == "BTC"
        balances[1].id == CurrencyID.BTC
        balances[1].value.numberValue() >= 0
    }

    def "load exodusAddress balance"() {
        given:
        def client = new OmniwalletClient()

        when:
        def balances = client.balancesForAddress(exodusAddress)

        then:
        balances != null
        balances[0].symbol == "MSC"
        balances[0].id == CurrencyID.MSC
        balances[0].value.numberValue() >= 0
        balances[1].symbol == "TMSC"
        balances[1].id == CurrencyID.TMSC
        balances[1].value.numberValue() >= 0
        balances[2].symbol == "BTC"
        balances[2].id == CurrencyID.BTC
        balances[2].value.numberValue() >= 0
    }

    def "load balances of addresses with single address"() {
        given:
        def client = new OmniwalletClient()

        when:
        def balances = client.balancesForAddresses([testAddr])

        then:
        balances != null
        balances[testAddr][new CurrencyID(31)] >= 0
        balances[testAddr][CurrencyID.BTC].numberValue() >= 0
    }
}
