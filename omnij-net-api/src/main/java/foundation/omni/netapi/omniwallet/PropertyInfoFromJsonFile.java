package foundation.omni.netapi.omniwallet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import foundation.omni.json.pojo.OmniPropertyInfo;
import foundation.omni.netapi.omniwallet.json.OmniwalletClientModule;
import foundation.omni.netapi.omniwallet.json.OmniwalletPropertiesListResponse;
import org.bitcoinj.params.MainNetParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

/**
 *  Utility to read OmniwalletPropertiesListResponse from an Input Stream (resource) or from
 *  a string.
 */
public class PropertyInfoFromJsonFile {
    
    public static List<OmniPropertyInfo> readPropertyInfoListFromInputStream(InputStream inputStream) {
        return readResponseFromInputStream(inputStream)
                .getPropertyInfoList().stream()
                .map(OmniwalletAbstractClient::mapToOmniPropertyInfo)
                .collect(Collectors.toList());
    }
    public static OmniwalletPropertiesListResponse readResponseFromInputStream(InputStream inputStream) {
        String json;
        try {
            json = readStringFromInputStream(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return readResponseFromString(json);
    }

    public static OmniwalletPropertiesListResponse readResponseFromString(String json) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new OmniwalletClientModule());
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
