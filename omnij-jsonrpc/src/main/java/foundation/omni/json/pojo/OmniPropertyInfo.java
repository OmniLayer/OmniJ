package foundation.omni.json.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import foundation.omni.CurrencyID;
import foundation.omni.OmniDivisibleValue;
import foundation.omni.OmniValue;
import org.bitcoinj.base.Address;
import org.bitcoinj.base.BitcoinNetwork;
import org.bitcoinj.base.Coin;
import org.bitcoinj.base.LegacyAddress;
import org.bitcoinj.base.Sha256Hash;

/**
 * JSON bean representing the set of information on an Omni Property as returned by the
 * {@code omni_getproperty} RPC method. It is a subclass of {@link SmartPropertyListInfo}
 * because it is a superset of that information.
 */
public class OmniPropertyInfo extends SmartPropertyListInfo {
    private static final Address bitcoinMainNetIssuer = LegacyAddress.fromBase58("1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa", BitcoinNetwork.MAINNET);
    private static final Address bitcoinTestNetIssuer = LegacyAddress.fromBase58("mpXwg4jMtRhuSpVq4xS3HFHmCmWp9NyGKt", BitcoinNetwork.TESTNET);
    private static final Sha256Hash genesisCoinbaseTxId = Sha256Hash.wrap("4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b");
    private final Address issuer;
    private final Sha256Hash creationtxid;
    private final boolean fixedissuance;
    private final boolean managedissuance;
    private final boolean freezingenabled;
    private final OmniValue totaltokens;

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
                            @JsonProperty("totaltokens") String totaltokensString) {
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
                OmniValue.of(totaltokensString, divisible));
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
        this.creationtxid = creationTxId;
        this.fixedissuance = fixedIssuance;
        this.managedissuance = managedIssuance;
        this.freezingenabled = freezingEnabled;
        this.totaltokens = totalTokens;
    }

    /**
     * Construct OmniPropertyInfo from SmartPropertyListInfo
     * Since there is no way to fetch OmniPropertyInfo for all properties with a
     * single request, this converter can be used to create a OmniPropertyInfo
     * from a SmartPropertyListInfo with default values for the extra fields.
     * TODO: Address the fact that (in this case) we really don't have all the info and the default
     *       values we are using are potentially incorrect
     * TODO: Figure out a way of efficiently getting OmniPropertyInfo for all properties.
     *
     * @param sptListInfo record to adapt
     */
    public OmniPropertyInfo(BitcoinNetwork bitcoinNetwork, SmartPropertyListInfo sptListInfo) {
        super(sptListInfo.getPropertyid(),
                sptListInfo.getName(),
                sptListInfo.getCategory(),
                sptListInfo.getSubcategory(),
                sptListInfo.getData(),
                sptListInfo.getUrl(),
                sptListInfo.getDivisible());
        this.issuer = defaultIssuer(bitcoinNetwork); // Issuer unknown
        this.creationtxid = Sha256Hash.ZERO_HASH;       // Creation Tx unknown
        this.fixedissuance = false;                     // Not nullable so use `false`
        this.managedissuance = false;                   // Not nullable so use `false`
        this.freezingenabled = false;                   // Not nullable so use `false`
        this.totaltokens = OmniValue.ofWilletts(0, sptListInfo.getDivisible());
    }

    /**
     * Produces OmniPropertyInfo with a MainNet default issuer Address. This can result in invalid Address
     * exceptions when used on TestNet or RegTest. <b>This constructor will be removed in the next release.</b>
     *
     * @param sptListInfo record to adapt
     * @deprecated Use {@link OmniPropertyInfo#OmniPropertyInfo(BitcoinNetwork, SmartPropertyListInfo)}
     */
    @Deprecated
    public OmniPropertyInfo(SmartPropertyListInfo sptListInfo) {
        this(BitcoinNetwork.MAINNET, sptListInfo);
    }

    public Address getIssuer() {
        return issuer;
    }

    public Sha256Hash getCreationtxid() {
        return creationtxid;
    }

    public boolean isFixedissuance() {
        return fixedissuance;
    }

    public boolean isManagedissuance() {
        return managedissuance;
    }

    public boolean isFreezingenabled() {
        return freezingenabled;
    }

    public OmniValue getTotaltokens() {
        return totaltokens;
    }

    /**
     * Return a <i>mock</i> {@code OmniPropertyInfo} for Bitcoin. Many of the fields are "n/a" and
     * we give the final, total number of tokens, not the current, dynamic value. This is useful
     * in cases where we need Bitcoin in the list of tokens, but don't have access to current
     * Bitcoin data. OmniProxy is able to provide accurate, dynamic information so does not
     * use this mock value.
     *
     * @return static (mock) info for Bitcoin
     */
    public static OmniPropertyInfo mockBitcoinPropertyInfo(BitcoinNetwork network) {
        return bitcoinPropertyInfo(network, Coin.COIN.multiply(21_000_000));
    }

    /**
     * Return {@code OmniPropertyInfo} for Bitcoin. Many of the fields are "n/a" but
     * we give current, dynamic count of bitcoins. This method is used
     * by <b>OmniProxy</b> with the current count of coins from {@link org.consensusj.bitcoin.json.pojo.TxOutSetInfo}.
     *
     * @return info for Bitcoin with current number of coins
     */
    public static OmniPropertyInfo bitcoinPropertyInfo(BitcoinNetwork network, Coin bitcoinSupply) {
        return new OmniPropertyInfo(CurrencyID.BTC,
                "Bitcoin",
                "n/a",
                "n/a",
                "n/a",
                "https://bitcoin.org",
                true,
                defaultIssuer(network),
                genesisCoinbaseTxId,
                false,
                true,
                false,
                OmniDivisibleValue.ofWilletts(bitcoinSupply.getValue()));
    }

    private static Address defaultIssuer(BitcoinNetwork network) {
        return network == BitcoinNetwork.MAINNET ? bitcoinMainNetIssuer : bitcoinTestNetIssuer;
    }
}
