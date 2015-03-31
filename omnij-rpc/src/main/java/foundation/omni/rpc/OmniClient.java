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
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Pure Java Bitcoin and Mastercoin JSON-RPC client with camelCase method names.
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

    public Map<String, Object> getinfo_MP() throws JsonRPCException, IOException {
        Map<String, Object> result = (Map<String, Object>) send("getinfo_MP", null);
        return result;
    }

    public List<SmartPropertyListInfo> listproperties_MP() throws JsonRPCException, IOException {
        List<Map<String, Object>> result = (List<Map<String, Object>>)  send("listproperties_MP", null);

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

    public Map<String, Object> getproperty_MP(CurrencyID currency) throws JsonRPCException, IOException {
        List<Object> params = Arrays.asList((Object) currency.longValue());
        Map<String, Object> result = (Map<String, Object>)  send("getproperty_MP", params);
        return result;
    }

    public Sha256Hash send_MP(Address fromAddress, Address toAddress, CurrencyID currency, BigDecimal amount) throws JsonRPCException, IOException {
        List<Object> params = Arrays.asList((Object) fromAddress.toString(), toAddress.toString(), currency.longValue(), amount.toPlainString());
        String txid = (String) send("send_MP", params);
        Sha256Hash hash = new Sha256Hash(txid);
        return hash;
    }
    
    public Sha256Hash sendrawtx_MP(Address fromAddress, String rawTxHex) throws JsonRPCException, IOException {
        return sendrawtx_MP(fromAddress, rawTxHex, null);
    }

    public Sha256Hash sendrawtx_MP(Address fromAddress, String rawTxHex, Address referenceAddress)
            throws JsonRPCException, IOException {
        List<Object> params = Arrays.asList((Object) fromAddress.toString(), rawTxHex, referenceAddress.toString());
        String txid = (String)  send("sendrawtx_MP", params);
        return new Sha256Hash(txid);
    }

    public List<Map<String, Object>> getactivedexsells_MP() throws JsonRPCException, IOException {
        List<Map<String, Object>>  result = (List<Map<String, Object>>) send("getactivedexsells_MP", null);
        return result;
    }

    public MPBalanceEntry getbalance_MP(Address address, CurrencyID currency) throws JsonRPCException, IOException, ParseException {
        List<Object> params = Arrays.asList((Object) address.toString(), currency.longValue());
        Map<String, String> result = (Map<String, String>)  send("getbalance_MP", params);
        BigDecimal balanceBTC = (BigDecimal) jsonDecimalFormat.parse(result.get("balance"));
        BigDecimal reservedBTC = (BigDecimal) jsonDecimalFormat.parse(result.get("reserved"));
        MPBalanceEntry entry = new MPBalanceEntry(address, balanceBTC, reservedBTC);
        return entry;
    }

    public List<MPBalanceEntry> getallbalancesforid_MP(CurrencyID currency) throws JsonRPCException, IOException, ParseException, AddressFormatException {
        List<Object> params = Arrays.asList((Object) currency.longValue());
        List<Map<String, Object>> untypedBalances = (List<Map<String, Object>>) send("getallbalancesforid_MP", params);
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

    public Map<String, Object> getTransactionMP(Sha256Hash txid) throws JsonRPCException, IOException {
        List<Object> params = Arrays.asList((Object) txid.toString());
        Map<String, Object> transaction = (Map<String, Object>) send("gettransaction_MP", params);
        return transaction;
    }

    public Sha256Hash sendToOwnersMP(Address fromAddress, CurrencyID currency, BigDecimal amount) throws JsonRPCException, IOException {
        List<Object> params = Arrays.asList((Object) fromAddress.toString(), currency.longValue(), amount.toPlainString());
        String txid = (String) send("sendtoowners_MP", params);
        Sha256Hash hash = new Sha256Hash(txid);
        return hash;
    }

}
