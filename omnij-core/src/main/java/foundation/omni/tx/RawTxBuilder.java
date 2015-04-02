package foundation.omni.tx;

import foundation.omni.CurrencyID;
import foundation.omni.Ecosystem;
import foundation.omni.PropertyType;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;

/**
 * Build hex-encoded raw Omni transactions
 */
public class RawTxBuilder {

    /**
     * Creates a hex-encoded raw transaction of type 0: "Simple Send".
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
     * Creates a hex-encoded raw transaction of type 20: "sell mastercoin for bitcoin".
     *
     * Currency amounts are Long values in Satoshis/Willets
     *
     * @param currencyId
     * @param amountForSale
     * @param amountDesired
     * @param paymentWindow
     * @param commitmentFee
     * @param action
     * @return
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
     * Creates a hex-encoded raw transaction of type 50: "create property with fixed supply".
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
     * Converts an UTF-8 encoded String into a hexadecimal string representation.
     *
     * @param str The string
     * @return The hexadecimal representation
     */
    String toHexString(String str) {
        byte[] ba = new byte[0];
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
    String toHexString(byte[] ba) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < ba.length; i++) {
            str.append(String.format("%x", ba[i]));
        }
        return str.toString();
    }

}
