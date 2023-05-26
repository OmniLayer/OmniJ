package foundation.omni.rpc.test;

import foundation.omni.rpc.OmniClientRawTxSupport;
import org.consensusj.jsonrpc.JsonRpcException;
import foundation.omni.CurrencyID;
import foundation.omni.Ecosystem;
import foundation.omni.OmniDivisibleValue;
import foundation.omni.OmniValue;
import foundation.omni.PropertyType;
import foundation.omni.tx.RawTxBuilder;
import org.bitcoinj.base.Address;
import org.bitcoinj.base.Coin;
import org.bitcoinj.base.Sha256Hash;

import java.io.IOException;

/**
 * OmniClient methods that create raw Omni transactions and send them
 * using {@code "omniSendRawTx"}. This avoids error-checking on the RPCs and allows us to test
 * the effect of sending raw transactions that would be disallowed by this checking.
 */
public interface OmniTestClientMethods extends OmniClientRawTxSupport {
    RawTxBuilder omniRawTxBuilder = new RawTxBuilder();

    /**
     * Creates and broadcasts a "send to owners" transaction.
     *
     * @param address     address
     * @param currencyId  The identifier of the currency
     * @param amount      The number of tokens to distribute (assumed in willetts)
     * @return transaction hash/id
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     */
    default Sha256Hash sendToOwners(Address address, CurrencyID currencyId, OmniValue amount) throws JsonRpcException, IOException {
        //  ... but it doesn't matter since  createSendToOwnersHex just converts back to willetts.
        String rawTxHex = omniRawTxBuilder.createSendToOwnersHex(currencyId, amount);
        Sha256Hash txid = omniSendRawTx(address, rawTxHex);
        return txid;
    }

    /**
     * Creates an offer on the traditional distributed exchange.
     *
     * @param address        The address
     * @param currencyId     The identifier of the currency for sale
     * @param amountForSale  The amount of currency
     * @param amountDesired  The amount of desired Bitcoin
     * @param paymentWindow  The payment window measured in blocks
     * @param commitmentFee  The minimum transaction fee required to be paid as commitment when accepting this offer
     * @param action         The action applied to the offer (1 = new, 2 = update, 3 = cancel)
     * @return transaction hash/id
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     */
    default Sha256Hash createDexSellOffer(Address address, CurrencyID currencyId, OmniDivisibleValue amountForSale,
                                         Coin amountDesired, Byte paymentWindow, Coin commitmentFee,
                                         Byte action) throws JsonRpcException, IOException {
        String rawTxHex = omniRawTxBuilder.createDexSellOfferHex(
                currencyId, amountForSale, amountDesired, paymentWindow, commitmentFee, action);
        Sha256Hash txid = omniSendRawTx(address, rawTxHex);
        return txid;
    }

    /**
     * Accepts an offer on the traditional distributed exchange.
     *
     * @param fromAddress  The address used for the purchase
     * @param currencyId   The token to purchase
     * @param amount       The amount of tokens to purchase
     * @param toAddress    The address of the offer
     * @return transaction hash/id
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     */
    default Sha256Hash acceptDexOffer(Address fromAddress, CurrencyID currencyId, OmniDivisibleValue amount, Address toAddress)
            throws JsonRpcException, IOException {
        String rawTxHex = omniRawTxBuilder.createAcceptDexOfferHex(currencyId, amount);
        Sha256Hash txid = omniSendRawTx(fromAddress, rawTxHex, toAddress);
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
     * @param amountForSale     The amount of currency
     * @param currencyDesired   The identifier of the currency for sale
     * @param amountDesired     The amount of desired Currency (divisible token, decimal format)
     * @param action            The action applied to the offer (1 = new, 2 = update, 3 = cancel)
     * @return transaction hash/id
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     */
    default Sha256Hash createMetaDexSellOffer(Address address, CurrencyID currencyForSale, OmniDivisibleValue amountForSale,
                                             CurrencyID currencyDesired, OmniDivisibleValue amountDesired,
                                             Byte action) throws JsonRpcException, IOException {
        String rawTxHex = omniRawTxBuilder.createMetaDexSellOfferHex(
                currencyForSale, amountForSale, currencyDesired, amountDesired, action);
        Sha256Hash txid = omniSendRawTx(address, rawTxHex);
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
     * @return transaction hash/id
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     */
    default Sha256Hash createCrowdsale(Address address, Ecosystem ecosystem, PropertyType propertyType,
                                      CurrencyID propertyDesired, Long tokensPerUnit, Long deadline,
                                      Byte earlyBirdBonus, Byte issuerBonus)
            throws JsonRpcException, IOException {
        String rawTxHex = omniRawTxBuilder.createCrowdsaleHex(ecosystem, propertyType, 0L, "", "", "CS", "", "", propertyDesired,
                tokensPerUnit, deadline, earlyBirdBonus, issuerBonus);
        Sha256Hash txid = omniSendRawTx(address, rawTxHex);
        return txid;
    }

    /**
     * Creates a smart property with fixed supply.
     *
     * @param address    The issuance address
     * @param ecosystem  The ecosystem to create the property in
     * @param value      Amount (and property type)
     * @return transaction hash/id
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     */
    default Sha256Hash createProperty(Address address, Ecosystem ecosystem, OmniValue value)
            throws JsonRpcException, IOException {
        return createProperty(address, ecosystem, value, "SP");
    }

    /**
     * Creates a smart property with fixed supply.
     *
     * @param address    The issuance address
     * @param ecosystem  The ecosystem to create the property in
     * @param value      Amount (and property type)
     * @param label      The label or title of the property
     * @return transaction hash/id
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     */
    default Sha256Hash createProperty(Address address, Ecosystem ecosystem, OmniValue value, String label)
            throws JsonRpcException, IOException {
        String rawTxHex = omniRawTxBuilder.createPropertyHex(ecosystem, value.getPropertyType(), 0L, "", "", label, "", "", value);
        Sha256Hash txid = omniSendRawTx(address, rawTxHex);
        return txid;
    }

    /**
     * Create property
     * @param address    The issuance address
     * @param ecosystem  The ecosystem to create the property in
     * @param value      Amount (and property type)
     * @param previousPropertyId an identifier of a predecessor token (0 for new tokens)
     * @param category a category for the new tokens (can be "")
     * @param subCategory a subcategory for the new tokens (can be "")
     * @param label label
     * @param website an URL for further information about the new tokens (can be "")
     * @param info info
     * @return transaction hash/id
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     */
    default Sha256Hash createProperty(Address address, Ecosystem ecosystem, OmniValue value,
                                     Long previousPropertyId,
                                     String category,
                                     String subCategory,
                                     String label,
                                     String website,
                                     String info)
            throws JsonRpcException, IOException {
        String rawTxHex = omniRawTxBuilder.createPropertyHex(ecosystem,
                value.getPropertyType(),
                previousPropertyId,
                category, subCategory, label, website, info, value);
        Sha256Hash txid = omniSendRawTx(address, rawTxHex);
        return txid;
    }

    /**
     * Closes a crowdsale.
     *
     * @param address     The issuance address
     * @param currencyID  The identifier of the crowdsale
     * @return transaction hash/id
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     */
    default Sha256Hash closeCrowdsale(Address address, CurrencyID currencyID) throws JsonRpcException, IOException {
        String rawTxHex = omniRawTxBuilder.createCloseCrowdsaleHex(currencyID);
        Sha256Hash txid = omniSendRawTx(address, rawTxHex);
        return txid;
    }

    /**
     * Creates a managed property.
     *
     * @param address      The issuance address
     * @param ecosystem    The ecosystem to create the property in
     * @param type         The property type
     * @param category     The category
     * @param subCategory  The subcategory
     * @param label        The label or title of the property to create
     * @param website      The website website
     * @param info         Additional information
     * @return transaction hash/id
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     */
    default Sha256Hash createManagedProperty(Address address, Ecosystem ecosystem, PropertyType type, String category,
                                            String subCategory, String label, String website, String info)
            throws JsonRpcException, IOException {
        String rawTxHex = omniRawTxBuilder.createManagedPropertyHex(ecosystem, type, 0L, category, subCategory, label, website,
                info);
        Sha256Hash txid = omniSendRawTx(address, rawTxHex);
        return txid;
    }

    /**
     * Grants tokens for a managed property.
     *
     * @param address     The issuance address
     * @param currencyID  The identifier of the property
     * @param amount      The number of tokens to grant
     * @return transaction hash/id
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     */
    default Sha256Hash grantTokens(Address address, CurrencyID currencyID, OmniValue amount)
            throws JsonRpcException, IOException {
        String rawTxHex = omniRawTxBuilder.createGrantTokensHex(currencyID, amount, "");
        Sha256Hash txid = omniSendRawTx(address, rawTxHex);
        return txid;
    }

    /**
     * Revokes tokens for a managed property.
     *
     * @param address     The issuance address
     * @param currencyID  The identifier of the property
     * @param amount      The number of tokens to revoke
     * @return transaction hash/id
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     */
    default Sha256Hash revokeTokens(Address address, CurrencyID currencyID, OmniValue amount)
            throws JsonRpcException, IOException {
        String rawTxHex = omniRawTxBuilder.createRevokeTokensHex(currencyID, amount, "");
        Sha256Hash txid = omniSendRawTx(address, rawTxHex);
        return txid;
    }

    /**
     * Changes the issuer on record of a managed property.
     *
     * @param fromAddress  The issuance address
     * @param currencyID   The identifier of the property
     * @param toAddress    The new issuer on record
     * @return transaction hash/id
     * @throws JsonRpcException JSON RPC error
     * @throws IOException network error
     */
    default Sha256Hash changeIssuer(Address fromAddress, CurrencyID currencyID, Address toAddress)
            throws JsonRpcException, IOException {
        String rawTxHex = omniRawTxBuilder.createChangePropertyManagerHex(currencyID);
        Sha256Hash txid = omniSendRawTx(fromAddress, rawTxHex, toAddress);
        return txid;
    }
}
