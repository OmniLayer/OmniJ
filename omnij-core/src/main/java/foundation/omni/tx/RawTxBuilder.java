package foundation.omni.tx;

import foundation.omni.CurrencyID;
import foundation.omni.Ecosystem;
import foundation.omni.PropertyType;

import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;

/**
 * Build hex-encoded raw Omni transactions
 *
 * @author msgilligan
 * @author dexX7
 */
public class RawTxBuilder {

    /**
     * Creates a hex-encoded raw transaction of type 0: "simple send".
     */
    public String createSimpleSendHex(CurrencyID currencyId, Long amount) {
        String rawTxHex = String.format("00000000%08x%016x", currencyId.longValue(), amount);
        return rawTxHex;
    }

    /**
     * Creates a hex-encoded raw transaction of type 3: "send to owners".
     */
    public String createSendToOwnersHex(CurrencyID currencyId, Long amount) {
        String rawTxHex = String.format("00000003%08x%016x", currencyId.longValue(), amount);
        return rawTxHex;
    }

    /**
     * Creates a hex-encoded raw transaction of type 20: "sell tokens for bitcoins".
     *
     * Currency amounts are Long values in satoshis/willets
     *
     * @param currencyId Currency ID to sell MSC or TMSC only
     * @param amountForSale Amount of MSC/TMSC for sale (in willets)
     * @param amountDesired Amount of BTC desired (in satoshis)
     * @param paymentWindow Time period in blocks
     * @param commitmentFee Minimum Bitcoin transaction fee (in satoshis)
     * @param action Sell offer sub-action
     * @return The hex-encoded raw transaction
     */
    public String createDexSellOfferHex(CurrencyID currencyId, Long amountForSale, Long amountDesired,
                                        Byte paymentWindow, Long commitmentFee, Byte action) {
        String rawTxHex = String.format("00010014%08x%016x%016x%02x%016x%02x",
                currencyId.longValue(),
                amountForSale,
                amountDesired,
                paymentWindow.byteValue(),
                commitmentFee,
                action.byteValue());
        return rawTxHex;
    }

    /**
     * Creates a hex-encoded raw transaction of type 21: "trade tokens for tokens".
     */
    public String createMetaDexSellOfferHex(CurrencyID currencyForSale, Long amountForSale, CurrencyID currencyDesired,
                                            Long amountDesired, Byte action) {
        String rawTxHex = String.format("00000015%08x%016x%08x%016x%02x",
                currencyForSale.longValue(),
                amountForSale,
                currencyDesired.longValue(),
                amountDesired,
                action.byteValue());
        return rawTxHex;
    }

    /**
     * Creates a hex-encoded raw transaction of type 22: "purchase tokens with bitcoins".
     */
    public String createAcceptDexOfferHex(CurrencyID currencyId, Long amount) {
        String rawTxHex = String.format("00000016%08x%016x",
                currencyId.longValue(),
                amount);
        return rawTxHex;
    }

    /**
     * Creates a hex-encoded raw transaction of type 50: "create a property with fixed supply".
     */
    public String createPropertyHex(Ecosystem ecosystem, PropertyType propertyType, Long previousPropertyId,
                                    String category, String subCategory, String label, String website, String info,
                                    Long amount) {
        String rawTxHex = String.format("00000032%02x%04x%08x%s00%s00%s00%s00%s00%016x",
                ecosystem.byteValue(),
                propertyType.intValue(),
                previousPropertyId,
                toHexString(category),
                toHexString(subCategory),
                toHexString(label),
                toHexString(website),
                toHexString(info),
                amount);
        return rawTxHex;
    }

    /**
     * Creates a hex-encoded raw transaction of type 51: "create a property via crowdsale with variable supply".
     */
    public String createCrowdsaleHex(Ecosystem ecosystem, PropertyType propertyType, Long previousPropertyId,
                                     String category, String subCategory, String label, String website, String info,
                                     CurrencyID propertyDesired, Long tokensPerUnit, Long deadline, Byte earlyBirdBonus,
                                     Byte issuerBonus) {
        String rawTxHex = String.format("00000033%02x%04x%08x%s00%s00%s00%s00%s00%08x%016x%016x%02x%02x",
                ecosystem.byteValue(),
                propertyType.intValue(),
                previousPropertyId,
                toHexString(category),
                toHexString(subCategory),
                toHexString(label),
                toHexString(website),
                toHexString(info),
                propertyDesired.longValue(),
                tokensPerUnit,
                deadline,
                earlyBirdBonus.byteValue(),
                issuerBonus.byteValue());
        return rawTxHex;
    }

    /**
     * Creates a hex-encoded raw transaction of type 53: "close a crowdsale manually".
     */
    public String createCloseCrowdsaleHex(CurrencyID currencyId) {
        String rawTxHex = String.format("00000035%08x", currencyId.longValue());
        return rawTxHex;
    }

    /**
     * Creates a hex-encoded raw transaction of type 54: "create a managed property with variable supply".
     */
    public String createManagedPropertyHex(Ecosystem ecosystem, PropertyType propertyType, Long previousPropertyId,
                                           String category, String subCategory, String label, String website,
                                           String info) {
        String rawTxHex = String.format("00000036%02x%04x%08x%s00%s00%s00%s00%s00",
                ecosystem.byteValue(),
                propertyType.intValue(),
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
     */
    public String createGrantTokensHex(CurrencyID currencyId, Long amount, String memo) {
        String rawTxHex = String.format("00000037%08x%016x%s00", currencyId.longValue(), amount, toHexString(memo));
        return rawTxHex;
    }

    /**
     * Creates a hex-encoded raw transaction of type 56: "revoke tokens of a managed property".
     */
    public String createRevokeTokensHex(CurrencyID currencyId, Long amount, String memo) {
        String rawTxHex = String.format("00000038%08x%016x%s00", currencyId.longValue(), amount, toHexString(memo));
        return rawTxHex;
    }

    /**
     * Creates a hex-encoded raw transaction of type 70: "change manager of a managed property".
     */
    public String createChangePropertyManagerHex(CurrencyID currencyId) {
        String rawTxHex = String.format("00000046%08x", currencyId.longValue());
        return rawTxHex;
    }

    /**
     * Converts an UTF-8 encoded String into a hexadecimal string representation.
     *
     * @param str The string
     * @return The hexadecimal representation
     */
    static String toHexString(String str) {
        byte[] ba;
        try {
            ba = str.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
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
        for (int i = 0; i < ba.length; i++) {
            str.append(String.format("%x", ba[i]));
        }
        return str.toString();
    }

    /**
     * Convert a hexadecimal string representation of binary data
     * to byte array.
     *
     * @param hexString Hexadecimal string
     * @return binary data
     */
    static byte[] hexToBinary(String hexString) {
        return DatatypeConverter.parseHexBinary(hexString);
    }
}
