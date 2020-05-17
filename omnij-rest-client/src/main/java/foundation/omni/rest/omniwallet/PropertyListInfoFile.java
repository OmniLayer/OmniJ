package foundation.omni.rest.omniwallet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import foundation.omni.rest.omniwallet.json.OmniwalletClientModule;
import foundation.omni.rest.omniwallet.json.OmniwalletPropertiesListResponse;
import org.bitcoinj.params.MainNetParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 *  Utility to read OmniwalletPropertiesListResponse from an Input Stream (resource) or from
 *  a string.
 */
public class PropertyListInfoFile {

    public static OmniwalletPropertiesListResponse readFromInputStream(InputStream inputStream) {
        String json;
        try {
            json = readStringFromInputStream(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return readFromString(json);
    }

    public static OmniwalletPropertiesListResponse readFromString(String json) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new OmniwalletClientModule(MainNetParams.get()));
        OmniwalletPropertiesListResponse response;
        try {
            response = mapper.readValue(json, OmniwalletPropertiesListResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    private static String readStringFromInputStream(InputStream inputStream)
            throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br
                     = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString();
    }
}
