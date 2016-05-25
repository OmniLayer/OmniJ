package foundation.omni.money

import foundation.omni.CurrencyID
import foundation.omni.Ecosystem
import spock.lang.Specification
import spock.lang.Unroll

import static foundation.omni.money.OmniCurrencyCode.*

/**
 *
 */
class OmniCurrencyCodeSpec extends Specification {
    def "test OMNI code"() {
        expect:
        OMNI.id() == CurrencyID.OMNI
        OMNI.id().ecosystem == Ecosystem.OMNI
        OMNI.name() == "OMNI"
        TOMNI.id() == CurrencyID.TOMNI
        TOMNI.id().ecosystem == Ecosystem.TOMNI
        TOMNI.name() == "TOMNI"
    }

    @Unroll
    def "convert from #code to #id"(code, id) {
        expect:
        codeToId(code) == id

        where:
        code                    | id
        "BTC"                   | CurrencyID.BTC
        "OMNI"                  | CurrencyID.OMNI
        "TOMNI"                 | CurrencyID.TOMNI
        "USDT"                  | CurrencyID.USDT
        "OMNI_SPT#100"          | new CurrencyID(100)
        "TOMNI_SPT#2147483737"  | new CurrencyID(2147483737)
    }

    @Unroll
    def "convert from #id to #code"(id, code) {
        expect:
        idToCodeString(id) == code

        where:
        id                          | code
        CurrencyID.BTC              | "BTC"
        CurrencyID.OMNI             | "OMNI"
        CurrencyID.TOMNI            | "TOMNI"
        CurrencyID.USDT             | "USDT"
        new CurrencyID(100)         | "OMNI_SPT#100"
        new CurrencyID(2147483737)  | "TOMNI_SPT#2147483737"
    }
}