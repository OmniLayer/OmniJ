package foundation.omni.rpc;

import com.msgilligan.bitcoin.BTC;
import com.msgilligan.bitcoin.rpc.JsonRPCException;
import com.msgilligan.bitcoin.rpc.RPCConfig;
import foundation.omni.CurrencyID;
import foundation.omni.Ecosystem;
import foundation.omni.OmniDivisibleValue;
import foundation.omni.PropertyType;
import foundation.omni.tx.RawTxBuilder;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Sha256Hash;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;

/**
 * OmniClient that adds "extended" methods for Omni transactions that lack
 * RPCs in Omni Core 0.9.0
 *
 * <p>Raw transactions are created and sent via sendrawtx_MP
 *
 */
public class OmniExtendedClient extends OmniClient {
    RawTxBuilder builder = new RawTxBuilder();

    public OmniExtendedClient(RPCConfig config) throws IOException {
        super(config);
    }

    public OmniExtendedClient(URI server, String rpcuser, String rpcpassword) throws IOException {
        super(server, rpcuser, rpcpassword);
    }

    /**
     * Creates and broadcasts a "send to owners" transaction.
     *
     * @param currencyId  The identifier of the currency
     * @param amount      The number of tokens to distribute
     * @return The transaction hash
     */
    public Sha256Hash sendToOwners(Address address, CurrencyID currencyId, Long amount) throws JsonRPCException, IOException {
        String rawTxHex = builder.createSendToOwnersHex(currencyId, amount);
        Sha256Hash txid = sendrawtx_MP(address, rawTxHex);
        return txid;
    }

    /**
     * Creates an offer on the traditional distributed exchange.
     *
     * @param address        The address
     * @param currencyId     The identifier of the currency for sale
     * @param amountForSale  The amount of currency (BigDecimal coins)
     * @param amountDesired  The amount of desired Bitcoin (in BTC)
     * @param paymentWindow  The payment window measured in blocks
     * @param commitmentFee  The minimum transaction fee required to be paid as commitment when accepting this offer
     * @param action         The action applied to the offer (1 = new, 2 = update, 3 = cancel)
     * @return The transaction hash
     */
    public Sha256Hash createDexSellOffer(Address address, CurrencyID currencyId, BigDecimal amountForSale,
                                  BigDecimal amountDesired, Byte paymentWindow, BigDecimal commitmentFee,
                                  Byte action) throws JsonRPCException, IOException {
        OmniDivisibleValue quantityForSale = OmniDivisibleValue.of(amountForSale);
        Coin satoshisDesired = BTC.btcToCoin(amountDesired);
        Coin satoshisFee = BTC.btcToCoin(commitmentFee);
        String rawTxHex = builder.createDexSellOfferHex(
                currencyId, quantityForSale.asWillets(), satoshisDesired.value, paymentWindow, satoshisFee.value, action);
        Sha256Hash txid = sendrawtx_MP(address, rawTxHex);
        return txid;
    }

    /**
     * Accepts an offer on the traditional distributed exchange.
     *
     * @param fromAddress  The address used for the purchase
     * @param currencyId   The token to purchase
     * @param amount       The amount of tokens to purchase
     * @param toAddress    The address of the offer
     * @return The transaction hash
     */
    public Sha256Hash acceptDexOffer(Address fromAddress, CurrencyID currencyId, BigDecimal amount, Address toAddress)
            throws JsonRPCException, IOException {
        OmniDivisibleValue quantity = OmniDivisibleValue.of(amount);
        String rawTxHex = builder.createAcceptDexOfferHex(currencyId, quantity.asWillets());
        Sha256Hash txid = sendrawtx_MP(fromAddress, rawTxHex, toAddress);
        return txid;
    }

    /**
     * Creates an offer on the MetaDex exchange (aka Dex Phase II) (tx 21).
     *
     * <p>Note: Currently assumes divisible currencies</p>
     * <p>Note: Untested</p>
     *
     * @param address           The address
     * @param currencyForSale   The identifier of the currency for sale
     * @param amountForSale     The amount of currency (BigDecimal coins)
     * @param currencyDesired   The identifier of the currency for sale
     * @param amountDesired     The amount of desired Currency (divisible token, decimal format)
     * @param action            The action applied to the offer (1 = new, 2 = update, 3 = cancel)
     * @return The transaction hash
     */
    public Sha256Hash createMetaDexSellOffer(Address address, CurrencyID currencyForSale, BigDecimal amountForSale,
                                      CurrencyID currencyDesired, BigDecimal amountDesired,
                                      Byte action) throws JsonRPCException, IOException {
        OmniDivisibleValue qtyForSale = OmniDivisibleValue.of(amountForSale);  // Assume divisible property
        OmniDivisibleValue qtyDesired = OmniDivisibleValue.of(amountDesired);  // Assume divisible property
        String rawTxHex = builder.createMetaDexSellOfferHex(
                currencyForSale, qtyForSale.asWillets(), currencyDesired, qtyDesired.asWillets(), action);
        Sha256Hash txid = sendrawtx_MP(address, rawTxHex);
        return txid;
    }

    /**
     * Creates a crowdsale.
     *
     * @param address          The issuance address
     * @param ecosystem        The ecosystem to create the crowdsale in
     * @param propertyType     The property type
     * @param propertyDesired  The desired property
     * @param tokensPerUnit    The number of tokens per unit invested
     * @param deadline         The deadline as UNIX timestamp
     * @param earlyBirdBonus   The bonus percentage per week
     * @param issuerBonus      The bonus for the issuer
     * @return The transaction hash
     */
    public Sha256Hash createCrowdsale(Address address, Ecosystem ecosystem, PropertyType propertyType,
                                      CurrencyID propertyDesired, Long tokensPerUnit, Long deadline,
                                      Byte earlyBirdBonus, Byte issuerBonus)
            throws JsonRPCException, IOException {
        String rawTxHex = builder.createCrowdsaleHex(ecosystem, propertyType, 0L, "", "", "CS", "", "", propertyDesired,
                                                     tokensPerUnit, deadline, earlyBirdBonus, issuerBonus);
        Sha256Hash txid = sendrawtx_MP(address, rawTxHex);
        return txid;
    }

    /**
     * Creates a smart property with fixed supply.
     *
     * @param address    The issuance address
     * @param ecosystem  The ecosystem to create the property in
     * @param type       The property type
     * @param amount     The number of units to create
     * @return The transaction hash
     */
    public Sha256Hash createProperty(Address address, Ecosystem ecosystem, PropertyType type, Long amount)
            throws JsonRPCException, IOException {
        return createProperty(address, ecosystem, type, amount, "SP");
    }

    /**
     * Creates a smart property with fixed supply.
     *
     * @param address    The issuance address
     * @param ecosystem  The ecosystem to create the property in
     * @param type       The property type
     * @param amount     The number of units to create
     * @param label      The label or title of the property
     * @return The transaction hash
     */
    public Sha256Hash createProperty(Address address, Ecosystem ecosystem, PropertyType type, Long amount, String label)
            throws JsonRPCException, IOException {
        String rawTxHex = builder.createPropertyHex(ecosystem, type, 0L, "", "", label, "", "", amount);
        Sha256Hash txid = sendrawtx_MP(address, rawTxHex);
        return txid;
    }

    /**
     * Closes a crowdsale.
     *
     * @param address     The issuance address
     * @param currencyID  The identifier of the crowdsale
     * @return The transaction hash
     */
    public Sha256Hash closeCrowdsale(Address address, CurrencyID currencyID) throws JsonRPCException, IOException {
        String rawTxHex = builder.createCloseCrowdsaleHex(currencyID);
        Sha256Hash txid = sendrawtx_MP(address, rawTxHex);
        return txid;
    }

    /**
     * Creates a manged property.
     *
     * @param address      The issuance address
     * @param ecosystem    The ecosystem to create the property in
     * @param type         The property type
     * @param category     The category
     * @param subCategory  The subcategory
     * @param label        The label or title of the property to create
     * @param website      The website website
     * @param info         Additional information
     * @return The transaction hash
     */
    public Sha256Hash createManagedProperty(Address address, Ecosystem ecosystem, PropertyType type, String category,
                                            String subCategory, String label, String website, String info)
            throws JsonRPCException, IOException {
        String rawTxHex = builder.createManagedPropertyHex(ecosystem, type, 0L, category, subCategory, label, website,
                                                           info);
        Sha256Hash txid = sendrawtx_MP(address, rawTxHex);
        return txid;
    }

    /**
     * Grants tokens for a managed property.
     *
     * @param address     The issuance address
     * @param currencyID  The identifier of the property
     * @param amount      The number of tokens to grant
     * @return The transaction hash
     */
    public Sha256Hash grantTokens(Address address, CurrencyID currencyID, Long amount)
            throws JsonRPCException, IOException {
        String rawTxHex = builder.createGrantTokensHex(currencyID, amount, "");
        Sha256Hash txid = sendrawtx_MP(address, rawTxHex);
        return txid;
    }

    /**
     * Revokes tokens for a managed property.
     *
     * @param address     The issuance address
     * @param currencyID  The identifier of the property
     * @param amount      The number of tokens to revoke
     * @return The transaction hash
     */
    public Sha256Hash revokeTokens(Address address, CurrencyID currencyID, Long amount)
            throws JsonRPCException, IOException {
        String rawTxHex = builder.createRevokeTokensHex(currencyID, amount, "");
        Sha256Hash txid = sendrawtx_MP(address, rawTxHex);
        return txid;
    }

    /**
     * Changes the issuer on record of a managed property.
     *
     * @param fromAddress  The issuance address
     * @param currencyID   The identifier of the property
     * @param toAddress    The new issuer on record
     * @return The transaction hash
     */
    public Sha256Hash changeIssuer(Address fromAddress, CurrencyID currencyID, Address toAddress)
            throws JsonRPCException, IOException {
        String rawTxHex = builder.createChangePropertyManagerHex(currencyID);
        Sha256Hash txid = sendrawtx_MP(fromAddress, rawTxHex, toAddress);
        return txid;
    }

}
