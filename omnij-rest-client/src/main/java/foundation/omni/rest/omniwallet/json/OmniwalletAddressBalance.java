package foundation.omni.rest.omniwallet.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Jackson POJO wrapper for raw Omniwallet address balance
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
