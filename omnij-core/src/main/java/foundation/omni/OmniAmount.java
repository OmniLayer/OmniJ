package foundation.omni;

import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;
import javax.money.MonetaryAmountFactory;
import javax.money.MonetaryContext;
import javax.money.MonetaryOperator;
import javax.money.MonetaryQuery;
import javax.money.NumberValue;

/**
 * Omni Amount: A number of coins/tokens with a specified CurrencyID
 *
 * Placeholder: Do not use - not ready yet!
 *
 * Note: Maybe only OmniValue will stay in omnij-core and OmniAmount
 * will live in a separate JAR.
 *
 */
public class OmniAmount implements MonetaryAmount {
    private final OmniValue value;
    private final CurrencyID currencyID;

    public OmniAmount(OmniValue value, CurrencyID currencyID) {
        this.value = value;
        this.currencyID = currencyID;
    }

    @Override
    public MonetaryContext getContext() {
        return null;
    }

    @Override
    public <R> R query(MonetaryQuery<R> query) {
        return null;
    }

    @Override
    public MonetaryAmount with(MonetaryOperator operator) {
        return null;
    }

    @Override
    public MonetaryAmountFactory<? extends MonetaryAmount> getFactory() {
        return null;
    }

    @Override
    public boolean isGreaterThan(MonetaryAmount amount) {
        return false;
    }

    @Override
    public boolean isGreaterThanOrEqualTo(MonetaryAmount amount) {
        return false;
    }

    @Override
    public boolean isLessThan(MonetaryAmount amount) {
        return false;
    }

    @Override
    public boolean isLessThanOrEqualTo(MonetaryAmount amt) {
        return false;
    }

    @Override
    public boolean isEqualTo(MonetaryAmount amount) {
        return false;
    }

    @Override
    public boolean isNegative() {
        return false;
    }

    @Override
    public boolean isNegativeOrZero() {
        return false;
    }

    @Override
    public boolean isPositive() {
        return false;
    }

    @Override
    public boolean isPositiveOrZero() {
        return false;
    }

    @Override
    public boolean isZero() {
        return false;
    }

    @Override
    public int signum() {
        return 0;
    }

    @Override
    public MonetaryAmount add(MonetaryAmount amount) {
        return null;
    }

    @Override
    public MonetaryAmount subtract(MonetaryAmount amount) {
        return null;
    }

    @Override
    public MonetaryAmount multiply(long multiplicand) {
        return null;
    }

    @Override
    public MonetaryAmount multiply(double multiplicand) {
        return null;
    }

    @Override
    public MonetaryAmount multiply(Number multiplicand) {
        return null;
    }

    @Override
    public MonetaryAmount divide(long divisor) {
        return null;
    }

    @Override
    public MonetaryAmount divide(double divisor) {
        return null;
    }

    @Override
    public MonetaryAmount divide(Number divisor) {
        return null;
    }

    @Override
    public MonetaryAmount remainder(long divisor) {
        return null;
    }

    @Override
    public MonetaryAmount remainder(double divisor) {
        return null;
    }

    @Override
    public MonetaryAmount remainder(Number divisor) {
        return null;
    }

    @Override
    public MonetaryAmount[] divideAndRemainder(long divisor) {
        return new MonetaryAmount[0];
    }

    @Override
    public MonetaryAmount[] divideAndRemainder(double divisor) {
        return new MonetaryAmount[0];
    }

    @Override
    public MonetaryAmount[] divideAndRemainder(Number divisor) {
        return new MonetaryAmount[0];
    }

    @Override
    public MonetaryAmount divideToIntegralValue(long divisor) {
        return null;
    }

    @Override
    public MonetaryAmount divideToIntegralValue(double divisor) {
        return null;
    }

    @Override
    public MonetaryAmount divideToIntegralValue(Number divisor) {
        return null;
    }

    @Override
    public MonetaryAmount scaleByPowerOfTen(int power) {
        return null;
    }

    @Override
    public MonetaryAmount abs() {
        return null;
    }

    @Override
    public MonetaryAmount negate() {
        return null;
    }

    @Override
    public MonetaryAmount plus() {
        return null;
    }

    @Override
    public MonetaryAmount stripTrailingZeros() {
        return null;
    }

    @Override
    public int compareTo(MonetaryAmount o) {
        return 0;
    }

    @Override
    public CurrencyUnit getCurrency() {
        return null;
    }

    @Override
    public NumberValue getNumber() {
        return value;
    }
}
