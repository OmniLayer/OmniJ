package foundation.omni.rest.omniwallet.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import foundation.omni.CurrencyID;
import foundation.omni.OmniValue;
import foundation.omni.json.pojo.OmniPropertyInfo;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Sha256Hash;

import java.math.BigDecimal;
import java.util.List;

/**
 * Java POJO for per-property record in /v1/properties/list response
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OmniwalletPropertyInfo {


    private final long blocktime;
    private final String category;
    private final Sha256Hash creationTxId;
    private final String data;
    private final boolean divisible;
    private final boolean fixedIssuance;
    private final boolean freezingEnabled;
    private final Address issuer;
    private final List<Object> issuances;
    private final boolean managedIssuance;
    private final String name;
    private final CurrencyID propertyid;
    private final String subcategory;
    private final OmniValue totalTokens;
    private final String url;

    public OmniwalletPropertyInfo(@JsonProperty("blocktime") long blocktime,
                                  @JsonProperty("category") String category,
                                  @JsonProperty("creationtxid") Sha256Hash creationTxId,
                                  @JsonProperty("data") String data,
                                  @JsonProperty("divisible") boolean divisible,
                                  @JsonProperty("fixedissuance") boolean fixedIssuance,
                                  @JsonProperty("freezingenabled") boolean freezingEnabled,
                                  @JsonProperty("issuer") String issuerString,
                                  @JsonProperty("issuances") List<Object> issuances,
                                  @JsonProperty("managedissuance") boolean managedIssuance,
                                  @JsonProperty("name") String name,
                                  @JsonProperty("propertyid") CurrencyID propertyid,
                                  @JsonProperty("subcategory") String subcategory,
                                  @JsonProperty("totaltokens") String totalTokensString,
                                  @JsonProperty("url") String url) {
        this.blocktime = blocktime;
        this.category = category;
        this.creationTxId = creationTxId;
        this.data = data;
        this.divisible = divisible;
        this.fixedIssuance = fixedIssuance;
        this.freezingEnabled = freezingEnabled;
        this.issuer = mapIssuer(issuerString);
        this.issuances = issuances;
        this.managedIssuance = managedIssuance;
        this.name = name;
        this.propertyid = propertyid;
        this.subcategory = subcategory;
        this.totalTokens = OmniValue.of(new BigDecimal(totalTokensString), divisible);
        this.url = url;
    }

    public long getBlocktime() {
        return blocktime;
    }

    public String getCategory() {
        return category;
    }

    public Sha256Hash getCreationTxId() {
        return creationTxId;
    }

    public String getData() {
        return data;
    }

    public boolean isDivisible() {
        return divisible;
    }

    public boolean isFixedIssuance() {
        return fixedIssuance;
    }

    public Address getIssuer() {
        return issuer;
    }

    public boolean isManagedIssuance() {
        return managedIssuance;
    }

    public String getName() {
        return name;
    }

    public CurrencyID getPropertyid() {
        return propertyid;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public OmniValue getTotalTokens() {
        return totalTokens;
    }

    public String getUrl() {
        return url;
    }

    private static Address mapIssuer(String issuerString) {
        return issuerString.equals("Satoshi Nakamoto") ?
                OmniPropertyInfo.defaultIssuerAddress : Address.fromString(null, issuerString);
    }
}


/*

{
    "properties": [
        {
            "blocktime": 1377994675,
            "category": "N/A",
            "creationtxid": "0000000000000000000000000000000000000000000000000000000000000000",
            "data": "Omni serve as the binding between Bitcoin, smart properties and contracts created on the Omni Layer.",
            "divisible": true,
            "fixedissuance": false,
            "issuer": "1EXoDusjGwvnjZUyKkxZ4UHEf77z6A5S4P",
            "managedissuance": false,
            "name": "Omni",
            "propertyid": 1,
            "subcategory": "N/A",
            "totaltokens": "617211.68177584",
            "url": "http://www.omnilayer.org"
        },
        {
            "blocktime": 1377994675,
            "category": "N/A",
            "creationtxid": "0000000000000000000000000000000000000000000000000000000000000000",
            "data": "Test Omni serve as the binding between Bitcoin, smart properties and contracts created on the Omni Layer.",
            "divisible": true,
            "fixedissuance": false,
            "issuer": "1EXoDusjGwvnjZUyKkxZ4UHEf77z6A5S4P",
            "managedissuance": false,
            "name": "Test Omni",
            "propertyid": 2,
            "subcategory": "N/A",
            "totaltokens": "563162.35759628",
            "url": "http://www.omnilayer.org"
        },
        {
            "additional records": "..."
        }
    ],
    "status": "OK"
}
 */