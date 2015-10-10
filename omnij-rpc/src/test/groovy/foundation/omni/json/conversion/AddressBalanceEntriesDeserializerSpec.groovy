package foundation.omni.json.conversion

import com.fasterxml.jackson.databind.module.SimpleModule
import foundation.omni.CurrencyID
import foundation.omni.OmniDivisibleValue
import foundation.omni.OmniValue
import foundation.omni.rpc.AddressBalanceEntries
import foundation.omni.rpc.BalanceEntry
import org.bitcoinj.core.Address
import spock.lang.Unroll

/**
 *
 */
class AddressBalanceEntriesDeserializerSpec extends BaseOmniClientMapperSpec {
    static final Address moneyMan = new Address(null, "moneyqMan7uh8FqdCA2BV5yZ8qVrc9ikLP")

    @Unroll
    def "fragment #fragment scans to #expectedResult"() {
        when:
        def result = mapper.readValue(fragment, AddressBalanceEntries.class)

        then:
        result == expectedResult

        where:
        fragment                                                                                | expectedResult
        '[{"address":"moneyqMan7uh8FqdCA2BV5yZ8qVrc9ikLP","balance":"0.0","reserved":"0.0"}]'   | [:]
        '[{"address":"moneyqMan7uh8FqdCA2BV5yZ8qVrc9ikLP","balance":"1.0","reserved":"0.0"}]'   | [(moneyMan): new BalanceEntry(1.divisible,0.divisible)]
        '[{"address":"moneyqMan7uh8FqdCA2BV5yZ8qVrc9ikLP","balance":"0.0","reserved":"1.0"}]'   | [(moneyMan): new BalanceEntry(0.divisible,1.divisible)]
        '[{"address":"moneyqMan7uh8FqdCA2BV5yZ8qVrc9ikLP","balance":"1.0","reserved":"1.0"}]'   | [(moneyMan): new BalanceEntry(1.divisible,1.divisible)]
        '[{"address":"moneyqMan7uh8FqdCA2BV5yZ8qVrc9ikLP","balance":"0","reserved":"0"}]'       | [:]
        '[{"address":"moneyqMan7uh8FqdCA2BV5yZ8qVrc9ikLP","balance":"1","reserved":"0"}]'       | [(moneyMan): new BalanceEntry(1.indivisible,0.indivisible)]
        '[{"address":"moneyqMan7uh8FqdCA2BV5yZ8qVrc9ikLP","balance":"0","reserved":"1"}]'       | [(moneyMan): new BalanceEntry(0.indivisible,1.indivisible)]
        '[{"address":"moneyqMan7uh8FqdCA2BV5yZ8qVrc9ikLP","balance":"1","reserved":"1"}]'       | [(moneyMan): new BalanceEntry(1.indivisible,1.indivisible)]
    }

}