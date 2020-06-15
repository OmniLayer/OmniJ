package foundation.omni.netapi.omniwallet.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Jackson POJO wrapper for raw Omniwallet address balance
 * Contains a list of balances for all properties owned by this address
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OmniwalletAddressBalance {
    private final List<OmniwalletAddressPropertyBalance> balance;

    public OmniwalletAddressBalance(@JsonProperty("balance") List<OmniwalletAddressPropertyBalance> balance) {
        this.balance = balance;
    }
    
    public List<OmniwalletAddressPropertyBalance> getBalance() {
        return balance;
    }
}
