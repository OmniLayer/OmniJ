package com.msgilligan.bitcoin.rpc;


import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * User: sean
 * Date: 6/15/14
 * Time: 10:03 AM
 */
public class BitcoinClient extends RPCClient {

    private static final Integer SECOND = 1000;
    public static final BigInteger SATOSHIS_PER_BITCOIN = new BigInteger("100000000", 10);
    public static final BigDecimal D_SATOSHIS_PER_BITCOIN = new BigDecimal(SATOSHIS_PER_BITCOIN);

    public BitcoinClient(URL server, String rpcuser, String rpcpassword) throws IOException {
        super(server, rpcuser, rpcpassword);
    }

    public BitcoinClient(RPCConfig config) throws IOException {
        super(config);
    }

    /**
     *
     * @param timeout Timeout in seconds
     * @return
     */
    public Boolean waitForServer(Integer timeout) {
        Integer seconds = 0;

        System.out.println("Waiting for server RPC ready...");

        Integer block;

        while ( seconds < timeout ) {
            try {
                block = this.getBlockCount();
                if (block != null ) {
                    System.out.println("\nRPC Ready.");
                    return true;
                }
            } catch (SocketException se ) {
                // These are expected exceptions while waiting for a server
                if (! ( se.getMessage().equals("Unexpected end of file from server") ||
                        se.getMessage().equals("Connection reset") ||
                        se.getMessage().equals("Connection refused") ||
                        se.getMessage().equals("recvfrom failed: ECONNRESET (Connection reset by peer)"))) {
                    se.printStackTrace();
                }

            } catch (java.io.EOFException e) {
                /* Android exception, ignore */
                // Expected exceptions on Android, RoboVM
            }
            catch(IOException e) {
                e.printStackTrace();
            }
            try {
                System.out.print(".");
                seconds++;
                if (seconds % 60 == 0) {
                    System.out.println();
                }
                Thread.sleep(SECOND);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     *
     * @param timeout Timeout in seconds
     * @return
     */
    public Boolean waitForSync(Long blockCount, Integer timeout) throws IOException {
        Integer seconds = 0;

        System.out.println("Waiting for server to get to block " +  blockCount);

        Integer block;

        while ( seconds < timeout ) {
            block = this.getBlockCount();
            if (block >= blockCount ) {
                return true;
            } else {
                try {
                    seconds++;
                    if (seconds % 60 == 0) {
                        System.out.println("block " + block);
                    }
                    Thread.sleep(SECOND);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
        return false;
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

    public void generateBlocks(Long blocks) throws IOException {
        setGenerate(true, blocks);
    }

    public String getNewAddress() throws IOException {
        Map<String, Object> response = send("getnewaddress", null);

        String address = (String) response.get("result");
        return address;
    }

    public Object getRawTransaction(String txid, Boolean verbose) throws IOException {
        List<Object> params = new ArrayList<Object>();
        params.add(txid);
        if (verbose != null) {
            params.add(verbose);
        }
        Map<String, Object> response = send("getrawtransaction", params);

        @SuppressWarnings("unchecked")
        String hexEncoded = (String) response.get("result");
        byte[] raw = BitcoinClient.hexStringToByteArray(hexEncoded);
        return raw;

    }

    public BigDecimal getReceivedByAddress(String address) throws IOException {
        return getReceivedByAddress(address, 1);   // Default to 1 or more confirmations
    }

    public BigDecimal getReceivedByAddress(String address, Integer minConf) throws IOException {
        List<Object> params = Arrays.asList((Object) address, minConf);
        Map<String, Object> response = send("getreceivedbyaddress", params);
        BigDecimal balance = new BigDecimal((Double) response.get("result"));
        return balance;
    }

    public List<Object> listReceivedByAddress(Integer minConf, Boolean includeEmpty ) throws IOException {
        List<Object> params = Arrays.asList((Object) minConf, includeEmpty);

        Map<String, Object> response = send("listreceivedbyaddress", params);

        @SuppressWarnings("unchecked")
        List<Object> addresses = (List<Object>) response.get("result");
        return addresses;
    }

    public List<Object> listUnspent(Integer minConf, Integer maxConf) throws IOException {
        List<Object> params = new ArrayList<Object>();
        if (minConf != null) {
            params.add(minConf);
            if (maxConf != null) {
                params.add(maxConf);
            }
        }
        Map<String, Object> response = send("listunspent", params);

        @SuppressWarnings("unchecked")
        List<Object> unspent = (List<Object>) response.get("result");
        return unspent;
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

        @SuppressWarnings("unchecked")
        Map<String, Object> transaction = (Map<String, Object>) response.get("result");
        return transaction;
    }

    public Map<String, Object> getInfo() throws IOException {
        Map<String, Object> response = send("getinfo", null);

        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) response.get("result");
        return result;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

}
