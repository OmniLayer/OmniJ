package foundation.omni.tx;

import foundation.omni.CurrencyID;
import foundation.omni.Ecosystem;
import foundation.omni.OmniDivisibleValue;
import foundation.omni.OmniValue;
import foundation.omni.PropertyType;
import org.bitcoinj.core.Coin;

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
    public String createSimpleSendHex(CurrencyID currencyId, OmniValue amount) {
        String rawTxHex = String.format("00000000%08x%016x", currencyId.getValue(), amount.getWillets());
        return rawTxHex;
    }

    /**
     * Creates a hex-encoded raw transaction of type 3: "send to owners".
     */
    public String createSendToOwnersHex(CurrencyID currencyId, OmniValue amount) {
        String rawTxHex = String.format("00000003%08x%016x", currencyId.getValue(), amount.getWillets());
        return rawTxHex;
    }

    /**
     * Creates a hex-encoded raw transaction of type 20: "sell tokens for bitcoins".
     *
     * Currency amounts are Long values in satoshis/willets
     *
     * @param currencyId Currency ID to sell MSC or TMSC only
     * @param amountForSale Amount of MSC/TMSC for sale
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
                amountForSale.getWillets(),
                amountDesired.value,
                paymentWindow.byteValue(),
                commitmentFee.value,
                action.byteValue());
        return rawTxHex;
    }

    /**
     * Creates a hex-encoded raw transaction of type 21: "trade tokens for tokens".
     */
    public String createMetaDexSellOfferHex(CurrencyID currencyForSale, OmniValue amountForSale, CurrencyID currencyDesired,
                                            OmniValue amountDesired, Byte action) {
        String rawTxHex = String.format("00000015%08x%016x%08x%016x%02x",
                currencyForSale.getValue(),
                amountForSale.getWillets(),
                currencyDesired.getValue(),
                amountDesired.getWillets(),
                action.byteValue());
        return rawTxHex;
    }

    /**
     * Creates a hex-encoded raw transaction of type 22: "purchase tokens with bitcoins".
     */
    public String createAcceptDexOfferHex(CurrencyID currencyId, OmniValue amount) {
        String rawTxHex = String.format("00000016%08x%016x",
                currencyId.getValue(),
                amount.getWillets());
        return rawTxHex;
    }

    /**
     * Creates a hex-encoded raw transaction of type 50: "create a property with fixed supply".
     */
    public String createPropertyHex(Ecosystem ecosystem, PropertyType propertyType, Long previousPropertyId,
                                    String category, String subCategory, String label, String website, String info,
                                    OmniValue amount) {
        String rawTxHex = String.format("00000032%02x%04x%08x%s00%s00%s00%s00%s00%016x",
                ecosystem.getValue(),
                propertyType.getValue(),
                previousPropertyId,
                toHexString(category),
                toHexString(subCategory),
                toHexString(label),
                toHexString(website),
                toHexString(info),
                amount.getWillets());
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
                ecosystem.getValue(),
                propertyType.getValue(),
                previousPropertyId,
                toHexString(category),
                toHexString(subCategory),
                toHexString(label),
                toHexString(website),
                toHexString(info),
                propertyDesired.getValue(),
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
        String rawTxHex = String.format("00000035%08x", currencyId.getValue());
        return rawTxHex;
    }

    /**
     * Creates a hex-encoded raw transaction of type 54: "create a managed property with variable supply".
     */
    public String createManagedPropertyHex(Ecosystem ecosystem, PropertyType propertyType, Long previousPropertyId,
                                           String category, String subCategory, String label, String website,
                                           String info) {
        String rawTxHex = String.format("00000036%02x%04x%08x%s00%s00%s00%s00%s00",
                ecosystem.getValue(),
                propertyType.getValue(),
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
    public String createGrantTokensHex(CurrencyID currencyId, OmniValue amount, String memo) {
        String rawTxHex = String.format("00000037%08x%016x%s00", currencyId.getValue(), amount.getWillets(), toHexString(memo));
        return rawTxHex;
    }

    /**
     * Creates a hex-encoded raw transaction of type 56: "revoke tokens of a managed property".
     */
    public String createRevokeTokensHex(CurrencyID currencyId, OmniValue amount, String memo) {
        String rawTxHex = String.format("00000038%08x%016x%s00", currencyId.getValue(), amount.getWillets(), toHexString(memo));
        return rawTxHex;
    }

    /**
     * Creates a hex-encoded raw transaction of type 70: "change manager of a managed property".
     */
    public String createChangePropertyManagerHex(CurrencyID currencyId) {
        String rawTxHex = String.format("00000046%08x", currencyId.getValue());
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
