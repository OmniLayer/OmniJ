package org.mastercoin.rpc;

import com.google.bitcoin.core.Address;
import com.google.bitcoin.core.Sha256Hash;
import com.msgilligan.bitcoin.rpc.BitcoinClient;
import org.mastercoin.CurrencyID;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
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

    public MastercoinClient(URL server, String rpcuser, String rpcpassword) throws IOException {
        super(server, rpcuser, rpcpassword);
    }

    public Sha256Hash send_MP(Address fromAddress, Address toAddress, CurrencyID currency, BigDecimal amount) throws IOException {
        List<Object> params = Arrays.asList((Object) fromAddress.toString(), toAddress.toString(), currency.intValue(), amount);
        Map<String, Object> response = send("send_MP", params);
        String txid = (String) response.get("result");
        Sha256Hash hash = new Sha256Hash(txid);
        return hash;
    }

    public BigDecimal getbalance_MP(Address address, CurrencyID currency) throws IOException {
        List<Object> params = Arrays.asList((Object) address.toString(), currency.intValue());
        Map<String, Object> response = send("getbalance_MP", params);
        Double balanceBTCd = (Double) response.get("result");
        // Beware of the new BigDecimal(double d) constructor, it results in unexpected/undesired values.
        BigDecimal balanceBTC = BigDecimal.valueOf(balanceBTCd);
        return balanceBTC;
    }

    public List<Object> getallbalancesforid_MP(CurrencyID currency) throws IOException {
        List<Object> params = Arrays.asList((Object) currency.intValue());
        Map<String, Object> response = send("getallbalancesforid_MP", params);
        @SuppressWarnings("unchecked")
        List<Object> balances = (List<Object>) response.get("result");
        return balances;
    }
}
