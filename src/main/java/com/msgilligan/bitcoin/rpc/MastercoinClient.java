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

    public String send_MP(String fromAddress, String toAddress, Long currencyId, BigDecimal amount) throws IOException {
        List<Object> params = Arrays.asList((Object) fromAddress, toAddress, currencyId, amount);
        Map<String, Object> response = send("send_MP", params);
        String txid = (String) response.get("result");
        return txid;
    }

    public BigDecimal getbalance_MP(String address, Long currencyId) throws IOException {
        List<Object> params = Arrays.asList((Object) address, currencyId);
        Map<String, Object> response = send("getbalance_MP", params);
        Double balanceBTCd = (Double) response.get("result");
        // Beware of the new BigDecimal(double d) constructor, it results in unexpected/undesired values.
        BigDecimal balanceBTC = BigDecimal.valueOf(balanceBTCd);
        return balanceBTC;
    }
}
