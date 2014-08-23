package org.mastercoin.rpc

import com.google.bitcoin.core.Address
import com.google.bitcoin.core.Sha256Hash
import com.google.bitcoin.core.Transaction
import com.msgilligan.bitcoin.rpc.BitcoinClient
import com.msgilligan.bitcoin.rpc.RPCConfig
import groovy.transform.CompileStatic

/**
 * Bitcoin and Mastercoin JSON-RPC client with method names that exactly match wire and CLI names.
 *
 * A version of the BitcoinClient with JVM method names that match
 * the RPC method names and bitcoin-cli method names exactly.
 * (in other words, all lowercase and underscores)
 *
 * Uses Groovy defaults to set automatic parameters to null. This is better
 * than using Java varargs because it allows us to specify type information.
 * We set parameters to null, not the default values in the Bitcoin RPC API
 * because we want the server to choose the defaults, not our client.
 *
 */
@CompileStatic
class MastercoinCLIClient extends MastercoinClient {

    MastercoinCLIClient(URL server, String rpcuser, String rpcpassword) {
        super(server, rpcuser, rpcpassword)
    }

    /**
     * Return the current block count
     *
     * @return current block count (block height)
     */
    Integer getblockcount() {
        return getBlockCount()
    }

    /**
     * Enable or disable hashing to attempt to find the next block
     *
     * In RegTest mode setgenerate is used to mine 1 or more blocks
     * on command.
     *
     * @param generate to enable generation, true; to disable, false.
     * @param genproclimit (optional) the number of logical processors to use. Defaults to 1; use -1 to use all available processors.
     */
    void setgenerate(Boolean generate, Long genproclimit = null) {
        setGenerate(generate, genproclimit);
    }

    /**
     * Returns a new Bitcoin address for receiving payments.
     *
     * @param account (optional) If account is specified, payments received with the address will be credited to that account.
     * @return A Bitcoin Address
     */
    Address getnewaddress(String account = null) {
        return getNewAddress(account)
    }

    /**
     * Returns the current Bitcoin address for receiving payments to this account.
     *
     * If the account doesn’t exist, it creates both the account and a new address for receiving payment.
     *
     * @param account
     * @return A Bitcoin Address
     */
    Address getaccountaddress(String account) {
        return getAccountAddress(account)
    }

    /**
     * Move a specified amount from one account in your wallet to another.
     *
     * @param fromaccount the name of the account from which to move the funds. Use “” for the default account.
     * @param toaccount the name of the account to which the funds should be moved. Use “” for the default account.
     * @param amount the amount to move in decimal bitcoins.
     * @param minconf the minimum number of confirmations
     * @param comment a comment to associate with this transaction.
     * @return
     */
    Boolean move(Address fromaccount, Address toaccount, BigDecimal amount, Integer minconf=null, String comment=null) {
        return moveFunds(fromaccount, toaccount, amount, minconf, comment)
    }

    /**
     * Get detailed information about an in-wallet transaction.
     *
     * @param txid a transaction identifier (hash) for the transaction to get information about.
     * @return a Map (JSON object) describing the transaction
     */
    Map<String, Object> gettransaction(Sha256Hash txid) {
        return getTransaction(txid)
    }

    /**
     * Get the rawtransaction-format data for a transaction.
     *
     * By default, bitcoind only stores complete transaction data for UTXOs and your own transactions,
     * so the RPC may fail on historic transactions unless you use the non-default txindex=1 in your
     * bitcoind startup settings.
     *
     * Note: The verbose param is not supported in this method since we're returning a strongly-typed
     * Transaction object which (in theory) offers the functionality of both the raw bytes and the JSON object
     * that would be returned by the lower-level JSON-RPC API.
     *
     * @param txid the txid (hash) of the transaction to get.
     * @return
     */
    Transaction getrawtransaction(Sha256Hash txid) {
        return getRawTransaction(txid)
    }

    /* TODO: Others still needing typed return values are omitted for now */

    BigDecimal getreceivedbyaddress(Address address, Integer minConf=null) {
        return getReceivedByAddress(address, minConf)
    }

    BigDecimal getbalance(String account, Integer minConf=null) {
        return getBalance(account,minConf)
    }

    Sha256Hash sendtoaddress(Address address, BigDecimal amount, String comment = null, String commentTo = null) {
        return sendToAddress(address,amount,comment,commentTo)
    }

    Sha256Hash sendfrom(String account, Address address, BigDecimal amount) {
        return sendFrom(account, address, amount)
    }

    Sha256Hash sendmany(String account, Map<Address, BigDecimal> amounts) {
        return sendMany(account, amounts)
    }

    Map<String, Object> getinfo() {
        return getInfo()
    }
}
