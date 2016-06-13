package foundation.omni.rest.omniwallet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bitcoinj.core.Address;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Define JSON format of Omniwallet backup file for Jackson JSON library.
 * Also provide static methods for reading and writing from a File.
 */
public class WalletBackupFile {
    private List<WalletBackupEntry> addresses = new ArrayList<>();

    public List<WalletBackupEntry> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<WalletBackupEntry> entries) {
        this.addresses = entries;
    }

    @JsonIgnore
    public List<Address> getAddressList() {
        return addresses.stream()
                .map(WalletBackupEntry::getAddressObject)
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public void setAddressList(List<Address> addresses) {
        this.addresses = addresses.stream()
                .map(WalletBackupEntry::new)
                .collect(Collectors.toList());;
    }


    public static WalletBackupFile read(File file) throws IOException {
        ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally
        WalletBackupFile backup = mapper.readValue(file, WalletBackupFile.class);
        return backup;
    }

    public static void save(File file, WalletBackupFile backup) throws IOException {
        ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally
        mapper.writeValue(file, backup);
    }
}
