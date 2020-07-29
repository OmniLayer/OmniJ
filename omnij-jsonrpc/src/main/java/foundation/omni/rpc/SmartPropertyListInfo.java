package foundation.omni.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import foundation.omni.CurrencyID;

/**
 * Result for a single property from {@code "omni_listproperties"}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SmartPropertyListInfo {
    private final CurrencyID  propertyid;
    private final String      name;
    private final String      category;
    private final String      subcategory;
    private final String      data;
    private final String      url;
    private final boolean     divisible;

    public SmartPropertyListInfo(@JsonProperty("propertyid")    CurrencyID propertyid,
                                 @JsonProperty("name")          String name,
                                 @JsonProperty("category")      String category,
                                 @JsonProperty("subCategory")   String subCategory,
                                 @JsonProperty("data")          String data,
                                 @JsonProperty("url")           String url,
                                 @JsonProperty("divisible")     boolean divisible) {
        this.propertyid = propertyid;
        this.name = name;
        this.category = category;
        this.subcategory = subCategory;
        this.data = data;
        this.url = url;
        this.divisible = divisible;
    }

    public CurrencyID getPropertyid() {
        return propertyid;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public String getData() {
        return data;
    }

    public String getUrl() {
        return url;
    }

    public boolean getDivisible() {
        return divisible;
    }
}
