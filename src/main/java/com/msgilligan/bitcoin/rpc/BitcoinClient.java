package com.msgilligan.bitcoin.rpc;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: sean
 * Date: 6/15/14
 * Time: 10:03 AM
 */
public class BitcoinClient extends RPCClient {

    public static final BigInteger SATOSHIS_PER_BITCOIN = new BigInteger("100000000", 10);
    public static final BigDecimal D_SATOSHIS_PER_BITCOIN = new BigDecimal(SATOSHIS_PER_BITCOIN);

    public BitcoinClient(URL server, String rpcuser, String rpcpassword) throws IOException {
        super(server, rpcuser, rpcpassword);
    }

    /**
     *
     * @return
     * @throws IOException
     */
    public Integer getBlockCount() throws IOException {
        Map<String, Object> response = send("getblockcount", null);

        Integer blockCount = (Integer) response.get("result");
        return blockCount;
    }

    /**
     *
     * @param generate        turn generation on or off
     * @param genproclimit    Generation is limited to [genproclimit] processors, -1 is unlimited
     *                        in regtest mode genproclimit is number of blocks to generate immediately
     * @throws IOException
     */
    public void setGenerate(Boolean generate, Long genproclimit) throws IOException {
        List<Object> params = Arrays.asList((Object) generate, genproclimit);

        Map<String, Object> response = send("setgenerate", params);


        String result = (String) response.get("result");
        assert result == null;
    }

    public String getNewAddress() throws IOException {
        Map<String, Object> response = send("getnewaddress", null);

        String address = (String) response.get("result");
        return address;
    }

    public BigDecimal getReceivedByAddress(String address, Integer minConf) throws IOException {
        if (minConf == null) minConf = 1;
        List<Object> params = Arrays.asList((Object) address, minConf);
        Map<String, Object> response = send("getreceivedbyaddress", params);
        BigDecimal balance = new BigDecimal((Double) response.get("result"));
        return balance;
    }

    public List<Object> listReceivedByAddress(Integer minConf, Boolean includeEmpty ) throws IOException {
        List<Object> params = Arrays.asList((Object) minConf, includeEmpty);

        Map<String, Object> response = send("listreceivedbyaddress", params);

        List<Object> addresses = (List<Object>) response.get("result");
        return addresses;
    }

    public BigDecimal getBalance(String account, Long minConf ) throws IOException {
//        List<Object> params = Arrays.asList((Object) account, minConf);

        Map<String, Object> response = send("getbalance", null);
        Double balanceBTCd = (Double) response.get("result");
        // Beware of the new BigDecimal(double d) constructor, it results in unexpected/undesired values.
        BigDecimal balanceBTC = BigDecimal.valueOf(balanceBTCd);
//        BigInteger balanceSatoshis = balanceBTC.multiply(D_SATOSHIS_PER_BITCOIN).toBigInteger();;
        return balanceBTC;
    }

    public String sendToAddress(String address, BigDecimal amount, String comment, String commentTo) throws IOException {
        List<Object> params = Arrays.asList((Object) address, amount, comment, commentTo);

        Map<String, Object> response = send("sendtoaddress", params);

        String txid = (String) response.get("result");
        return txid;

    }

    public Map<String, Object> getTransaction(String txid) throws IOException {
        List<Object> params = Arrays.asList((Object) txid);
        Map<String, Object> response = send("gettransaction", params);

        Map<String, Object> transaction = (Map<String, Object>) response.get("result");
        return transaction;
    }



}
