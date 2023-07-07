package foundation.omni.json.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import foundation.omni.CurrencyID;
import foundation.omni.OmniValue;
import org.bitcoinj.base.Address;
import org.bitcoinj.base.Coin;
import org.bitcoinj.base.Sha256Hash;

import java.time.Instant;
import java.util.List;

/**
 * OmniTradeInfo - response object for {@code omni_gettrade} and {@code omni_gettradehistoryforaddress}
 */
public class OmniTradeInfo {
    private final Sha256Hash txId;
    private final Address sendingAddress;
    private final boolean isMine;
    private final int confirmations;
    private final Coin fee;
    private final Instant blockTime;
    private final boolean valid;
    private final int positionInBlock;
    private final int version;
    private final int type_int;
    private final String type;
    private final CurrencyID propertyIdForSale;
    private final boolean propertyIdForSaleIsDivisible;
    private final CurrencyID propertyIdDesired;
    private final OmniValue amountForSale;
    private final boolean propertyIdDesiredIsDivisble;
    private final OmniValue amountDesired;
    private final String unitPrice;
    private final OmniValue amountRemaining;
    private final OmniValue amountToFill;
    private final String status;
    private final Sha256Hash cancelTxId;
    private final List<Match> matches;

    @JsonCreator
    public OmniTradeInfo(@JsonProperty("txid")                  Sha256Hash txId,
                         @JsonProperty("sendingaddress")        Address sendingAddress,
                         @JsonProperty("ismine")                boolean isMine,
                         @JsonProperty("confirmations")         int confirmations,
                         @JsonProperty("fee")                   Coin fee,
                         @JsonProperty("blocktime")             long blockTime,
                         @JsonProperty("valid")                 boolean valid,
                         @JsonProperty("positioninblock")       int positionInBlock,
                         @JsonProperty("version")               int version,
                         @JsonProperty("type_int")              int type_int,
                         @JsonProperty("type")                  String type,
                         @JsonProperty("propertyidforsale")     CurrencyID propertyIdForSale,
                         @JsonProperty("propertyidforsaleisdivisible")  boolean propertyIdForSaleIsDivisible,
                         @JsonProperty("amountforsale")         OmniValue amountForSale,
                         @JsonProperty("propertyiddesired")     CurrencyID propertyIdDesired,
                         @JsonProperty("propertyiddesiredisdivisible")  boolean propertyIdDesiredIsDivisble,
                         @JsonProperty("amountdesired")         OmniValue amountDesired,
                         @JsonProperty("unitprice")             String unitPrice,
                         @JsonProperty("amountremaining")       OmniValue amountRemaining,
                         @JsonProperty("amounttofill")          OmniValue amountToFill,
                         @JsonProperty("status")                String status,
                         @JsonProperty("cancelTxId")            Sha256Hash cancelTxId,
                         @JsonProperty("matches")               List<Match> matches) {
        this.txId = txId;
        this.sendingAddress = sendingAddress;
        this.isMine = isMine;
        this.confirmations = confirmations;
        this.fee = fee;
        this.blockTime = Instant.ofEpochSecond(blockTime);
        this.valid = valid;
        this.positionInBlock = positionInBlock;
        this.version = version;
        this.type_int = type_int;
        this.type = type;
        this.propertyIdForSale = propertyIdForSale;
        this.propertyIdForSaleIsDivisible = propertyIdForSaleIsDivisible;
        this.propertyIdDesired = propertyIdDesired;
        this.amountForSale = amountForSale;
        this.propertyIdDesiredIsDivisble = propertyIdDesiredIsDivisble;
        this.amountDesired = amountDesired;
        this.unitPrice = unitPrice;
        this.amountRemaining = amountRemaining;
        this.amountToFill = amountToFill;
        this.status = status;
        this.cancelTxId = cancelTxId;
        this.matches = matches;
    }

    public Sha256Hash getTxId() {
        return txId;
    }

    public Address getSendingAddress() {
        return sendingAddress;
    }

    public boolean isMine() {
        return isMine;
    }

    public int getConfirmations() {
        return confirmations;
    }

    public Coin getFee() {
        return fee;
    }

    public Instant getBlockTime() {
        return blockTime;
    }

    public boolean isValid() {
        return valid;
    }

    public int getPositionInBlock() {
        return positionInBlock;
    }

    public int getVersion() {
        return version;
    }

    public int getType_int() {
        return type_int;
    }

    public String getType() {
        return type;
    }

    public CurrencyID getPropertyIdForSale() {
        return propertyIdForSale;
    }

    public boolean isPropertyIdForSaleIsDivisible() {
        return propertyIdForSaleIsDivisible;
    }

    public CurrencyID getPropertyIdDesired() {
        return propertyIdDesired;
    }

    public OmniValue getAmountForSale() {
        return amountForSale;
    }

    public boolean isPropertyIdDesiredIsDivisble() {
        return propertyIdDesiredIsDivisble;
    }

    public OmniValue getAmountDesired() {
        return amountDesired;
    }

    public String getUnitPrice() {
        return unitPrice;
    }

    public OmniValue getAmountRemaining() {
        return amountRemaining;
    }

    public OmniValue getAmountToFill() {
        return amountToFill;
    }

    public String getStatus() {
        return status;
    }

    public Sha256Hash getCancelTxId() {
        return cancelTxId;
    }

    public List<Match> getMatches() {
        return matches;
    }

    public static class Match {
        private final Sha256Hash txId;
        private final int block;
        private final Instant blockTime;
        private final Address address;
        private final OmniValue amountSold;
        private final OmniValue amountReceived;

        @JsonCreator
        public Match(@JsonProperty("txid")              Sha256Hash txId,
                     @JsonProperty("block")             int block,
                     @JsonProperty("blocktime")         Long blockTime,
                     @JsonProperty("address")           Address address,
                     @JsonProperty("amountsold")        OmniValue amountSold,
                     @JsonProperty("amountreceived")    OmniValue amountReceived) {
            this.txId = txId;
            this.block = block;
            this.blockTime = (blockTime != null) ? Instant.ofEpochSecond(blockTime) : null;
            this.address = address;
            this.amountSold = amountSold;
            this.amountReceived = amountReceived;
        }

        public Sha256Hash getTxId() {
            return txId;
        }

        public int getBlock() {
            return block;
        }

        // Nullable, added in Omni Core 0.12.0
        public Instant getBlockTime() {
            return blockTime;
        }

        public Address getAddress() {
            return address;
        }

        public OmniValue getAmountSold() {
            return amountSold;
        }

        public OmniValue getAmountReceived() {
            return amountReceived;
        }
    }
}
