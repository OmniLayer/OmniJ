package foundation.omni.rest.omniwallet;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Define JSON format of Omniwallet backup file for Jackson JSON library.
 * Also provide static methods for reading and writing from a File.
 */
public class WalletBackupFile {
    public List<WalletBackupEntry> addresses;

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
