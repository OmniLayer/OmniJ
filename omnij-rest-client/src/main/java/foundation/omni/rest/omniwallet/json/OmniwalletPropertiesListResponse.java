package foundation.omni.rest.omniwallet.json;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Java POJO for /v1/properties/list response.
 * {@code additionalProperties} is here so we can add a {@code blockheight} in text file
 * representations of this response so we can know how old they are.
 */
public class OmniwalletPropertiesListResponse {
    private final String status;
    private final  List<OmniwalletPropertyInfo> propertyInfoList;
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<>();

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

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
