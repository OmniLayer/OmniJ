package org.mastercoin.rpc;

import com.google.bitcoin.core.Address;
import com.google.bitcoin.core.AddressFormatException;
import com.google.bitcoin.core.Sha256Hash;
import com.msgilligan.bitcoin.rpc.BitcoinClient;
import com.msgilligan.bitcoin.rpc.JsonRPCException;
import org.mastercoin.CurrencyID;

import java.io.IOException;
import java.math.BigDecimal;
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
public class MastercoinClient extends BitcoinClient {

    public static Sha256Hash zeroHash = new Sha256Hash("0000000000000000000000000000000000000000000000000000000000000000");
    private DecimalFormat jsonDecimalFormat;

    public MastercoinClient(URL server, String rpcuser, String rpcpassword) throws IOException {
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
        Map<String, Object> response = send("getinfo_MP", null);

        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) response.get("result");
        return result;
    }

    public Sha256Hash send_MP(Address fromAddress, Address toAddress, CurrencyID currency, BigDecimal amount) throws JsonRPCException, IOException {
        List<Object> params = Arrays.asList((Object) fromAddress.toString(), toAddress.toString(), currency.intValue(), amount.toString());
        Map<String, Object> response = send("send_MP", params);
        String txid = (String) response.get("result");
        Sha256Hash hash = new Sha256Hash(txid);
        return hash;
    }

    public MPBalanceEntry getbalance_MP(Address address, CurrencyID currency) throws JsonRPCException, IOException, ParseException {
        List<Object> params = Arrays.asList((Object) address.toString(), currency.intValue());
        Map<String, Object> response = send("getbalance_MP", params);
        Map<String, String> result = (Map<String, String>) response.get("result");
        BigDecimal balanceBTC = (BigDecimal) jsonDecimalFormat.parse(result.get("balance"));
        BigDecimal reservedBTC = (BigDecimal) jsonDecimalFormat.parse(result.get("reserved"));
        MPBalanceEntry entry = new MPBalanceEntry(address, balanceBTC, reservedBTC);
        return entry;
    }

    public List<MPBalanceEntry> getallbalancesforid_MP(CurrencyID currency) throws JsonRPCException, IOException, ParseException, AddressFormatException {
        List<Object> params = Arrays.asList((Object) currency.intValue());
        Map<String, Object> response = send("getallbalancesforid_MP", params);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> untypedBalances = (List<Map<String, Object>>) response.get("result");
        List<MPBalanceEntry> balances = new ArrayList<MPBalanceEntry>(untypedBalances.size());
        for (Map map : untypedBalances) {
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
        Map<String, Object> response = send("gettransaction_MP", params);

        @SuppressWarnings("unchecked")
        Map<String, Object> transaction = (Map<String, Object>) response.get("result");
        return transaction;
    }

    public Sha256Hash sendToOwnersMP(Address fromAddress, CurrencyID currency, BigDecimal amount) throws JsonRPCException, IOException {
        List<Object> params = Arrays.asList((Object) fromAddress.toString(), currency.intValue(), amount.toString());
        Map<String, Object> response = send("sendtoowners_MP", params);
        String txid = (String) response.get("result");
        Sha256Hash hash = new Sha256Hash(txid);
        return hash;
    }

}
