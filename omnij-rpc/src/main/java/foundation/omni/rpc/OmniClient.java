package foundation.omni.rpc;

import com.msgilligan.bitcoin.rpc.RPCConfig;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Sha256Hash;
import com.msgilligan.bitcoin.rpc.BitcoinClient;
import com.msgilligan.bitcoin.rpc.JsonRPCException;
import foundation.omni.CurrencyID;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Pure Java Bitcoin and Omni Core JSON-RPC client with camelCase method names.
 */
public class OmniClient extends BitcoinClient {

    public static Sha256Hash zeroHash = new Sha256Hash("0000000000000000000000000000000000000000000000000000000000000000");
    private DecimalFormat jsonDecimalFormat;

    public OmniClient(RPCConfig config) throws IOException {
        this(config.getURI(), config.getUsername(), config.getPassword());
    }

    public OmniClient(URI server, String rpcuser, String rpcpassword) throws IOException {
        super(server, rpcuser, rpcpassword);
        // Create a DecimalFormat that fits our requirements
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        String pattern = "#,##0.0#";
        jsonDecimalFormat = new DecimalFormat(pattern, symbols);
        jsonDecimalFormat.setParseBigDecimal(true);
    }

    /**
     * Returns various state information of Omni Core and the Omni Layer protocol.
     *
     * @return An object with state information
     */
    public Map<String, Object> getinfo_MP() throws JsonRPCException, IOException {
        Map<String, Object> result = send("getinfo_MP", null);
        return result;
    }

    /**
     * Lists all currencies, smart properties and tokens.
     *
     * @return A list with short information
     */
    public List<SmartPropertyListInfo> listproperties_MP() throws JsonRPCException, IOException {
        List<Map<String, Object>> result = send("listproperties_MP", null);

        List<SmartPropertyListInfo> propList = new ArrayList<SmartPropertyListInfo>();
        for (Map jsonProp : result) {
            // TODO: Should this mapping be done by Jackson?
            Number idnum = (Number) jsonProp.get("propertyid");
            CurrencyID id = new CurrencyID(idnum.longValue());
            String name = (String) jsonProp.get("name");
            String category = (String) jsonProp.get("category");
            String subCategory = (String) jsonProp.get("subcategory");
            String data = (String) jsonProp.get("data");
            String url = (String) jsonProp.get("url");
            Boolean divisible = (Boolean) jsonProp.get("divisible");
            SmartPropertyListInfo prop = new SmartPropertyListInfo(id,
                    name,
                    category,
                    subCategory,
                    data,
                    url,
                    divisible);
            propList.add(prop);
        }
        return propList;
    }

    /**
     * Returns information about the given currency, property, or token.
     *
     * @param currency The identifier to look up
     * @return An object with detailed information
     */
    public Map<String, Object> getproperty_MP(CurrencyID currency) throws JsonRPCException, IOException {
        List<Object> params = createParamList(currency.longValue());
        Map<String, Object> result = send("getproperty_MP", params);
        return result;
    }

    /**
     * Creates and broadcasts a "simple send" transaction.
     *
     * @param fromAddress The address to spent from
     * @param toAddress   The address to send to
     * @param currency    The identifier of the token to transfer
     * @param amount      The amount to transfer
     * @return The hash of the transaction
     */
    public Sha256Hash send_MP(Address fromAddress, Address toAddress, CurrencyID currency, BigDecimal amount)
            throws JsonRPCException, IOException {
        List<Object> params = createParamList(fromAddress.toString(), toAddress.toString(), currency.longValue(),
                                              amount.toPlainString());
        String txid = send("send_MP", params);
        Sha256Hash hash = new Sha256Hash(txid);
        return hash;
    }

    /**
     * Broadcasts a raw Omni Layer transaction.
     *
     * @param fromAddress The address to send from
     * @param rawTxHex    The hex-encoded raw transaction
     * @return The hash of the transaction
     */
    public Sha256Hash sendrawtx_MP(Address fromAddress, String rawTxHex) throws JsonRPCException, IOException {
        return sendrawtx_MP(fromAddress, rawTxHex, null);
    }

    /**
     * Broadcasts a raw Omni Layer transaction with reference address.
     *
     * @param fromAddress      The address to send from
     * @param rawTxHex         The hex-encoded raw transaction
     * @param referenceAddress The reference address
     * @return The hash of the transaction
     */
    public Sha256Hash sendrawtx_MP(Address fromAddress, String rawTxHex, Address referenceAddress)
            throws JsonRPCException, IOException {
        List<Object> params = createParamList(fromAddress.toString(), rawTxHex);
        if (referenceAddress != null) {
            params.add(referenceAddress.toString());
        }
        String txid = send("sendrawtx_MP", params);
        return new Sha256Hash(txid);
    }

    /**
     * Lists currently active offers on the distributed BTC/MSC exchange.
     *
     * @return A list with information about the active offers
     */
    public List<Map<String, Object>> getactivedexsells_MP() throws JsonRPCException, IOException {
        List<Map<String, Object>> result = send("getactivedexsells_MP", null);
        return result;
    }

    /**
     * Returns the balance for a given address and property.
     *
     * @param address  The address to look up
     * @param currency The identifier of the token to look up
     * @return The available and reserved balance
     */
    public MPBalanceEntry getbalance_MP(Address address, CurrencyID currency)
            throws JsonRPCException, IOException, ParseException {
        List<Object> params = createParamList(address.toString(), currency.longValue());
        Map<String, String> result = send("getbalance_MP", params);
        BigDecimal balanceBTC = (BigDecimal) jsonDecimalFormat.parse(result.get("balance"));
        BigDecimal reservedBTC = (BigDecimal) jsonDecimalFormat.parse(result.get("reserved"));
        MPBalanceEntry entry = new MPBalanceEntry(address, balanceBTC, reservedBTC);
        return entry;
    }

    /**
     * Returns a list of balances for a given identifier.
     *
     * @param currency The identifier of the token to look up
     * @return A list containing addresses, and the associated available and reserved balances
     */
    public List<MPBalanceEntry> getallbalancesforid_MP(CurrencyID currency)
            throws JsonRPCException, IOException, ParseException, AddressFormatException {
        List<Object> params = createParamList(currency.longValue());
        List<Map<String, Object>> untypedBalances = send("getallbalancesforid_MP", params);
        List<MPBalanceEntry> balances = new ArrayList<MPBalanceEntry>(untypedBalances.size());
        for (Map map : untypedBalances) {
            // TODO: Should this mapping be done by Jackson?
            BigDecimal balance;
            BigDecimal reserved;
            String addressString = (String) map.get("address");
            Address address = new Address(null, addressString);
            Object balanceJson = map.get("balance");
            Object reservedJson = map.get("reserved");
            /* Assume that if balanceJson field is of type String, so is reserved */
            /* The RPCs have been changing here, but currently they should be using Strings */
            if (balanceJson instanceof String) {
                balance = (BigDecimal) jsonDecimalFormat.parse((String) balanceJson);
                reserved = (BigDecimal) jsonDecimalFormat.parse((String) reservedJson);
            } else if (balanceJson instanceof Integer) {
                balance = new BigDecimal((Integer) balanceJson);
                reserved = new BigDecimal((Integer) reservedJson);

            } else {
                throw new RuntimeException("unexpected data type");
            }
            MPBalanceEntry balanceEntry = new MPBalanceEntry(address, balance, reserved);
            balances.add(balanceEntry);
        }
        return balances;
    }

    /**
     * Returns information about an Omni Layer transaction.
     *
     * @param txid The hash of the transaction to look up
     * @return Information about the transaction
     */
    public Map<String, Object> getTransactionMP(Sha256Hash txid) throws JsonRPCException, IOException {
        List<Object> params = createParamList(txid.toString());
        Map<String, Object> transaction = send("gettransaction_MP", params);
        return transaction;
    }

    /**
     * Creates and broadcasts a "send to owners" transaction.
     *
     * @param fromAddress The address to spent from
     * @param currency    The identifier of the token to distribute
     * @param amount      The amount to distribute
     * @return The hash of the transaction
     */
    public Sha256Hash sendToOwnersMP(Address fromAddress, CurrencyID currency, BigDecimal amount)
            throws JsonRPCException, IOException {
        List<Object> params = createParamList(fromAddress.toString(), currency.longValue(), amount.toPlainString());
        String txid = send("sendtoowners_MP", params);
        Sha256Hash hash = new Sha256Hash(txid);
        return hash;
    }

}
