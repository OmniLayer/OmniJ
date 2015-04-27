package foundation.omni.rpc

import org.bitcoinj.core.Address
import org.bitcoinj.core.Sha256Hash
import org.bitcoinj.core.Transaction
import groovy.transform.CompileStatic

/**
 * Bitcoin and Mastercoin JSON-RPC client that closely mirrors JSON-RPC API.
 *
 * <p>
 * A JSON-RPC client for Bitcoin/Mastercoin with JVM method names that match
 * the RPC method names and bitcoin-cli method names exactly.
 * (in other words, all lowercase and underscores)
 *
 * <p>
 * We use Groovy defaults to set automatic parameters to <code>null</code>. This is better
 * than using Java varargs because it allows us to specify a unique type for each parameter.
 * We set parameters to <code>null</code>, not the default values in the RPC API
 * because we want the server to choose the defaults, not our client. A <code>null</code>
 * parameter is not sent in the RPC request.
 *
 */
@CompileStatic
class OmniCLIClient extends OmniExtendedClient {

    OmniCLIClient(URI server, String rpcuser, String rpcpassword) {
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
     * <p>
     * In RegTest mode <code>setgenerate</code> is used to mine 1 or more blocks
     * on command.
     * <p>
     * @param generate to enable generation, true; to disable, false.
     * @param genproclimit (optional) the number of logical processors to use. Defaults to 1; use -1 to use all available processors.
     * @return Object         Bitcoin 0.10.0+: An array containing the block header hashes of the generated blocks or null
     *                        if no blocks were generated
     *                        Bitcoin 0.9.x: null
     */
    Object setgenerate(Boolean generate, Long genproclimit = null) {
        return setGenerate(generate, genproclimit);
    }

    /**
     * Returns a new Bitcoin address for receiving payments.
     * <p>
     * @param account (optional) If account is specified, payments received with the address will be credited to that account.
     * @return A Bitcoin Address
     */
    Address getnewaddress(String account = null) {
        return getNewAddress(account)
    }

    /**
     * Returns the current Bitcoin address for receiving payments to this account.
     * <p>
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
     * @param minconf (optional) the minimum number of confirmations
     * @param comment (optional) a comment to associate with this transaction.
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
     * <p>
     * By default, bitcoind only stores complete transaction data for UTXOs and your own transactions,
     * so the RPC may fail on historic transactions unless you use the non-default txindex=1 in your
     * bitcoind startup settings.
     * <p>
     * Note: The verbose param is not supported in this method since we're returning a strongly-typed
     * Transaction object which (in theory) offers the functionality of both the raw bytes and the JSON object
     * that would be returned by the lower-level JSON-RPC API.
     * <p>
     * @param txid the transaction identifier (hash) of the transaction to get.
     * @return
     */
    Transaction getrawtransaction(Sha256Hash txid) {
        return getRawTransaction(txid)
    }

    /* TODO: Others still needing typed return values are omitted for now */

    /**
     * Returns the total amount received by the specified address in transactions with at least the indicated number of confirmations.
     *
     * @param address a Bitcoin address to check. Must be an address belonging to the wallet.
     * @param minconf (optional) the minimum number of confirmations a transaction must have before it is counted towards the total. 1 is the default; use 0 to also count unconfirmed transactions.
     * @return the total amount received
     */
    BigDecimal getreceivedbyaddress(Address address, Integer minconf=null) {
        return getReceivedByAddress(address, minconf)
    }

    /**
     * Get the balance in decimal bitcoins across all accounts or for a particular account.
     *
     * @param account  the name of the account to get a balance for or “*” to get the balance for all accounts (the default). The default (primary) account can be specified using “”, which is not the same as specifying “*” for all accounts.
     * @param minconf (optional) the minimum number of confirmations an incoming transaction must have before it is counted towards the balance.
     * @return
     */
    BigDecimal getbalance(String account, Integer minconf=null) {
        return getBalance(account,minconf)
    }

    /**
     * Spend an amount to a given address. Encrypted wallets must be unlocked first.
     * @param address
     * @param amount
     * @param comment
     * @param commentTo
     * @return
     */
    Sha256Hash sendtoaddress(Address address, BigDecimal amount, String comment = null, String commentTo = null) {
        return sendToAddress(address,amount,comment,commentTo)
    }

    /**
     * Spend an amount from an account to a bitcoin address.
     *
     * @param account
     * @param address
     * @param amount
     * @return
     */
    Sha256Hash sendfrom(String account, Address address, BigDecimal amount) {
        return sendFrom(account, address, amount)
    }

    /**
     * Create and broadcast a transaction which spends outputs to multiple addresses.
     *
     * @param account
     * @param amounts
     * @return
     */
    Sha256Hash sendmany(String account, Map<Address, BigDecimal> amounts) {
        return sendMany(account, amounts)
    }

    /**
     * Get various information about the node and the network.
     * Warning: getinfo will be removed in a later version of Bitcoin Core. Use getblockchaininfo, getnetworkinfo, or getwalletinfo instead.
     *
     * @return A Map (JSON object) containing the information.
     */
    Map<String, Object> getinfo() {
        return getInfo()
    }
}
