package foundation.omni.rpc;

import foundation.omni.CurrencyID;

/**
 * Result for a single property from {@code "omni_listproperties"}
 */
public class SmartPropertyListInfo {
    private final CurrencyID  id;               // propertyid in JSON
    private final String      name;
    private final String      category;
    private final String      subCategory;      // subcategory (all lowercase) in JSON
    private final String      data;
    private final String      url;    // Should this be a URL or URI type? (String appears to be a hostname, not URL)
    private final Boolean     divisible;

    public SmartPropertyListInfo(CurrencyID id, String name, String category, String subCategory, String data, String url, Boolean divisible) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.subCategory = subCategory;
        this.data = data;
        this.url = url;
        this.divisible = divisible;
    }

    public CurrencyID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getSubCategory() {
        return subCategory;
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
