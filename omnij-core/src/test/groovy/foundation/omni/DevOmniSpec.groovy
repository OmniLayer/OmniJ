package foundation.omni

import spock.lang.Specification

import java.time.Instant

/**
 *
 */
class DevOmniSpec extends Specification {

    def "verify dev OMNI vesting after n years"(int years, BigDecimal vesting) {
        when:
        Instant end = DevOmni.addYears(years)
        double vested = DevOmni.percentVested(end)

        then:
        vested == vesting.doubleValue()

        where:
        years   | vesting
        0       | 0.0
        1       | 0.50
        2       | 0.75
        3       | 0.875
        10      | 0.9990234375
        20      | 0.9999990463256836
    }
}
