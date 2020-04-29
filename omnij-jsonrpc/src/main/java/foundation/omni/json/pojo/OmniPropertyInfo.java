package foundation.omni.json.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import foundation.omni.CurrencyID;
import foundation.omni.OmniValue;
import foundation.omni.net.OmniMainNetParams;
import foundation.omni.rpc.SmartPropertyListInfo;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Sha256Hash;

import java.math.BigDecimal;

/**
 * JSON bean representing the set of information on an Omni Property as returned by the
 * {@code omni_getproperty} RPC method. It is a subclass of {@code SmartPropertyListInfo}
 * because it is a superset of that information.
 */
public class OmniPropertyInfo extends SmartPropertyListInfo {
    static private final Address defaultIssuerAddress =  OmniMainNetParams.get().getExodusAddress();

    private final Address issuer;
    private final Sha256Hash creationTxId;
    private final boolean fixedIssuance;
    private final boolean managedIssuance;
    private final boolean freezingEnabled;
    private final OmniValue totalTokens;

    @JsonCreator
    public OmniPropertyInfo(@JsonProperty("propertyid") CurrencyID propertyid,
                            @JsonProperty("name") String name,
                            @JsonProperty("category") String category,
                            @JsonProperty("subCategory") String subCategory,
                            @JsonProperty("data") String data,
                            @JsonProperty("url") String url,
                            @JsonProperty("divisible") Boolean divisible,
                            @JsonProperty("issuer") Address issuer,
                            @JsonProperty("creationtxid") Sha256Hash creationTxId,
                            @JsonProperty("fixedissuance") boolean fixedIssuance,
                            @JsonProperty("managedissuance") boolean managedIssuance,
                            @JsonProperty("freezingenabled") boolean freezingEnabled,
                            @JsonProperty("totaltokens") String totalTokensString) {
        this(propertyid,
                name,
                category,
                subCategory,
                data,
                url,
                divisible,
                issuer,
                creationTxId,
                fixedIssuance,
                managedIssuance,
                freezingEnabled,
                OmniValue.of(new BigDecimal(totalTokensString), divisible));
    }

    public OmniPropertyInfo(CurrencyID propertyid,
                            String name,
                            String category,
                            String subCategory,
                            String data,
                            String url,
                            Boolean divisible,
                            Address issuer,
                            Sha256Hash creationTxId,
                            boolean fixedIssuance,
                            boolean managedIssuance,
                            boolean freezingEnabled,
                            OmniValue totalTokens) {
        super(propertyid, name, category, subCategory, data, url, divisible);
        this.issuer = issuer;
        this.creationTxId = creationTxId;
        this.fixedIssuance = fixedIssuance;
        this.managedIssuance = managedIssuance;
        this.freezingEnabled = freezingEnabled;
        this.totalTokens = totalTokens;
    }

    /**
     * Construct OmniPropertyInfo from SmartPropertyListInfo
     * Since there is no way to fetch OmniPropertyInfo for all properties with a
     * single request, this converter can be used to create a OmniPropertyInfo
     * from a SmartPropertyListInfo with default values for the extra fields.
     * TODO: Figure out a way of efficiently getting OmniPropertyInfo for all properties.
     *
     * @param sptListInfo record to adapt
     */
    public OmniPropertyInfo(SmartPropertyListInfo sptListInfo) {
        super(sptListInfo.getPropertyid(),
                sptListInfo.getName(),
                sptListInfo.getCategory(),
                sptListInfo.getSubcategory(),
                sptListInfo.getData(),
                sptListInfo.getUrl(),
                sptListInfo.getDivisible());
        this.issuer = defaultIssuerAddress;         // Use the Exodus address for now
        this.creationTxId = Sha256Hash.ZERO_HASH;
        this.fixedIssuance = true;
        this.managedIssuance = false;
        this.freezingEnabled = false;
        this.totalTokens = OmniValue.ofWilletts(0, sptListInfo.getDivisible());
    }

    public Address getIssuer() {
        return issuer;
    }

    public Sha256Hash getCreationTxId() {
        return creationTxId;
    }

    public boolean isFixedIssuance() {
        return fixedIssuance;
    }

    public boolean isManagedIssuance() {
        return managedIssuance;
    }

    public boolean isFreezingEnabled() {
        return freezingEnabled;
    }

    public OmniValue getTotalTokens() {
        return totalTokens;
    }
}
