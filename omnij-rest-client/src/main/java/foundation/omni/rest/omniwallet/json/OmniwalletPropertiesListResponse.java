package foundation.omni.rest.omniwallet.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Java POJO for /v1/properties/list response
 */
public class OmniwalletPropertiesListResponse {
    private final String status;
    private final  List<OmniwalletPropertyInfo> propertyInfoList;

    @JsonCreator
    public OmniwalletPropertiesListResponse(@JsonProperty("status") String status,
                                            @JsonProperty("properties") List<OmniwalletPropertyInfo> propertyInfoList) {
        this.status = status;
        this.propertyInfoList = propertyInfoList;
    }

    public String getStatus() {
        return status;
    }

    public List<OmniwalletPropertyInfo> getPropertyInfoList() {
        return propertyInfoList;
    }
}
