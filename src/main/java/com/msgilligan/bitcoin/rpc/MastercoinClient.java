package com.msgilligan.bitcoin.rpc;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * User: sean
 * Date: 6/16/14
 * Time: 5:34 PM
 */
public class MastercoinClient extends BitcoinClient {

    public MastercoinClient(URL server, String rpcuser, String rpcpassword) throws IOException {
        super(server, rpcuser, rpcpassword);
    }

    public BigDecimal getMPbalance(String address, Long currencyId) throws IOException {
        List<Object> params = Arrays.asList((Object) address, currencyId);
        Map<String, Object> response = send("getMPbalance", params);
        Double balanceBTCd = (Double) response.get("result");
        // Beware of the new BigDecimal(double d) constructor, it results in unexpected/undesired values.
        BigDecimal balanceBTC = BigDecimal.valueOf(balanceBTCd);
//        BigInteger balanceSatoshis = balanceBTC.multiply(D_SATOSHIS_PER_BITCOIN).toBigInteger();;
        return balanceBTC;
    }
}
