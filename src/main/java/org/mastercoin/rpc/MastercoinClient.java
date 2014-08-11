package org.mastercoin.rpc;

import com.google.bitcoin.core.Address;
import com.google.bitcoin.core.AddressFormatException;
import com.google.bitcoin.core.Sha256Hash;
import com.msgilligan.bitcoin.rpc.BitcoinClient;
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
 * User: sean
 * Date: 6/16/14
 * Time: 5:34 PM
 */
public class MastercoinClient extends BitcoinClient {

    public static Sha256Hash zeroHash = new Sha256Hash("0000000000000000000000000000000000000000000000000000000000000000");
    public DecimalFormat jsonDecimalFormat;

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

    public Sha256Hash send_MP(Address fromAddress, Address toAddress, CurrencyID currency, BigDecimal amount) throws IOException {
        List<Object> params = Arrays.asList((Object) fromAddress.toString(), toAddress.toString(), currency.intValue(), amount);
        Map<String, Object> response = send("send_MP", params);
        String txid = (String) response.get("result");
        Sha256Hash hash = new Sha256Hash(txid);
        return hash;
    }

    public BigDecimal getbalance_MP(Address address, CurrencyID currency) throws IOException, ParseException {
        boolean expectString = true;
        List<Object> params = Arrays.asList((Object) address.toString(), currency.intValue());
        Map<String, Object> response = send("getbalance_MP", params);
        BigDecimal balanceBTC;
        if (expectString) {
            String balanceBTCd = (String) response.get("result");
            balanceBTC = (BigDecimal) jsonDecimalFormat.parse(balanceBTCd);
        } else {
            Double balanceBTCd = (Double) response.get("result");
            // Beware of the new BigDecimal(double d) constructor, it results in unexpected/undesired values.
            balanceBTC = BigDecimal.valueOf(balanceBTCd);
        }
        return balanceBTC;
    }

    public List<MPBalanceEntry> getallbalancesforid_MP(CurrencyID currency) throws IOException, ParseException, AddressFormatException {
        List<Object> params = Arrays.asList((Object) currency.intValue());
        Map<String, Object> response = send("getallbalancesforid_MP", params);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> untypedBalances = (List<Map<String, Object>>) response.get("result");
        List<MPBalanceEntry> balances = new ArrayList<>(untypedBalances.size());
        for (Map map : untypedBalances) {
            BigDecimal balance;
            BigDecimal reservedByOffer;
            BigDecimal reservedByAccept;
            String addressString = (String) map.get("address");
            Address address = new Address(null, addressString);
            Object balanceJson = map.get("balance");
            Object reservedByOfferJson = map.get("reservedbyoffer");
//            Object reservedByAcceptJson = map.get("reservedByAccept");
            if (balanceJson instanceof Integer) {
                balance = new BigDecimal((Integer) balanceJson);
                reservedByOffer = new BigDecimal((Integer) reservedByOfferJson);
                reservedByAccept = new BigDecimal(0);
//                reservedByAccept = new BigDecimal((Integer) reservedByAcceptJson);

            } else {
                balance = (BigDecimal) jsonDecimalFormat.parse((String) balanceJson);
                reservedByOffer = (BigDecimal) jsonDecimalFormat.parse((String) reservedByOfferJson);
                reservedByAccept = new BigDecimal(0);
//                reservedByAccept = (BigDecimal) jsonDecimalFormat.parse((String) reservedByAcceptJson);
            }
            MPBalanceEntry balanceEntry = new MPBalanceEntry(address, balance, reservedByOffer, reservedByAccept);
            balances.add(balanceEntry);
        }
        return balances;
    }

    public Map<String, Object> getTransactionMP(Sha256Hash txid) throws IOException {
        List<Object> params = Arrays.asList((Object) txid.toString());
        Map<String, Object> response = send("gettransaction_MP", params);

        @SuppressWarnings("unchecked")
        Map<String, Object> transaction = (Map<String, Object>) response.get("result");
        return transaction;
    }

    public Sha256Hash sendToOwnersMP(Address fromAddress, CurrencyID currency, BigDecimal amount) throws IOException {
        List<Object> params = Arrays.asList((Object) fromAddress.toString(), currency.intValue(), amount);
        Map<String, Object> response = send("sendtoowners_MP", params);
        String txid = (String) response.get("result");
        Sha256Hash hash = new Sha256Hash(txid);
        return hash;
    }

}
