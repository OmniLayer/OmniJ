package foundation.omni.rest.omniwallet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;

/**
 * For mapping to/from Omniwallet backup json files
 */
public class WalletBackupEntry {
    private final String address;
    private final String privkey;
    private final String pubkey;

    public WalletBackupEntry(@JsonProperty("address") String address,
                             @JsonProperty("privkey") String privkey,
                             @JsonProperty("pubkey") String pubkey) {
        this.address = address;
        this.privkey = privkey;
        this.pubkey = pubkey;
    }


    @JsonIgnore
    public Address getAddressObject() {
        try {
            return new Address(null, address);
        } catch (AddressFormatException e) {
            throw new RuntimeException("Bitcoin AddressFormatException");
        }
    }

    public String getAddress() {
        return address;
    }

    public String getPrivkey() {
        return privkey;
    }

    public String getPubkey() {
        return pubkey;
    }
}
