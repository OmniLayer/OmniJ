package foundation.omni

import spock.lang.Specification

import java.time.Instant

/**
 * Preliminary tests for DevOmni (aka Dev MSC) issuance
 */
class DevOmniSpec extends Specification {

    def "ALL DEV OMNI is correct value"() {
        expect:
        DevOmni.ALL_DEV_OMNI.willetts == 56_316_235_762_22L
    }

    // NOTE: This value has not been verified against the actual Crowdsale totals!!
    def "CROWDSALE_OMNI is correct value"() {
        expect:
        DevOmni.CROWDSALE_OMNI.willetts == 563_202_059_032_29L
    }

    def "verify dev OMNI unvested % after n years"(int years, BigDecimal expected) {
        when:
        Instant end = addYears(years)
        BigDecimal unvested = DevOmni.percentUnvested(end)

        then:
        unvested == expected

        where:
        years   | expected
        0       | 1.0
        1       | 0.50
        2       | 0.25
        3       | 0.125
        10      | 0.0009765625
        20      | 0.00000095367431640625
    }

    def "verify dev OMNI vesting after n years"(int years, BigDecimal vesting) {
        when:
        Instant end = addYears(years)
        BigDecimal vested = DevOmni.percentVested(end)

        then:
        vested == vesting

        where:
        years   | vesting
        0       | 0.0
        1       | 0.50
        2       | 0.75
        3       | 0.875
        10      | 0.9990234375
        20      | 0.99999904632568359375
    }
    
    def "verify dev OMNI vested after n years"(int years, long expectedVested) {
        when:
        Instant end = addYears(years)
        OmniDivisibleValue vested = DevOmni.omniVested(end)

        then:
        vested.willetts == expectedVested

        where:
        years   | expectedVested
        0       | 0.0
        1       | 0.5d * DevOmni.ALL_DEV_OMNI.willetts
        2       | 0.75d * DevOmni.ALL_DEV_OMNI.willetts
        3       | 0.875d * DevOmni.ALL_DEV_OMNI.willetts
        10      | 0.9990234375d * DevOmni.ALL_DEV_OMNI.willetts
        20      | 0.9999990463256836d * DevOmni.ALL_DEV_OMNI.willetts
    }

    def "verify OMNI vesting at timestamp"(int timestamp, BigDecimal vesting) {
        when:
        Instant end = Instant.ofEpochSecond(timestamp)
        BigDecimal vested = DevOmni.percentVested(end)

        then:
        vested == vesting

        where:
        block       | timestamp       | vesting
        714162      | 1639510432      | 0.99609375
        714163      | 1639512841      | 0.99609375
        714164      | 1639513378      | 0.99609375
        714165      | 1639513483      | 0.99609375
        714166      | 1639514670      | 0.99609375
    }

    def "verify total Dev OMNI at timestamp"(int timestamp, long vesting) {
        when:
        Instant end = Instant.ofEpochSecond(timestamp)
        long vested = DevOmni.omniVested(end).willetts

        then:
        vested == vesting

        where:
        block       | timestamp       | vesting
        714162      | 1639510432      | 56_096_250_466_27
        714163      | 1639512841      | 56_096_250_466_27
        714164      | 1639513378      | 56_096_250_466_27
        714165      | 1639513483      | 56_096_250_466_27
        714166      | 1639514670      | 56_096_250_466_27
    }

    def "verify total Tokens for timestamp"(int block, int timestamp, long totalTokens) {
        when:
        def calcTotal = DevOmni.totalOmniTokens(Instant.ofEpochSecond(timestamp))

        then:
        calcTotal.willetts == totalTokens

        where:
        block   | timestamp     | totalTokens
        714162  | 1639510432    | 619_298_309_498_56
        714163  | 1639512841    | 619_298_309_498_56
        714164  | 1639513378    | 619_298_309_498_56
        714165  | 1639513483    | 619_298_309_498_56
        714166  | 1639514670    | 619_298_309_498_56
     // 714167  | 1639510432    | 619_298_321_579_90 // This calculation doesn't handle time that goes backward (i.e. via high-water mark)
    }

    /**
     * Calculate timestamp using {@link DevOmni#SECONDS_PER_YEAR}
     * <p>
     * @param years years to add
     * @return epoch value
     */
    private static Instant addYears(int years) {
        return DevOmni.EXODUS_END_SECS.plusSeconds(years * DevOmni.SECONDS_PER_YEAR.getSeconds());
    }
}
