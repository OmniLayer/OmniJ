package com.msgilligan.bitcoin.rpc;


import com.google.bitcoin.core.Address;
import com.google.bitcoin.core.AddressFormatException;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.core.Sha256Hash;
import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.params.RegTestParams;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.List;
import java.util.Map;

/**
 * JSON-RPC Client for bitcoind
 */
public class BitcoinClient extends RPCClient {

    private static final Integer SECOND = 1000;

    public BitcoinClient(URL server, String rpcuser, String rpcpassword) {
        super(server, rpcuser, rpcpassword);
    }

    public BitcoinClient(RPCConfig config) throws IOException {
        this(config.getUrl(), config.getUsername(), config.getPassword());
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
        List<Object> params = createParamList(generate, genproclimit);

        Map<String, Object> response = send("setgenerate", params);


        String result = (String) response.get("result");
        assert result == null;
    }

    public void generateBlock() throws IOException {
        generateBlocks(1L);
    }

    public void generateBlocks(Long blocks) throws IOException {
        setGenerate(true, blocks);
    }

    public Address getNewAddress() throws IOException {
        Map<String, Object> response = send("getnewaddress", null);

        String addr = (String) response.get("result");
        Address address = null;
        try {
            address = new Address(null, addr);
        } catch (AddressFormatException e) {
            throw new RuntimeException(e);
        }
        return address;
    }

    public Address getAccountAddress(String account) throws IOException {
        List<Object> params = createParamList(account);
        Map<String, Object> response = send("getaccountaddress", params);
        @SuppressWarnings("unchecked")
        String addr = (String) response.get("result");
        Address address = null;
        try {
            address = new Address(null, addr);
        } catch (AddressFormatException e) {
            throw new RuntimeException(e);
        }
        return address;
    }

    public Boolean move(Address fromaccount, Address toaccount, BigDecimal amount) throws IOException {
        List<Object> params = createParamList(fromaccount, toaccount, amount);
        Map<String, Object> response = send("move", params);
        @SuppressWarnings("unchecked")
        Boolean result = (Boolean) response.get("result");
        return result;
    }

    public Object getRawTransaction(Sha256Hash txid, Boolean verbose) throws IOException {
        Object result;
        if (verbose) {
            result = getRawTransactionMap(txid);    // Verbose means JSON
        } else {
            result = getRawTransactionBytes(txid);  // Not-verbose is Binary
        }
        return result;
    }

    /* Return a BitcoinJ Transaction type */
    public Transaction getRawTransaction(Sha256Hash txid) throws IOException {
        byte[] raw = getRawTransactionBytes(txid);
        // Hard-code RegTest for now
        // TODO: All RPC client connections should have a BitcoinJ params object?
        Transaction tx = new Transaction(RegTestParams.get(), raw);
        return tx;
    }

    public byte[] getRawTransactionBytes(Sha256Hash txid) throws IOException {
        List<Object> params = createParamList(txid.toString());
        Map<String, Object> response = send("getrawtransaction", params);

        @SuppressWarnings("unchecked")
        String hexEncoded = (String) response.get("result");
        byte[] raw = BitcoinClient.hexStringToByteArray(hexEncoded);
        return raw;
    }

    /* TODO: Return a stronger type than an a Map? */
    public Map<String, Object> getRawTransactionMap(Sha256Hash txid) throws IOException {
        List<Object> params = createParamList(txid, 1);
        Map<String, Object> response = send("getrawtransaction", params);

        @SuppressWarnings("unchecked")
        Map<String, Object> json = (Map<String, Object>) response.get("result");
        return json;
    }

    public Sha256Hash sendRawTransaction(Transaction tx) throws IOException {
        return sendRawTransaction(tx, null);
    }

    public Sha256Hash sendRawTransaction(Transaction tx, Boolean allowHighFees) throws IOException {
        String hexTx = transactionToHex(tx);
        List<Object> params = createParamList(hexTx, allowHighFees);
        Map<String, Object> response = send("sendrawtransaction", params);

        String txid = (String) response.get("result");
        Sha256Hash hash = new Sha256Hash(txid);
        return hash;
    }

    public BigDecimal getReceivedByAddress(Address address) throws IOException {
        return getReceivedByAddress(address, 1);   // Default to 1 or more confirmations
    }

    public BigDecimal getReceivedByAddress(Address address, Integer minConf) throws IOException {
        List<Object> params = createParamList(address.toString(), minConf);
        Map<String, Object> response = send("getreceivedbyaddress", params);
        BigDecimal balance = BigDecimal.valueOf((Double) response.get("result"));
        return balance;
    }

    public List<Object> listReceivedByAddress(Integer minConf, Boolean includeEmpty ) throws IOException {
        List<Object> params = createParamList(minConf, includeEmpty);

        Map<String, Object> response = send("listreceivedbyaddress", params);

        @SuppressWarnings("unchecked")
        List<Object> addresses = (List<Object>) response.get("result");
        return addresses;
    }

    public List<Object> listUnspent() throws IOException {
        return listUnspent(null, null);
    }

    public List<Object> listUnspent(Integer minConf, Integer maxConf) throws IOException {
        List<Object> params = createParamList(minConf, maxConf);
        Map<String, Object> response = send("listunspent", params);

        @SuppressWarnings("unchecked")
        List<Object> unspent = (List<Object>) response.get("result");
        return unspent;
    }
    public BigDecimal getBalance() throws IOException {
        return getBalance(null, null);
    }

    public BigDecimal getBalance(String account) throws IOException {
        return getBalance(account, null);
    }

    public BigDecimal getBalance(String account, Integer minConf) throws IOException {
        List<Object> params = createParamList(account, minConf);
        Map<String, Object> response = send("getbalance", params);
        Double balanceBTCd = (Double) response.get("result");
        // Beware of the new BigDecimal(double d) constructor, it results in unexpected/undesired values.
        BigDecimal balanceBTC = BigDecimal.valueOf(balanceBTCd);
        return balanceBTC;
    }

    public Sha256Hash sendToAddress(Address address, BigDecimal amount) throws IOException {
        return sendToAddress(address, amount, null, null);
    }

    public Sha256Hash sendToAddress(Address address, BigDecimal amount, String comment, String commentTo) throws IOException {
        List<Object> params = createParamList(address.toString(), amount, comment, commentTo);

        Map<String, Object> response = send("sendtoaddress", params);

        String txid = (String) response.get("result");
        Sha256Hash hash = new Sha256Hash(txid);
        return hash;
    }

    public Sha256Hash sendFrom(String account, Address address, BigDecimal amount) throws IOException {
        List<Object> params = createParamList(account, address.toString(), amount);

        Map<String, Object> response = send("sendfrom", params);

        String txid = (String) response.get("result");
        Sha256Hash hash = new Sha256Hash(txid);
        return hash;
    }

    public Sha256Hash sendMany(String account, Map<Address, BigDecimal> amounts) throws IOException {
        List<Object> params = Arrays.asList(account, amounts);

        Map<String, Object> response = send("sendmany", params);

        String txid = (String) response.get("result");
        Sha256Hash hash = new Sha256Hash(txid);
        return hash;
    }

    public Map<String, Object> getTransaction(Sha256Hash txid) throws IOException {
        List<Object> params = createParamList(txid.toString());
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

    /*
     * Create a mutable param list (so send() can remove null parameters)
     */
    private List<Object> createParamList(Object... parameters) {
        List<Object> paramList = new ArrayList<Object>(Arrays.asList(parameters));
        return paramList;
    }

    private String transactionToHex(Transaction tx) {
        // From: http://bitcoin.stackexchange.com/questions/8475/how-to-get-hex-string-from-transaction-in-bitcoinj
        final StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb);
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            tx.bitcoinSerialize(os);
            byte[] bytes = os.toByteArray();
            for (byte b : bytes) {
                formatter.format("%02x", b);
            }

        } catch (IOException e) {
            throw new RuntimeException("Can't convert Transaction to Hex String", e);
        } finally {
            formatter.close();
        }
        return sb.toString();
    }

}
