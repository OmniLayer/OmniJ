package foundation.omni.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import foundation.omni.CurrencyID;

/**
 * Result for a single property from {@code "omni_listproperties"}
 */
public class SmartPropertyListInfo {
    private final CurrencyID  propertyid;
    private final String      name;
    private final String      category;
    private final String      subcategory;
    private final String      data;
    private final String      url;
    private final Boolean     divisible;

    public SmartPropertyListInfo(@JsonProperty("propertyid")    CurrencyID propertyid,
                                 @JsonProperty("name")          String name,
                                 @JsonProperty("category")      String category,
                                 @JsonProperty("subCategory")   String subCategory,
                                 @JsonProperty("data")          String data,
                                 @JsonProperty("url")           String url,
                                 @JsonProperty("divisible")     Boolean divisible) {
        this.propertyid = propertyid;
        this.name = name;
        this.category = category;
        this.subcategory = subCategory;
        this.data = data;
        this.url = url;
        this.divisible = divisible;
    }

    /**
     * @return currency id
     * @deprecated Use getPropertyid()
     */
    @Deprecated
    public CurrencyID getId() {
        return propertyid;
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

    /**
     * @return subcategory
     * @deprecated Use getSubcategory()
     */
    public String getSubCategory() {
        return subcategory;
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

    public Boolean getDivisible() {
        return divisible;
    }
}
