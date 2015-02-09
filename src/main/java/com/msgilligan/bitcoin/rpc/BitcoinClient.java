package com.msgilligan.bitcoin.rpc;


import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.params.RegTestParams;

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
            } catch (JsonRPCException e) {
                e.printStackTrace();
            } catch (IOException e) {
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
    public Boolean waitForSync(Long blockCount, Integer timeout) throws JsonRPCException, IOException {
        Integer seconds = 0;

        System.out.println("Waiting for server to get to block " +  blockCount);

        Integer block;

        while ( seconds < timeout ) {
            block = this.getBlockCount();
            if (block >= blockCount ) {
                System.out.println("Server is at block " +  block + " returning 'true'.");
                return true;
            } else {
                try {
                    seconds++;
                    if (seconds % 60 == 0) {
                        System.out.println("Server at block " + block);
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
    public Integer getBlockCount() throws JsonRPCException, IOException {
        Map<String, Object> response = send("getblockcount", null);

        Integer blockCount = (Integer) response.get("result");
        return blockCount;
    }

    /**
     * Returns the hash of block in best-block-chain at index provided.
     *
     * @param index The block index
     * @return The block hash
     */
    public Sha256Hash getBlockHash(Integer index) throws JsonRPCException, IOException {
        List<Object> params = createParamList(index);
        Map<String, Object> response = send("getblockhash", params);

        String hashStr = (String) response.get("result");
        Sha256Hash hash = new Sha256Hash(hashStr);
        return hash;
    }

    /**
     * Returns information about a block with the given block hash.
     *
     * @param hash The block hash
     * @return The information about the block
     */
    public Map<String,Object> getBlock(Sha256Hash hash) throws JsonRPCException, IOException {
        // Use "verbose = true"
        List<Object> params = createParamList(hash.toString(), true);
        Map<String, Object> response = send("getblock", params);

        @SuppressWarnings("unchecked")
        Map<String, Object> json = (Map<String, Object>) response.get("result");
        return json;
    }

    /**
     * Returns information about a block at index provided.
     *
     * @param index The block index
     * @return The information about the block
     */
    public Map<String,Object> getBlock(Integer index) throws JsonRPCException, IOException {
        Sha256Hash blockHash = getBlockHash(index);
        return getBlock(blockHash);
    }

    /**
     *
     * @param generate        turn generation on or off
     * @param genproclimit    Generation is limited to [genproclimit] processors, -1 is unlimited
     *                        in regtest mode genproclimit is number of blocks to generate immediately
     * @throws IOException
     */
    public void setGenerate(Boolean generate, Long genproclimit) throws JsonRPCException, IOException {
        List<Object> params = createParamList(generate, genproclimit);

        Map<String, Object> response = send("setgenerate", params);


        String result = (String) response.get("result");
        assert result == null;
    }

    public void generateBlock() throws JsonRPCException, IOException {
        generateBlocks(1L);
    }

    public void generateBlocks(Long blocks) throws JsonRPCException, IOException {
        setGenerate(true, blocks);
    }

    public Address getNewAddress() throws JsonRPCException, IOException {
        return getNewAddress(null);
    }

    public Address getNewAddress(String account) throws JsonRPCException, IOException {
        List<Object> params = createParamList(account);
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

    public Address getAccountAddress(String account) throws JsonRPCException, IOException {
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

    public Boolean moveFunds(Address fromaccount, Address toaccount, BigDecimal amount, Integer minconf, String comment) throws JsonRPCException, IOException {
        List<Object> params = createParamList(fromaccount, toaccount, amount, minconf, comment);
        Map<String, Object> response = send("move", params);
        @SuppressWarnings("unchecked")
        Boolean result = (Boolean) response.get("result");
        return result;
    }

    /**
     * Creates a raw transaction spending the given inputs to the given destinations.
     *
     * Note: the transaction inputs are not signed, and the transaction is not stored in the wallet or transmitted to
     * the network.
     *
     * @param inputs  The outpoints to spent
     * @param outputs The destinations and amounts to transfer
     * @return The hex-encoded raw transaction
     * @throws JsonRPCException
     * @throws IOException
     */
    public String createRawTransaction(List<Object> inputs, Map<Address, BigDecimal> outputs)
            throws JsonRPCException, IOException {
        List<Object> params = Arrays.asList(inputs, outputs);
        Map<String, Object> response = send("createrawtransaction", params);

        String transactionHex = (String) response.get("result");
        return transactionHex;
    }

    /**
     * Signs inputs of a raw transaction.
     *
     * @param unsignedTransaction The hex-encoded raw transaction
     * @return The signed transaction and information whether it has a complete set of signature
     * @throws IOException
     * @throws JsonRPCException
     */
    public Map<String, Object> signRawTransaction(String unsignedTransaction) throws IOException, JsonRPCException {
        List<Object> params = createParamList(unsignedTransaction);
        Map<String, Object> response = send("signrawtransaction", params);

        @SuppressWarnings("unchecked")
        Map<String, Object> signedTransaction = (Map<String, Object>) response.get("result");
        return signedTransaction;
    }

    public Object getRawTransaction(Sha256Hash txid, Boolean verbose) throws JsonRPCException, IOException {
        Object result;
        if (verbose) {
            result = getRawTransactionMap(txid);    // Verbose means JSON
        } else {
            result = getRawTransactionBytes(txid);  // Not-verbose is Binary
        }
        return result;
    }

    /* Return a BitcoinJ Transaction type */
    public Transaction getRawTransaction(Sha256Hash txid) throws JsonRPCException, IOException {
        byte[] raw = getRawTransactionBytes(txid);
        // Hard-code RegTest for now
        // TODO: All RPC client connections should have a BitcoinJ params object?
        Transaction tx = new Transaction(RegTestParams.get(), raw);
        return tx;
    }

    public byte[] getRawTransactionBytes(Sha256Hash txid) throws JsonRPCException, IOException {
        List<Object> params = createParamList(txid.toString());
        Map<String, Object> response = send("getrawtransaction", params);

        @SuppressWarnings("unchecked")
        String hexEncoded = (String) response.get("result");
        byte[] raw = BitcoinClient.hexStringToByteArray(hexEncoded);
        return raw;
    }

    /* TODO: Return a stronger type than an a Map? */
    public Map<String, Object> getRawTransactionMap(Sha256Hash txid) throws JsonRPCException, IOException {
        List<Object> params = createParamList(txid.toString(), 1);
        Map<String, Object> response = send("getrawtransaction", params);

        @SuppressWarnings("unchecked")
        Map<String, Object> json = (Map<String, Object>) response.get("result");
        return json;
    }

    public Sha256Hash sendRawTransaction(Transaction tx) throws JsonRPCException, IOException {
        return sendRawTransaction(tx, null);
    }

    public Sha256Hash sendRawTransaction(String hexTx) throws JsonRPCException, IOException {
        return sendRawTransaction(hexTx, null);
    }

    public Sha256Hash sendRawTransaction(Transaction tx, Boolean allowHighFees) throws JsonRPCException, IOException {
        String hexTx = transactionToHex(tx);
        return sendRawTransaction(hexTx, allowHighFees);
    }

    public Sha256Hash sendRawTransaction(String hexTx, Boolean allowHighFees) throws JsonRPCException, IOException {
        List<Object> params = createParamList(hexTx, allowHighFees);
        Map<String, Object> response = send("sendrawtransaction", params);

        String txid = (String) response.get("result");
        Sha256Hash hash = new Sha256Hash(txid);
        return hash;
    }

    public BigDecimal getReceivedByAddress(Address address) throws JsonRPCException, IOException {
        return getReceivedByAddress(address, 1);   // Default to 1 or more confirmations
    }

    public BigDecimal getReceivedByAddress(Address address, Integer minConf) throws JsonRPCException, IOException {
        List<Object> params = createParamList(address.toString(), minConf);
        Map<String, Object> response = send("getreceivedbyaddress", params);
        BigDecimal balance = BigDecimal.valueOf((Double) response.get("result"));
        return balance;
    }

    public List<Object> listReceivedByAddress(Integer minConf, Boolean includeEmpty ) throws JsonRPCException, IOException {
        List<Object> params = createParamList(minConf, includeEmpty);

        Map<String, Object> response = send("listreceivedbyaddress", params);

        @SuppressWarnings("unchecked")
        List<Object> addresses = (List<Object>) response.get("result");
        return addresses;
    }

    /**
     * Returns a list of unspent transaction outputs with at least one confirmation.
     *
     * @return The unspent transaction outputs
     * @throws JsonRPCException
     * @throws IOException
     */
    public List<Map<String, Object>> listUnspent() throws JsonRPCException, IOException {
        return listUnspent(null, null, null);
    }

    /**
     * Returns a list of unspent transaction outputs with at least {@code minConf} and not more than {@code maxConf}
     * confirmations.
     *
     * @param minConf The minimum confirmations to filter
     * @param maxConf The maximum confirmations to filter
     * @return The unspent transaction outputs
     * @throws JsonRPCException
     * @throws IOException
     */
    public List<Map<String, Object>> listUnspent(Integer minConf, Integer maxConf)
            throws JsonRPCException, IOException {
        return listUnspent(minConf, maxConf, null);
    }

    /**
     * Returns a list of unspent transaction outputs with at least {@code minConf} and not more than {@code maxConf}
     * confirmations, filtered by a list of addresses.
     *
     * @param minConf The minimum confirmations to filter
     * @param maxConf The maximum confirmations to filter
     * @param filter  Include only transaction outputs to the specified addresses
     * @return The unspent transaction outputs
     * @throws JsonRPCException
     * @throws IOException
     */
    public List<Map<String, Object>> listUnspent(Integer minConf, Integer maxConf, Iterable<Address> filter)
            throws JsonRPCException, IOException {
        List<String> addressFilter = null;
        if (filter != null) {
            addressFilter = applyToString(filter);
        }

        List<Object> params = createParamList(minConf, maxConf, addressFilter);
        Map<String, Object> response = send("listunspent", params);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> unspent = (List<Map<String, Object>>) response.get("result");
        return unspent;
    }

    /**
     * Returns details about an unspent transaction output.
     *
     * @param txid The transaction hash
     * @param vout The transaction output index
     * @return Details about an unspent output or nothing, if the output was already spent
     */
    public Map<String,Object> getTxOut(Sha256Hash txid, Integer vout) throws JsonRPCException, IOException {
        return getTxOut(txid, vout, null);
    }

    /**
     * Returns details about an unspent transaction output.
     *
     * @param txid The transaction hash
     * @param vout The transaction output index
     * @param includeMemoryPool Whether to included the memory pool
     * @return Details about an unspent output or nothing, if the output was already spent
     */
    public Map<String,Object> getTxOut(Sha256Hash txid, Integer vout, Boolean includeMemoryPool)
            throws JsonRPCException, IOException {
        List<Object> params = createParamList(txid.toString(), vout, includeMemoryPool);
        Map<String, Object> response = send("gettxout", params);

        @SuppressWarnings("unchecked")
        Map<String, Object> json = (Map<String, Object>) response.get("result");
        return json;
    }

    public BigDecimal getBalance() throws JsonRPCException, IOException {
        return getBalance(null, null);
    }

    public BigDecimal getBalance(String account) throws JsonRPCException, IOException {
        return getBalance(account, null);
    }

    public BigDecimal getBalance(String account, Integer minConf) throws JsonRPCException, IOException {
        List<Object> params = createParamList(account, minConf);
        Map<String, Object> response = send("getbalance", params);
        Double balanceBTCd = (Double) response.get("result");
        // Beware of the new BigDecimal(double d) constructor, it results in unexpected/undesired values.
        BigDecimal balanceBTC = BigDecimal.valueOf(balanceBTCd);
        return balanceBTC;
    }

    public Sha256Hash sendToAddress(Address address, BigDecimal amount) throws JsonRPCException, IOException {
        return sendToAddress(address, amount, null, null);
    }

    public Sha256Hash sendToAddress(Address address, BigDecimal amount, String comment, String commentTo) throws JsonRPCException, IOException {
        List<Object> params = createParamList(address.toString(), amount, comment, commentTo);

        Map<String, Object> response = send("sendtoaddress", params);

        String txid = (String) response.get("result");
        Sha256Hash hash = new Sha256Hash(txid);
        return hash;
    }

    public Sha256Hash sendFrom(String account, Address address, BigDecimal amount) throws JsonRPCException, IOException {
        List<Object> params = createParamList(account, address.toString(), amount);

        Map<String, Object> response = send("sendfrom", params);

        String txid = (String) response.get("result");
        Sha256Hash hash = new Sha256Hash(txid);
        return hash;
    }

    public Sha256Hash sendMany(String account, Map<Address, BigDecimal> amounts) throws JsonRPCException, IOException {
        List<Object> params = Arrays.asList(account, amounts);

        Map<String, Object> response = send("sendmany", params);

        String txid = (String) response.get("result");
        Sha256Hash hash = new Sha256Hash(txid);
        return hash;
    }

    /**
     * Set the transaction fee per kB.
     *
     * @param amount The transaction fee in BTC/kB rounded to the nearest 0.00000001.
     * @return True if successful
     */
    public Boolean setTxFee(BigDecimal amount) throws JsonRPCException, IOException {
        List<Object> params = createParamList(amount);
        Map<String, Object> response = send("settxfee", params);

        @SuppressWarnings("unchecked")
        Boolean result = (Boolean) response.get("result");
        return result;
    }

    public Map<String, Object> getTransaction(Sha256Hash txid) throws JsonRPCException, IOException {
        List<Object> params = createParamList(txid.toString());
        Map<String, Object> response = send("gettransaction", params);

        @SuppressWarnings("unchecked")
        Map<String, Object> transaction = (Map<String, Object>) response.get("result");
        return transaction;
    }

    public Map<String, Object> getInfo() throws JsonRPCException, IOException {
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

    /**
     * Applies toString() to every element of {@code elements} and returns a list of the results.
     *
     * @param elements The elements
     * @return The list of strings
     */
    private <T> List<String> applyToString(Iterable<T> elements) {
        List<String> stringList = new ArrayList<String>();
        for (T element : elements) {
            String elementAsString = element.toString();
            stringList.add(elementAsString);
        }
        return stringList;
    }

}
