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
 * because the Omni Specification provides a specific value for {@link DevOmni#SECONDS_PER_YEAR}.
 * This is why the {@link DevOmni#yearsSinceCrowdSale} function returns a {@link BigDecimal}.
 * <p>
 * See {@code calculate_and_update_devmsc()} in {@code omnicore.cpp}
 * <p>
 * TODO: Verify math versus Omni Core C++ implementation (regtest and/or functional test vs mainnet?)
 */
public class DevOmni {
    /**
     * Unix timestamp of End of the Exodus Crowd Sale as defined in the Omni Specification
     */
    public static final Instant EXODUS_END_SECS = Instant.ofEpochSecond(1_377_993_874);
    /**
     * Seconds-per-year used in Dev OMNI calculations as defined in the Omni Specification
     */
    public static final Duration SECONDS_PER_YEAR = Duration.ofSeconds(31_556_926);
    /**
     * The factor 2.0, the inverse of the 0.5 used in the {@code 1 - 0.5 ** y} calculation in the Omni Specification
     */
    public static final BigDecimal VESTING_FACTOR = BigDecimal.valueOf(2);
    /**
     * Total number of Dev OMNI (as specified by the {@code all_reward} constant in {@code omnicore.cpp})
     */
    public static final OmniDivisibleValue ALL_DEV_OMNI = OmniDivisibleValue.ofWilletts(56_316_235_762_22L);
    /**
     * APPROXIMATE Total number of OMNI issued in Crowdsale
     */
    public static final OmniDivisibleValue CROWDSALE_OMNI = OmniDivisibleValue.ofWilletts(563_202_059_032_29L);

    private static final BigDecimal secondsPerYearBD = BigDecimal.valueOf(SECONDS_PER_YEAR.getSeconds());
    private static final double vestingFactorDouble = VESTING_FACTOR.doubleValue();

    /**
     * @return Fractional years since end-of-Exodus based on system time
     * @param time end of time interval (e.g. timestamp of a particular block)
     */
    public static BigDecimal yearsSinceCrowdSale(Instant time) {
        BigDecimal secsSinceExodus = new BigDecimal(time.getEpochSecond() - EXODUS_END_SECS.getEpochSecond(), MathContext.UNLIMITED);
        return secsSinceExodus.divide(secondsPerYearBD, RoundingMode.HALF_UP);
    }

    /**
     * @return APPROXIMATE percent vested (possibly different from Omni Core calculations due to rounding)
     */
    public static BigDecimal percentVested(Instant time) {
        return BigDecimal.ONE.subtract(percentUnvested(time), MathContext.UNLIMITED);
    }

    /**
     * @return APPROXIMATE percent unvested (possibly different from Omni Core calculations due to rounding)
     */
    public static BigDecimal percentUnvested(Instant time) {
        BigDecimal divisor = unvestedDivisor(time);
        return BigDecimal.ONE.divide(divisor, MathContext.UNLIMITED);
    }

    /**
     * Calculate APPROXIMATE vested amount of Dev OMNI
     *
     * @param time Timestamp
     * @return Amount of vested OMNI
     */
    public static OmniDivisibleValue omniVested(Instant time) {
        return OmniDivisibleValue.ofWilletts(ALL_DEV_OMNI.willetts - omniUnvested(time).willetts);
    }

    /**
     * Calculate APPROXIMATE unvested amount of Dev OMNI
     *
     * @param time Timestamp
     * @return Amount of unvested OMNI
     */
    public static OmniDivisibleValue omniUnvested(Instant time) {
        BigDecimal divisor = unvestedDivisor(time);
        BigDecimal unvested = BigDecimal.valueOf(ALL_DEV_OMNI.willetts).divide(divisor, RoundingMode.HALF_UP);
        return OmniDivisibleValue.ofWilletts(unvested.longValue());
    }

    /**
     * @return APPROXIMATE total OMNI tokens at a given block time
     */
    public static OmniDivisibleValue totalOmniTokens(Instant time) {
        return CROWDSALE_OMNI.plus(omniVested(time));
    }
    
    /**
     * Calculate the APPROXIMATE denominator/divisor used in Dev OMNI/MSC calculations.
     * <p>
     * In some contexts the numerator may be a {@code 1} and in others it may be a
     * quantity of Dev OMNI.
     * <p>
     * TODO: Figure out how to divide by fractional years with more precision than double
     * @param time Timestamp used for vesting calculation
     * @return The denominator, n to use for calculating unvested percentage or amount
     */
    private static BigDecimal unvestedDivisor(Instant time) {
        // Built-in JDK math only allows `pow()` with fractional exponents for `double` type
        double years = yearsSinceCrowdSale(time).doubleValue();
        return BigDecimal.valueOf(Math.pow(vestingFactorDouble, years));
    }
}
