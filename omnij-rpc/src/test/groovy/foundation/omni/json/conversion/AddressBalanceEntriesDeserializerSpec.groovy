package foundation.omni.json.conversion

import com.fasterxml.jackson.databind.module.SimpleModule
import foundation.omni.CurrencyID
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
        setup:
        def fragment = '[{"address":"moneyqMan7uh8FqdCA2BV5yZ8qVrc9ikLP","balance":"0.0","reserved":"0.0"}]'
        def expectedResult = new AddressBalanceEntries()
        expectedResult.put(moneyMan, new BalanceEntry(0.0,0.0))

        when:
        def result = mapper.readValue(fragment, AddressBalanceEntries.class)

        then:
        result == expectedResult
    }

}