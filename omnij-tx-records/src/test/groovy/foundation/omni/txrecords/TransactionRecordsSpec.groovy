package foundation.omni.txrecords

import foundation.omni.CurrencyID
import foundation.omni.OmniDivisibleValue
import foundation.omni.net.OmniMainNetParams
import foundation.omni.tx.Transactions
import spock.lang.Specification

/**
 *
 */
class TransactionRecordsSpec extends Specification {
    def "SimpleSend roundtrip"() {
        given:
        var address = OmniMainNetParams.get().exodusAddress
        var currencyID = CurrencyID.OMNI
        var amount = OmniDivisibleValue.of(123)

        when:
        TransactionRecords.SimpleSend txa = new TransactionRecords.SimpleSend(address, currencyID, amount)
        byte[] payload = txa.payload()

        and:
        TransactionRecords.SimpleSend txb = TransactionRecords.SimpleSend.of(payload, address)

        then:
        txa == txb
        payload == txb.payload()
    }

    def "SimpleSend roundtrip - 2"() {
        given:
        var address = OmniMainNetParams.get().exodusAddress
        var currencyID = CurrencyID.OMNI
        var amount = OmniDivisibleValue.of(123)

        when:
        TransactionRecords.SimpleSend txa = new TransactionRecords.SimpleSend(address, currencyID, amount)
        byte[] payload = txa.payload()

        and:
        Transactions.OmniTx txb = TransactionParser.parse(payload, address)

        then:
        txb instanceof Transactions.OmniRefTx
        txb instanceof TransactionRecords.SimpleSend
        txa == txb
        payload == txb.payload()
    }
}
