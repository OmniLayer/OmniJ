package foundation.omni.rpc;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import foundation.omni.*;
import foundation.omni.json.conversion.OmniClientModule;
import foundation.omni.json.pojo.AddressBalanceEntries;
import foundation.omni.json.pojo.OmniPropertyInfo;
import foundation.omni.json.pojo.OmniTradeInfo;
import foundation.omni.json.pojo.OmniTransactionInfo;
import foundation.omni.json.pojo.PropertyBalanceEntries;
import foundation.omni.json.pojo.SmartPropertyListInfo;
import foundation.omni.net.OmniNetworkParameters;
import org.bitcoinj.base.Address;
import org.bitcoinj.base.Coin;
import org.bitcoinj.base.Network;
import org.bitcoinj.base.Sha256Hash;
import org.consensusj.bitcoin.jsonrpc.RpcConfig;
import org.consensusj.bitcoin.jsonrpc.bitcoind.BitcoinConfFile;
import org.consensusj.bitcoin.rx.jsonrpc.RxBitcoinClient;
import org.consensusj.jsonrpc.JsonRpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.concurrent.CompletableFuture;

// TODO: add missing RPCs:
// - omni_gettradehistoryforpair

/**
 * Pure Java Bitcoin and Omni Core JSON-RPC client with camelCase method names.
 * <p>
 * For example, if the RPC is {@code "omni_getbalance"}, then the corresponding method name is {@link #omniGetBalance(Address, CurrencyID)}.
 *
 * @see <a href="https://github.com/OmniLayer/omnicore/blob/master/src/omnicore/doc/rpc-api.md#json-rpc-api">Omni Core JSON RPC API documentation on GitHub</a>
 */
public class OmniClient extends RxBitcoinClient implements OmniClientRawTxSupport, OmniProxyMethods {
    private static final Logger log = LoggerFactory.getLogger(OmniClient.class);
    private final boolean isOmniProxy;

    /**
     * Construct a client by reading {@code bitcoin.conf} (Incubating)
     */
    public OmniClient() {
        this(BitcoinConfFile.readDefaultConfig().getRPCConfig());
    }

    public OmniClient(RpcConfig config) {
        this(config.network(), config.getURI(), config.getUsername(), config.getPassword());
    }

    public OmniClient(Network network, URI server, String rpcuser, String rpcpassword) {
        this(getDefaultSSLContext(), network, server, rpcuser, rpcpassword, false, false);
    }

    public OmniClient(SSLContext sslContext, Network network, URI server, String rpcuser, String rpcpassword, boolean useZmq) {
        this(sslContext, network, server, rpcuser, rpcpassword, useZmq, false);
    }

    public OmniClient(Network network, URI server, String rpcuser, String rpcpassword, boolean useZmq, boolean isOmniProxy) {
        this(getDefaultSSLContext(), network, server, rpcuser, rpcpassword, useZmq, isOmniProxy);
    }

    public OmniClient(SSLContext sslContext, Network network, URI server, String rpcuser, String rpcpassword, boolean useZmq, boolean isOmniProxy) {
        super(sslContext, network, server, rpcuser, rpcpassword, useZmq);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new OmniClientModule());
        mapper.registerModule(new ParameterNamesModule());
        this.isOmniProxy = isOmniProxy;
    }

    public OmniNetworkParameters getOmniNetParams() {
        return OmniNetworkParameters.fromBitcoinNetwork(getNetwork());
    }

    /**
     * Check if server is running Omni Core
     * @return result is true if server is an <b>Omni Core</b> server, false otherwise
     */
    public CompletableFuture<Boolean> isOmniServer() {
        return supplyAsync(this::getNetworkInfo)
                .thenApply(n -> n.getSubVersion().toLowerCase(Locale.ROOT).contains("omni"));
    }

    // TODO: Query the server to determine if it is OmniProxy
    /**
     * @return true of client was configured to talk to OmniProxy
     */
    @Override
    public boolean isOmniProxyServer() {
        return this.isOmniProxy;
    }

    /**
     * Returns various state information of Omni Core and the Omni Layer protocol.
     *
     * @return An object with state information
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     */
    public Map<String, Object> omniGetInfo() throws JsonRpcException, IOException {
        Map<String, Object> result = send("omni_getinfo");
        return result;
    }

    /**
     * Lists all currencies, smart properties and tokens.
     *
     * @return A list with short information
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     */
    public List<SmartPropertyListInfo> omniListProperties() throws JsonRpcException, IOException {
        JavaType resultType = mapper.getTypeFactory().constructCollectionType(List.class, SmartPropertyListInfo.class);
        return send("omni_listproperties", resultType);
    }

    /**
     * Returns information about the specified currency, property, or token.
     *
     * @param currency The identifier to look up
     * @return Omni Smart Property Info
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     */
    public OmniPropertyInfo omniGetProperty(CurrencyID currency) throws JsonRpcException, IOException {
        OmniPropertyInfo result = send("omni_getproperty", OmniPropertyInfo.class, currency);
        return result;
    }

    /**
     * Returns information about a crowdsale.
     *
     * @param currency The identifier of the crowdsale
     * @return An object with detailed information
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     */
    public Map<String, Object> omniGetCrowdsale(CurrencyID currency) throws JsonRpcException, IOException {
        Map<String, Object> result = send("omni_getcrowdsale", currency);
        return result;
    }

    /**
     * Lists currently active crowdsales.
     *
     * @return A list with information about active crowdsales
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     */
    public List<Map<String, Object>> omniGetActiveCrowdsales() throws JsonRpcException, IOException {
        List<Map<String, Object>> result = send("omni_getactivecrowdsales");
        return result;
    }

    /**
     * Lists currently active offers on the distributed BTC/OMNI exchange.
     *
     * @return A list with information about the active offers
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     */
    public List<Map<String, Object>> omniGetActiveDExSells() throws JsonRpcException, IOException {
        List<Map<String, Object>> result = send("omni_getactivedexsells");
        return result;
    }

    /**
     * Returns the balance for a given address and property.
     *
     * @param address  The address to look up
     * @param currency The identifier of the token to look up
     * @return The available and reserved balance
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     */
    public BalanceEntry omniGetBalance(Address address, CurrencyID currency)
            throws JsonRpcException, IOException {
        return send("omni_getbalance", BalanceEntry.class, address, currency.getValue());
    }

    /**
     * Returns a sorted map of address-balances for a given identifier.
     *
     * @param currency The identifier of the token to look up
     * @return A Sorted Map indexed by addresses to available and reserved balances
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     */
    public SortedMap<Address, BalanceEntry> omniGetAllBalancesForId(CurrencyID currency)
            throws JsonRpcException, IOException {
        return send("omni_getallbalancesforid", AddressBalanceEntries.class, currency);
    }

    /**
     * Returns a list of address-balances for a given identifier.
     *
     * @param currency The identifier of the token to look up
     * @return A list of address-balances
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     */
    public List<AddressBalanceEntry> omniGetAllBalancesForIdAsList(CurrencyID currency)
            throws JsonRpcException, IOException {
        JavaType resultType = mapper.getTypeFactory().constructCollectionType(List.class, AddressBalanceEntry.class);
        return send("omni_getallbalancesforid", resultType, currency);
    }

    /**
     * Returns a sorted map of all token balances for a given address.
     *
     * @param address The address to look up
     * @return A Sorted Map indexed by currency/propertyid to available and reserved balances
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     */
    public SortedMap<CurrencyID, BalanceEntry> omniGetAllBalancesForAddress(Address address)
            throws JsonRpcException, IOException {
        return send("omni_getallbalancesforaddress", PropertyBalanceEntries.class, address);
    }

    /**
     * Returns a list of all token balances for a given address.

     * @param address The address to look up
     * @return A List of {@link PropertyBalanceEntry}
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     */
    public List<PropertyBalanceEntry> omniGetAllBalancesForAddressAsList(Address address)
            throws JsonRpcException, IOException {
        JavaType resultType = mapper.getTypeFactory().constructCollectionType(List.class, PropertyBalanceEntry.class);
        return send("omni_getallbalancesforaddress", resultType, address);
    }

    /**
     * Returns information about an Omni Layer transaction.
     *
     * @param txId The hash of the transaction to look up
     * @return Information about the transaction
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     */
    public OmniTransactionInfo omniGetTransaction(Sha256Hash txId) throws JsonRpcException, IOException {
        return send("omni_gettransaction", OmniTransactionInfo.class, txId);
    }

    public List<OmniTransactionInfo> omniListTransactions() throws JsonRpcException, IOException {
        return omniListTransactions(null, null, null, null, null);
    }

    public List<OmniTransactionInfo> omniListTransactions(String addressFilter, int count) throws JsonRpcException, IOException {
        return omniListTransactions(addressFilter, count, null, null, null);
    }

    /**
     * Return a list of wallet transactions
     *
     * @param addressFilter Return transactions matching this address, use "*" to return all transactions.
     * @param count maximum transaction count to return
     * @param skip number to skip
     * @param startBlock starting block
     * @param endBlock ending block
     * @return a list of transaction info objects
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     */
    public List<OmniTransactionInfo> omniListTransactions(String addressFilter, Integer count, Integer skip, Integer startBlock, Integer endBlock) throws JsonRpcException, IOException {
        JavaType resultType = mapper.getTypeFactory().constructCollectionType(List.class, OmniTransactionInfo.class);
        return send("omni_listtransactions", resultType, addressFilter, count, skip, startBlock, endBlock);
    }

    /**
     * Lists all Omni transactions in a block.
     *
     * @param blockIndex The block height or block index
     * @return A list of transaction hashes
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     */
    public List<Sha256Hash> omniListBlockTransactions(Integer blockIndex) throws JsonRpcException, IOException {
        JavaType resultType = mapper.getTypeFactory().constructCollectionType(List.class, Sha256Hash.class);
        return send("omni_listblocktransactions", resultType, blockIndex);
    }

    /**
     * Broadcasts a raw Omni Layer transaction.
     *
     * @param fromAddress The address to send from
     * @param rawTxHex    The hex-encoded raw transaction
     * @return The hash of the transaction
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     */
    @Override
    public Sha256Hash omniSendRawTx(Address fromAddress, String rawTxHex) throws JsonRpcException, IOException {
        return omniSendRawTx(fromAddress, rawTxHex, null);
    }

    /**
     * Broadcasts a raw Omni Layer transaction with reference address.
     *
     * @param fromAddress      The address to send from
     * @param rawTxHex         The hex-encoded raw transaction
     * @param referenceAddress The reference address
     * @return The hash of the transaction
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     */
    @Override
    public Sha256Hash omniSendRawTx(Address fromAddress, String rawTxHex, Address referenceAddress)
            throws JsonRpcException, IOException {
        return send("omni_sendrawtx", Sha256Hash.class, fromAddress, rawTxHex, referenceAddress);
    }

    /**
     * Creates and broadcasts a "simple send" transaction.
     *
     * @param fromAddress The address to spent from
     * @param toAddress   The address to send to
     * @param currency    The identifier of the token to transfer
     * @param amount      The amount to transfer (Divisible/Indivisible type should match currency ID)
     * @return The hash of the transaction
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     */
    public Sha256Hash omniSend(Address fromAddress, Address toAddress, CurrencyID currency, OmniValue amount)
            throws JsonRpcException, IOException {
        return send("omni_send", Sha256Hash.class, fromAddress, toAddress, currency, amount);
    }

    /**
     * Creates and broadcasts a "send to owners" transaction.
     *
     * @param fromAddress The address to spent from
     * @param currency    The identifier of the token to distribute
     * @param amount      The amount to distribute
     * @return The hash of the transaction
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     */
    public Sha256Hash omniSendSTO(Address fromAddress, CurrencyID currency, OmniValue amount)
            throws JsonRpcException, IOException {
        return send("omni_sendsto", Sha256Hash.class, fromAddress, currency, amount);
    }

    /**
     * Creates and broadcasts a "send all" transaction.
     *
     * @param fromAddress The address to spent from
     * @param toAddress   The address to send to
     * @param ecosystem   The ecosystem of the tokens to send
     * @return The hash of the transaction
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     * @since Omni Core 0.0.10
     */
    public Sha256Hash omniSendAll(Address fromAddress, Address toAddress, Ecosystem ecosystem)
            throws JsonRpcException, IOException {
        return send("omni_sendall", Sha256Hash.class, fromAddress, toAddress, ecosystem);
    }

    /**
     * Creates and broadcasts a "send to many" transaction.
     *
     * @param fromAddress The address to spent from
     * @param currency    The identifier of the token to distribute
     * @param mapping     The receiving addresses and amounts as list of OmniOutput
     * @return The hash of the transaction
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     * @since Omni Core 0.0.12
     */
    public Sha256Hash omniSendToMany(Address fromAddress, CurrencyID currency, List<OmniOutput> mapping)
            throws JsonRpcException, IOException {
        return send("omni_sendtomany", Sha256Hash.class, fromAddress, currency, mapping);
    }

    /**
     * Creates an offer on the traditional distributed exchange.
     *
     * @param fromAddress   The address
     * @param currencyId    The identifier of the currency for sale
     * @param amountForSale The amount of currency (BigDecimal coins)
     * @param amountDesired The amount of desired Bitcoin (in BTC)
     * @param paymentWindow The payment window measured in blocks
     * @param commitmentFee The minimum transaction fee required to be paid as commitment when accepting this offer
     * @param action        The action applied to the offer (1 = new, 2 = update, 3 = cancel)
     * @return The hash of the transaction
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     * @since Omni Core 0.0.10
     */
    public Sha256Hash omniSendDExSell(Address fromAddress, CurrencyID currencyId, OmniValue amountForSale,
                                      Coin amountDesired, Byte paymentWindow, Coin commitmentFee,
                                      Byte action)
            throws JsonRpcException, IOException {
        return send("omni_senddexsell", Sha256Hash.class,
                fromAddress, currencyId,
                amountForSale,
                amountDesired.toString(),   // Omni Core expects string for BTC value, unlike BTC RPCs?
                paymentWindow,
                commitmentFee.toString(),
                action);
    }

    /**
     * Create and broadcast an accept order for the specified token and amount.
     *
     * @param fromAddress The address to send from
     * @param toAddress   The address of the seller
     * @param currencyId  The identifier of the token to purchase
     * @param amount      The amount to accept
     * @param override    Override minimum accept fee and payment window checks (use with caution!)
     * @return The hash of the transaction
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     * @since Omni Core 0.0.10
     */
    public Sha256Hash omniSendDExAccept(Address fromAddress, Address toAddress, CurrencyID currencyId,
                                        OmniValue amount, Boolean override)
            throws JsonRpcException, IOException {
        return send("omni_senddexaccept", Sha256Hash.class, fromAddress, toAddress, currencyId, amount, override);
    }

    /**
     * Place a trade offer on the distributed token exchange.
     *
     * @param fromAddress     The address to trade with
     * @param propertyForSale The identifier of the tokens to list for sale
     * @param amountForSale   The amount of tokens to list for sale
     * @param propertyDesired The identifier of the tokens desired in exchange
     * @param amountDesired   The amount of tokens desired in exchange
     * @return The hash of the transaction
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     * @since Omni Core 0.0.10
     */
    public Sha256Hash omniSendTrade(Address fromAddress, CurrencyID propertyForSale, OmniValue amountForSale,
                                    CurrencyID propertyDesired, OmniValue amountDesired)
            throws JsonRpcException, IOException {
        return send("omni_sendtrade", Sha256Hash.class, fromAddress, propertyForSale, amountForSale,
                                                                        propertyDesired, amountDesired);
    }

    /**
     * Cancel offers on the distributed token exchange with the specified price.
     *
     * @param fromAddress     The address to trade with
     * @param propertyForSale The identifier of the tokens to list for sale
     * @param amountForSale   The amount of tokens to list for sale
     * @param propertyDesired The identifier of the tokens desired in exchange
     * @param amountDesired   The amount of tokens desired in exchange
     * @return The hash of the transaction
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     * @since Omni Core 0.0.10
     */
    public Sha256Hash omniSendCancelTradesByPrice(Address fromAddress, CurrencyID propertyForSale,
                                                  OmniValue amountForSale, CurrencyID propertyDesired,
                                                  OmniValue amountDesired)
            throws JsonRpcException, IOException {
        return send("omni_sendcanceltradesbyprice", Sha256Hash.class, fromAddress, propertyForSale, amountForSale,
                                                                                    propertyDesired, amountDesired);
    }

    /**
     * Cancel all offers on the distributed token exchange with the given currency pair.
     *
     * @param fromAddress     The address to trade with
     * @param propertyForSale The identifier of the tokens listed for sale
     * @param propertyDesired The identifier of the tokens desired in exchange
     * @return The hash of the transaction
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     * @since Omni Core 0.0.10
     */
    public Sha256Hash omniSendCancelTradesByPair(Address fromAddress, CurrencyID propertyForSale,
                                                 CurrencyID propertyDesired)
            throws JsonRpcException, IOException {
        return send("omni_sendcanceltradesbypair", Sha256Hash.class, fromAddress, propertyForSale, propertyDesired);
    }

    /**
     * Cancel all offers on the distributed token exchange with the given currency pair.
     *
     * @param fromAddress The address to trade with
     * @param ecosystem   The ecosystem of the offers to cancel: (1) main, (2) test
     * @return The hash of the transaction
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     * @since Omni Core 0.0.10
     */
    public Sha256Hash omniSendCancelAllTrades(Address fromAddress, Ecosystem ecosystem)
            throws JsonRpcException, IOException {
        return send("omni_sendcancelalltrades", Sha256Hash.class, fromAddress, ecosystem);
    }

    /**
     * Create new tokens with fixed supply.
     *
     * @param fromAddress  The address to send from
     * @param ecosystem    The ecosystem to create the tokens in
     * @param propertyType The type of the tokens to create
     * @param previousId   An identifier of a predecessor token (0 for new tokens)
     * @param category     A category for the new tokens (can be "")
     * @param subCategory  A subcategory for the new tokens (can be "")
     * @param name         The name of the new tokens to create
     * @param url          An URL for further information about the new tokens (can be "")
     * @param data         A description for the new tokens (can be "")
     * @param amount       The number of tokens to create
     * @return The hash of the transaction
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     * @since Omni Core 0.0.10
     */
    public Sha256Hash omniSendIssuanceFixed(Address fromAddress, Ecosystem ecosystem, PropertyType propertyType,
                                            CurrencyID previousId, String category, String subCategory, String name,
                                            String url, String data, OmniValue amount)
            throws JsonRpcException, IOException {
        return send("omni_sendissuancefixed", Sha256Hash.class, fromAddress, ecosystem,  propertyType, previousId,
                                                        category, subCategory, name, url, data, amount);
    }

    /**
     * Create new tokens as crowdsale.
     *
     * @param fromAddress     The address to send from
     * @param ecosystem       The ecosystem to create the tokens in
     * @param propertyType    The type of the tokens to create
     * @param previousId      An identifier of a predecessor token (0 for new tokens)
     * @param category        A category for the new tokens (can be "")
     * @param subCategory     A subcategory for the new tokens (can be "")
     * @param name            The name of the new tokens to create
     * @param url             An URL for further information about the new tokens (can be "")
     * @param data            A description for the new tokens (can be "")
     * @param propertyDesired the identifier of a token eligible to participate in the crowdsale
     * @param tokensPerUnit   the amount of tokens granted per unit invested in the crowdsale
     * @param deadline        the deadline of the crowdsale as Unix timestamp
     * @param earlyBirdBonus  an early bird bonus for participants in percent per week
     * @param issuerBonus     a percentage of tokens that will be granted to the issuer
     * @return The hash of the transaction
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     * @since Omni Core 0.0.10
     */
    public Sha256Hash omniSendIssuanceCrowdsale(Address fromAddress, Ecosystem ecosystem, PropertyType propertyType,
                                                CurrencyID previousId, String category, String subCategory, String name,
                                                String url, String data, CurrencyID propertyDesired,
                                                BigDecimal tokensPerUnit, Long deadline, Byte earlyBirdBonus,
                                                Byte issuerBonus)
            throws JsonRpcException, IOException {
        return send("omni_sendissuancecrowdsale", Sha256Hash.class, fromAddress, ecosystem, propertyType, previousId,
                category, subCategory, name, url, data,
                propertyDesired, tokensPerUnit.toPlainString(), deadline, earlyBirdBonus, issuerBonus);
    }

    /**
     * Manually close a crowdsale.
     *
     * @param fromAddress The address associated with the crowdsale to close
     * @param propertyId  The identifier of the crowdsale to close
     * @return The hash of the transaction
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     * @since Omni Core 0.0.10
     */
    public Sha256Hash omniSendCloseCrowdsale(Address fromAddress, CurrencyID propertyId)
            throws JsonRpcException, IOException {
        return send("omni_sendclosecrowdsale", Sha256Hash.class, fromAddress, propertyId);
    }

    /**
     * Create new tokens with manageable supply.
     *
     * @param fromAddress  The address to send from
     * @param ecosystem    The ecosystem to create the tokens in
     * @param propertyType The type of the tokens to create
     * @param previousId   An identifier of a predecessor token (0 for new tokens)
     * @param category     A category for the new tokens (can be "")
     * @param subCategory  A subcategory for the new tokens (can be "")
     * @param name         The name of the new tokens to create
     * @param url          An URL for further information about the new tokens (can be "")
     * @param data         A description for the new tokens (can be "")
     * @return The hash of the transaction
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     * @since Omni Core 0.0.10
     */
    public Sha256Hash omniSendIssuanceManaged(Address fromAddress, Ecosystem ecosystem, PropertyType propertyType,
                                              CurrencyID previousId, String category, String subCategory, String name,
                                              String url, String data)
            throws JsonRpcException, IOException {
        return send("omni_sendissuancemanaged", Sha256Hash.class, fromAddress, ecosystem, propertyType, previousId,
                                                        category, subCategory, name, url, data);
    }

    /**
     * Issue or grant new units of managed tokens.
     *
     * @param fromAddress The address to send from
     * @param toAddress   The receiver of the tokens
     * @param propertyId  The identifier of the tokens to grant
     * @param amount      The amount of tokens to create
     * @return The hash of the transaction
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     * @since Omni Core 0.0.10
     */
    public Sha256Hash omniSendGrant(Address fromAddress, Address toAddress, CurrencyID propertyId, OmniValue amount)
            throws JsonRpcException, IOException {
        return send("omni_sendgrant", Sha256Hash.class, fromAddress, toAddress, propertyId, amount);
    }

    /**
     * Revoke units of managed tokens.
     *
     * @param fromAddress The address to revoke the tokens from
     * @param propertyId  The identifier of the tokens to revoke
     * @param amount      The amount of tokens to revoke
     * @return The hash of the transaction
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     * @since Omni Core 0.0.10
     */
    public Sha256Hash omniSendRevoke(Address fromAddress, CurrencyID propertyId, OmniValue amount)
            throws JsonRpcException, IOException {
        return send("omni_sendrevoke", Sha256Hash.class, fromAddress, propertyId, amount);
    }

    /**
     * Change the issuer on record of the given tokens.
     *
     * @param fromAddress The address associated with the tokens
     * @param toAddress   The address to transfer administrative control to
     * @param propertyId  The identifier of the tokens
     * @return The hash of the transaction
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     * @since Omni Core 0.0.10
     */
    public Sha256Hash omniSendChangeIssuer(Address fromAddress, Address toAddress, CurrencyID propertyId)
            throws JsonRpcException, IOException {
        return send("omni_sendchangeissuer", Sha256Hash.class, fromAddress, toAddress, propertyId);
    }

    /**
     * Activates a protocol feature.
     *
     * @param fromAddress  The address to send from
     * @param featureId    The identifier of the feature to activate
     * @param block        The activation block
     * @param minVersion   The minimum supported client version
     * @return The hash of the transaction
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     * @since Omni Core 0.0.10
     */
    public Sha256Hash omniSendActivation(Address fromAddress, Short featureId, Integer block, Integer minVersion)
            throws JsonRpcException, IOException {
        return send("omni_sendactivation", Sha256Hash.class, fromAddress, featureId, block, minVersion);
    }

    /**
     * Get information and recipients of a send-to-owners transaction.
     *
     * @param txid  The hash of the transaction to lookup
     * @return Information about the transaction
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     */
    public Map<String, Object> omniGetSTO(Sha256Hash txid) throws JsonRpcException, IOException {
        String filter = "*"; // no filter at all
        return send("omni_getsto", txid, filter);
    }

    /**
     * Returns information about an order on the distributed token exchange.
     *
     * @param txid The transaction hash of the order to look up
     * @return Information about the order, trade, and order matches
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     * @since Omni Core 0.0.10
     */
    public OmniTradeInfo omniGetTrade(Sha256Hash txid) throws JsonRpcException, IOException {
        return send("omni_gettrade", OmniTradeInfo.class, txid);
    }

    public List<OmniTradeInfo> omniGetTradeHistoryForAddress(Address address, Integer count, CurrencyID propertyId) throws JsonRpcException, IOException {
        JavaType resultType = mapper.getTypeFactory().constructCollectionType(List.class, OmniTradeInfo.class);
        return send("omni_gettradehistoryforaddress", resultType, address, count, propertyId);
    }

    /**
     * Lists orders on the distributed token exchange with the given token for sale.
     *
     * @param propertyForSale The identifier of the token for sale, used as filter
     * @return A list of orders
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     * @since Omni Core 0.0.10
     */
    public List<Map<String, Object>> omniGetOrderbook(CurrencyID propertyForSale) throws JsonRpcException, IOException {
        return send("omni_getorderbook", propertyForSale);
    }

    /**
     * Lists orders on the distributed token exchange with the given token for sale, and token desired.
     *
     * @param propertyForSale The identifier of the token for sale, used as filter
     * @param propertyDesired The identifier of the token desired, used as filter
     * @return A list of orders
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     * @since Omni Core 0.0.10
     */
    public List<Map<String, Object>> omniGetOrderbook(CurrencyID propertyForSale, CurrencyID propertyDesired)
            throws JsonRpcException, IOException {
        return send("omni_getorderbook", propertyForSale, propertyDesired);
    }

    /**
     * Returns information about granted and revoked units of managed tokens.
     *
     * @param propertyid The identifier of the managed tokens to lookup
     * @return A list of grants and revokes
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     */
    public List<Map<String, Object>> omniGetGrants(CurrencyID propertyid) throws JsonRpcException, IOException {
        return send("omni_getgrants", propertyid);
    }

    /**
     * Returns pending and completed feature activations.
     *
     * @return Pending and complete feature activations
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     * @since Omni Core 0.0.10
     */
    public Map<String, List<Map<String, Object>>> omniGetActivations() throws JsonRpcException, IOException {
        return send("omni_getactivations");
    }

    /**
     * Obtains the current amount of fees cached (pending distribution).
     *
     * If a property ID is supplied the results will be filtered to show this property ID only. If no property ID is
     * supplied the results will contain all properties that currently have fees cached pending distribution.
     *
     * @param propertyid the identifier of the property to filter results on
     * @return A list of amounts of fees cached
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     * @since Omni Core 0.0.11
     */
    public List<Map<String, Object>> omniGetFeeCache(CurrencyID propertyid)
            throws JsonRpcException, IOException {
        return send("omni_getfeecache", propertyid);
    }

    /**
     * Obtains the amount at which cached fees will be distributed.
     *
     * If a property ID is supplied the results will be filtered to show this property ID only.  If no property ID is
     * supplied the results will contain all properties.
     *
     * @param propertyId the identifier of the property to filter results on
     * @return A list of amounts of fees required to trigger distribution
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     * @since Omni Core 0.0.11
     */
    public List<Map<String, Object>> omniGetFeeTrigger(CurrencyID propertyId)
            throws JsonRpcException, IOException {
        return send("omni_getfeetrigger", propertyId);
    }

    /**
     * Obtains the current percentage share of fees addresses would receive if a distribution were to occur.
     *
     * If an address is supplied the results will be filtered to show this address only. If no address is supplied the
     * results will be filtered to show wallet addresses only.
     *
     * If an ecosystem is supplied the results will reflect the fee share for that ecosystem (main or test). If no
     * ecosystem is supplied the results will reflect the main ecosystem.
     *
     * @param address   the address to filter results on
     * @param ecosystem the ecosystem to obtain the current percentage fee share
     * @return A list of percentages of fees the address(es) will receive based on the current state
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     * @since Omni Core 0.0.11
     */
    public List<Map<String, Object>> omniGetFeeShare(Address address, Ecosystem ecosystem)
            throws JsonRpcException, IOException {
        return send("omni_getfeeshare", Objects.requireNonNullElse(address, ""), ecosystem);
    }

    /**
     * Obtains data for a past distribution of fees.
     *
     * A distribution ID must be supplied to identify the distribution to obtain data for.
     *
     * @param distributionId the identifier of the distribution to obtain data for
     * @return Information about a fee distribution
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     * @since Omni Core 0.0.11
     */
    public Map<String, Object> omniGetFeeDistribution(Integer distributionId)
            throws JsonRpcException, IOException {
        return send("omni_getfeedistribution", distributionId);
    }

    /**
     * Obtains data for past distributions of fees for a property.
     *
     * A property ID must be supplied to retrieve past distributions for.
     *
     * @param propertyId the identifier of the property to retrieve past distributions for
     * @return A list of fee distributions
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     * @since Omni Core 0.0.11
     */
    public List<Map<String, Object>> omniGetFeeDistributions(CurrencyID propertyId)
            throws JsonRpcException, IOException {
        return send("omni_getfeedistributions", propertyId);
    }
}
