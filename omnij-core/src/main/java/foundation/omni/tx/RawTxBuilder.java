package foundation.omni.tx;

import foundation.omni.CurrencyID;
import foundation.omni.Ecosystem;
import foundation.omni.OmniDivisibleValue;
import foundation.omni.OmniValue;
import foundation.omni.PropertyType;
import org.bitcoinj.core.Coin;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static foundation.omni.tx.Transactions.TransactionType.SEND_TO_OWNERS;
import static foundation.omni.tx.Transactions.TransactionType.SIMPLE_SEND;

/**
 * Build hex-encoded raw Omni transactions
 *
 * @author msgilligan
 * @author dexX7
 */
public class RawTxBuilder {

    private static final int SIZE_VERSIONTYPE = 4;
    private static final int SIZE_32 = 4;
    private static final int SIZE_64 = 8;

    /**
     * Creates a hex-encoded raw transaction of type 0: "simple send".
     * @param currencyId currency ID to send
     * @param amount amount to send
     * @return Hex encoded string for the transaction
     */
    @Deprecated
    public String createSimpleSendHex(CurrencyID currencyId, OmniValue amount) {
        return toHexString(createSimpleSend(currencyId, amount));
    }

    public byte[] createSimpleSend(CurrencyID currencyId, OmniValue amount) {
        return ByteBuffer
                .allocate(SIZE_VERSIONTYPE + SIZE_32 + SIZE_64)
                .putInt(SIMPLE_SEND.versionType())                         // Version + Type
                .putInt(currencyId.unsignedIntValue())
                .putLong(amount.getWilletts())
                .array();
    }


    /**
     * Creates a hex-encoded raw transaction of type 3: "send to owners".
     * @param currencyId currency ID to send
     * @param amount amount to send to all owners
     * @return Hex encoded string for the transaction
     */
    public String createSendToOwnersHex(CurrencyID currencyId, OmniValue amount) {
        return toHexString(createSendToOwners(currencyId, amount));
    }

    public byte[] createSendToOwners(CurrencyID currencyId, OmniValue amount) {
        return ByteBuffer
                .allocate(SIZE_VERSIONTYPE + SIZE_32 + SIZE_64)
                .putInt(SEND_TO_OWNERS.versionType())                       // Version + Type
                .putInt(currencyId.unsignedIntValue())
                .putLong(amount.getWilletts())
                .array();
    }

    /**
     * Creates a hex-encoded raw transaction of type 20: "sell tokens for bitcoins".
     *
     * Currency amounts are Long values in satoshis/willetts
     *
     * @param currencyId Currency ID to sell OMNI or TOMNI only
     * @param amountForSale Amount of OMNI/TOMNI for sale
     * @param amountDesired Amount of BTC desired
     * @param paymentWindow Time period in blocks
     * @param commitmentFee Minimum Bitcoin transaction fee
     * @param action Sell offer sub-action
     * @return The hex-encoded raw transaction
     */
    public String createDexSellOfferHex(CurrencyID currencyId, OmniDivisibleValue amountForSale, Coin amountDesired,
                                        Byte paymentWindow, Coin commitmentFee, Byte action) {
        String rawTxHex = String.format("00010014%08x%016x%016x%02x%016x%02x",
                currencyId.getValue(),
                amountForSale.getWilletts(),
                amountDesired.value,
                paymentWindow,
                commitmentFee.value,
                action);
        return rawTxHex;
    }

    /**
     * Creates a hex-encoded raw transaction of type 21: "trade tokens for tokens".
     * @param currencyForSale currency to sell
     * @param amountForSale amount to sell
     * @param currencyDesired currency desired in exchange
     * @param amountDesired amount of other currency desired
     * @param action action
     * @return The hex-encoded raw transaction
     */
    public String createMetaDexSellOfferHex(CurrencyID currencyForSale, OmniValue amountForSale, CurrencyID currencyDesired,
                                            OmniValue amountDesired, Byte action) {
        String rawTxHex = String.format("00000015%08x%016x%08x%016x%02x",
                currencyForSale.getValue(),
                amountForSale.getWilletts(),
                currencyDesired.getValue(),
                amountDesired.getWilletts(),
                action);
        return rawTxHex;
    }

    /**
     * Creates a hex-encoded raw transaction of type 22: "purchase tokens with bitcoins".
     * @param currencyId currency ID to purchase
     * @param amount amount to purchase
     * @return The hex-encoded raw transaction
     */
    public String createAcceptDexOfferHex(CurrencyID currencyId, OmniValue amount) {
        String rawTxHex = String.format("00000016%08x%016x",
                currencyId.getValue(),
                amount.getWilletts());
        return rawTxHex;
    }

    /**
     * Creates a hex-encoded raw transaction of type 50: "create a property with fixed supply".
     * @param ecosystem main or test ecosystem
     * @param propertyType divisible or indivisible
     * @param previousPropertyId an identifier of a predecessor token (0 for new tokens)
     * @param category a category for the new tokens (can be "")
     * @param subCategory a subcategory for the new tokens (can be "")
     * @param label label
     * @param website an URL for further information about the new tokens (can be "")
     * @param info info
     * @param amount the number of tokens to create
     * @return The hex-encoded raw transaction
     */
    public String createPropertyHex(Ecosystem ecosystem, PropertyType propertyType, Long previousPropertyId,
                                    String category, String subCategory, String label, String website, String info,
                                    OmniValue amount) {
        String rawTxHex = String.format("00000032%02x%04x%08x%s00%s00%s00%s00%s00%016x",
                ecosystem.value(),
                propertyType.value(),
                previousPropertyId,
                toHexString(category),
                toHexString(subCategory),
                toHexString(label),
                toHexString(website),
                toHexString(info),
                amount.getWilletts());
        return rawTxHex;
    }

    /**
     * Creates a hex-encoded raw transaction of type 51: "create a property via crowdsale with variable supply".
     * @param ecosystem main or test ecosystem
     * @param propertyType divisible or indivisible
     * @param previousPropertyId an identifier of a predecessor token (0 for new crowdsales)
     * @param category a category for the new tokens (can be "")
     * @param subCategory a subcategory for the new tokens (can be "")
     * @param label label
     * @param website an URL for further information about the new tokens (can be "")
     * @param info info
     * @param propertyDesired the identifier of a token eligible to participate in the crowdsale
     * @param tokensPerUnit the amount of tokens granted per unit invested in the crowdsale
     * @param deadline the deadline of the crowdsale as Unix timestamp
     * @param earlyBirdBonus an early bird bonus for participants in percent per week
     * @param issuerBonus a percentage of tokens that will be granted to the issuer
     * @return The hex-encoded raw transaction
     */
    public String createCrowdsaleHex(Ecosystem ecosystem, PropertyType propertyType, Long previousPropertyId,
                                     String category, String subCategory, String label, String website, String info,
                                     CurrencyID propertyDesired, Long tokensPerUnit, Long deadline, Byte earlyBirdBonus,
                                     Byte issuerBonus) {
        String rawTxHex = String.format("00000033%02x%04x%08x%s00%s00%s00%s00%s00%08x%016x%016x%02x%02x",
                ecosystem.value(),
                propertyType.value(),
                previousPropertyId,
                toHexString(category),
                toHexString(subCategory),
                toHexString(label),
                toHexString(website),
                toHexString(info),
                propertyDesired.getValue(),
                tokensPerUnit,
                deadline,
                earlyBirdBonus,
                issuerBonus);
        return rawTxHex;
    }

    /**
     * Creates a hex-encoded raw transaction of type 53: "close a crowdsale manually".
     * @param currencyId currency id of crowdsale
     * @return The hex-encoded raw transaction
     */
    public String createCloseCrowdsaleHex(CurrencyID currencyId) {
        String rawTxHex = String.format("00000035%08x", currencyId.getValue());
        return rawTxHex;
    }

    /**
     * Creates a hex-encoded raw transaction of type 54: "create a managed property with variable supply".
     * @param ecosystem Omni ecosystem
     * @param propertyType divisible or indivisible
     * @param previousPropertyId an identifier of a predecessor token (0 for new tokens)
     * @param category a category for the new tokens (can be "")
     * @param subCategory a subcategory for the new tokens (can be "")
     * @param label label
     * @param website an URL for further information about the new tokens (can be "")
     * @param info info
     * @return The hex-encoded raw transaction
     */
    public String createManagedPropertyHex(Ecosystem ecosystem, PropertyType propertyType, Long previousPropertyId,
                                           String category, String subCategory, String label, String website,
                                           String info) {
        String rawTxHex = String.format("00000036%02x%04x%08x%s00%s00%s00%s00%s00",
                ecosystem.value(),
                propertyType.value(),
                previousPropertyId,
                toHexString(category),
                toHexString(subCategory),
                toHexString(label),
                toHexString(website),
                toHexString(info));
        return rawTxHex;
    }

    /**
     * Creates a hex-encoded raw transaction of type 55: "grant tokens for a managed property".
     * @param currencyId currency id for grant
     * @param amount amount to grant
     * @param memo memo
     * @return The hex-encoded raw transaction
     */
    public String createGrantTokensHex(CurrencyID currencyId, OmniValue amount, String memo) {
        String rawTxHex = String.format("00000037%08x%016x%s00", currencyId.getValue(), amount.getWilletts(), toHexString(memo));
        return rawTxHex;
    }

    /**
     * Creates a hex-encoded raw transaction of type 56: "revoke tokens of a managed property".
     * @param currencyId currency id for revoke
     * @param amount amount to revoke
     * @param memo memo
     * @return The hex-encoded raw transaction
     */
    public String createRevokeTokensHex(CurrencyID currencyId, OmniValue amount, String memo) {
        String rawTxHex = String.format("00000038%08x%016x%s00", currencyId.getValue(), amount.getWilletts(), toHexString(memo));
        return rawTxHex;
    }

    /**
     * Creates a hex-encoded raw transaction of type 70: "change manager of a managed property".
     * @param currencyId currency id to change manager
     * @return The hex-encoded raw transaction
     */
    public String createChangePropertyManagerHex(CurrencyID currencyId) {
        String rawTxHex = String.format("00000046%08x", currencyId.getValue());
        return rawTxHex;
    }

    /**
     * Converts a UTF-8 encoded String into a hexadecimal string representation.
     *
     * @param str The string
     * @return The hexadecimal representation
     */
    static String toHexString(String str) {
        byte[] ba = str.getBytes(StandardCharsets.UTF_8);
        return toHexString(ba);
    }

    /**
     * Converts a byte array into a hexadecimal string representation.
     *
     * @param ba The byte array
     * @return The hexadecimal representation
     */
    static String toHexString(byte[] ba) {
        StringBuilder str = new StringBuilder();
        for (byte b : ba) {
            str.append(String.format("%02x", b));
        }
        return str.toString();
    }

    /**
     * Convert a hexadecimal string representation of binary data
     * to byte array.
     *
     * @param hex Hexadecimal string
     * @return binary data
     */
    static byte[] hexToBinary(String hex) {
        int length = hex.length();
        byte[] bin = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            int hi = Character.digit(hex.charAt(i), 16);
            int lo = Character.digit(hex.charAt(i+1), 16);
            bin[i / 2] = (byte) (( hi << 4) + lo);
        }
        return bin;
    }
}
