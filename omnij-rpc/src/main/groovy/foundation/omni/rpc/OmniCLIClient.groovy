package foundation.omni.rpc

import org.consensusj.bitcoin.json.pojo.WalletTransactionInfo
import org.bitcoinj.base.Address
import org.bitcoinj.base.Coin
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.base.Sha256Hash
import org.bitcoinj.core.Transaction
import groovy.transform.CompileStatic

/**
 * Bitcoin and Omni JSON-RPC client that closely mirrors JSON-RPC API.
 *
 * <p>
 * A JSON-RPC client for Bitcoin/Omni with JVM method names that match
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
 * @deprecated Use {@link OmniClient} (or {@code var dynamicClient = omniClient as DynamicRpcMethodFallback} if you really want lower-case RPC method names)
 */
@CompileStatic
@Deprecated
class OmniCLIClient extends OmniClient {

    OmniCLIClient(NetworkParameters netParams, URI server, String rpcuser, String rpcpassword) {
        super(netParams, server, rpcuser, rpcpassword)
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
     * Returns a new Bitcoin address for receiving payments.
     * <p>
     * @param account (optional) If account is specified, payments received with the address will be credited to that account.
     * @return A Bitcoin Address
     */
    Address getnewaddress(String account = null) {
        return getNewAddress(account)
    }
    
    /**
     * Get detailed information about an in-wallet transaction.
     *
     * @param txid a transaction identifier (hash) for the transaction to get information about.
     * @return a Map (JSON object) describing the transaction
     */
    WalletTransactionInfo gettransaction(Sha256Hash txid) {
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
    Coin getreceivedbyaddress(Address address, Integer minconf=null) {
        return getReceivedByAddress(address, minconf)
    }

    /**
     * Get the balance in decimal bitcoins across all accounts or for a particular account.
     *
     * @param account  the name of the account to get a balance for or “*” to get the balance for all accounts (the default). The default (primary) account can be specified using “”, which is not the same as specifying “*” for all accounts.
     * @param minconf (optional) the minimum number of confirmations an incoming transaction must have before it is counted towards the balance.
     * @return
     */
    Coin getbalance(String account, Integer minconf=null) {
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
    Sha256Hash sendtoaddress(Address address, Coin amount, String comment = null, String commentTo = null) {
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
    Sha256Hash sendfrom(String account, Address address, Coin amount) {
        return sendFrom(account, address, amount)
    }

    /**
     * Create and broadcast a transaction which spends outputs to multiple addresses.
     *
     * @param account
     * @param amounts
     * @return
     */
    Sha256Hash sendmany(String account, Map<Address, Coin> amounts) {
        return sendMany(account, amounts)
    }
}
