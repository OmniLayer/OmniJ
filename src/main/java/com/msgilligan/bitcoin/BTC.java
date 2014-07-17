package com.msgilligan.bitcoin;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

/**
 * User: sean
 * Date: 7/16/14
 * Time: 11:12 AM
 */
public class BTC  {
    public static final long satoshisPerBitcoin = 100000000;
    public static final BigInteger satoshisPerBTC = BigInteger.valueOf(satoshisPerBitcoin);
    public static final BigDecimal satoshisPerBTCDecimal = new BigDecimal(satoshisPerBTC);

    public static BigDecimal satoshisToBTC(BigInteger satoshis) {
        BigDecimal decimalSatoshis = new BigDecimal(satoshis);
        return decimalSatoshis.divide(satoshisPerBTCDecimal);
    }
    public static BigInteger btcToSatoshis(BigDecimal btc) {
        BigDecimal satoshisDecimal = btc.multiply(BTC.satoshisPerBTCDecimal);
        return satoshisDecimal.toBigInteger();
    }
}
