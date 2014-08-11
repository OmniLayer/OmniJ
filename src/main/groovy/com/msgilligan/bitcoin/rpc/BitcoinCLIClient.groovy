package com.msgilligan.bitcoin.rpc

import com.google.bitcoin.core.Address
import com.google.bitcoin.core.Sha256Hash
import groovy.transform.CompileStatic

/*
 * A version of the BitcoinClient with JVM method names that match
 * the RPC method names and bitcoin-cli method names exactly.
 * (in other words, all lowercase and underscores)
 *
 * Uses Groovy defaults to set automatic parameters to null. This is better
 * than using Java varargs because it allows us to specify type information.
 * We set parameters to null, not the default values in the Bitcoin RPC API
 * because we want the server to chose the defaults, not our client.
 *
 */
@CompileStatic
class BitcoinCLIClient extends BitcoinClient {

    BitcoinCLIClient(URL server, String rpcuser, String rpcpassword) {
        super(server, rpcuser, rpcpassword)
    }

    BitcoinCLIClient(RPCConfig config) throws IOException {
        super(config)
    }

    Integer getblockcount() {
        return getBlockCount()
    }

    void setgenerate(Boolean generate, Long genproclimit = null) {
        setGenerate(generate, genproclimit);
    }

    Address getnewaddress() {
        return getNewAddress()
    }

    Address getaccountaddress(String account) {
        return getAccountAddress(account)
    }

    /* move -- Java implementation needs no wrapper */

    /* getRawTransaction - TODO typed return value
     * Could return binary or JSON, depending upon verbose flag  */

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
