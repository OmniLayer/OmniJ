package foundation.omni;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;

/**
 * Work-in-progress constants and functions for calculating Dev OMNI.
 * <p>
 * We use {@link Instant} and {@link Duration} but do not use other Java Time functions
 * because the Omni Specification specifies a precise value for {@link DevOmni#SECONDS_PER_YEAR}.
 * This is why the "years since" functions return {@link BigDecimal}.
 * <p>
 * TODO: More precise calculation of percentVested than `double`?
 * TODO: Verify math versus Omni Core C++ implementation (regtest and/or functional test vs mainnet?)
 * TODO: Functions for calculating Dev Omni issued in a specific block (by block timestamp?)
 */
public class DevOmni {
    private static final BigDecimal TWO = BigDecimal.valueOf(2);
    /**
     * Unix timestamp of End of the Exodus Crowd Sale as defined in the Omni Specification
     */
    public static final Instant EXODUS_END_SECS = Instant.ofEpochSecond(1_377_993_874);
    /**
     * Seconds-per-year used in Dev OMNI calculations as defined in the Omni Specification
     */
    public static final Duration SECONDS_PER_YEAR = Duration.ofSeconds(31_556_926);
    /**
     * The factor 0.5, as used in the 1 - 0.5 ** y calculation in the Omni Specification
     */
    public static final BigDecimal VESTING_FACTOR = BigDecimal.ONE.divide(TWO, MathContext.UNLIMITED);

    /**
     * @return Fractional years since end-of-Exodus based on system time
     */
    public static BigDecimal yearsSinceCrowdSale() {
        return yearsSinceCrowdSale(Instant.now());
    }

    /**
     * @return Fractional years since end-of-Exodus based on system time
     * @param time end of time interval (e.g. timestamp of a particular block)
     */
    public static BigDecimal yearsSinceCrowdSale(Instant time) {
        BigDecimal secsSinceExodus = BigDecimal.valueOf(time.getEpochSecond() - EXODUS_END_SECS.getEpochSecond());
        BigDecimal secondsPerYear = BigDecimal.valueOf(SECONDS_PER_YEAR.getSeconds());
        return secsSinceExodus.divide(secondsPerYear, RoundingMode.HALF_UP);
    }

    /**
     * @return APPROXIMATE percent vested (possibly different from Omni Core calculations due to rounding)
     */
    public static double percentVested() {
        return percentVested(Instant.now());
    }

    /**
     * @return APPROXIMATE percent vested (possibly different from Omni Core calculations due to rounding)
     */
    public static double percentVested(Instant time) {
        return 1.0 - Math.pow(VESTING_FACTOR.doubleValue(), yearsSinceCrowdSale(time).doubleValue());
    }

    /**
     * Calculate timestamp using {@link DevOmni#SECONDS_PER_YEAR}
     * <p>
     * (package-private for use internally and in tests)
     * @param years years to add
     * @return epoch value
     */
    static Instant addYears(int years) {
        return EXODUS_END_SECS.plusSeconds(years * SECONDS_PER_YEAR.getSeconds());
    }
}
