package foundation.omni.json.conversion

import foundation.omni.CurrencyID
import foundation.omni.rpc.BalanceEntry
import foundation.omni.rpc.PropertyBalanceEntries
import spock.lang.Unroll

/**
 *
 */
class PropertyBalanceEntriesDeserializerSpec extends BaseOmniClientMapperSpec {
    @Unroll
    def "fragment #fragment scans to #expectedResult"() {
        setup:
        def fragment = '[{"propertyid":1,"balance":"0.0","reserved":"0.0"}]'
        def expectedResult = new PropertyBalanceEntries()
        expectedResult.put(CurrencyID.OMNI, new BalanceEntry(0.divisible,0.divisible))

        when:
        def result = mapper.readValue(fragment, PropertyBalanceEntries.class)

        then:
        result == expectedResult
    }

}