package foundation.omni.json.pojo;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import foundation.omni.CurrencyID;
import foundation.omni.OmniValue;
import foundation.omni.tx.Transactions;
import org.bitcoinj.base.Address;
import org.bitcoinj.base.Coin;
import org.bitcoinj.base.Sha256Hash;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * (Mostly) Immutable representation of OmniTransaction info JSON
 * <p>
 * It is not fully-immutable because of {@link #addOtherInfo(String, Object)} which should only be used by Jackson
 * when deserializing. In the future we might use subclasses and polymorphism to define the properties for each type
 * of Omni transaction.
 * <p>
 * This is returned by {@link foundation.omni.rpc.OmniClient#omniGetTransaction(Sha256Hash)},
 * {@link foundation.omni.rpc.OmniClient#omniListTransactions()}, and {@link foundation.omni.rpc.OmniClient#omniListTransactions()}.
 */
public class OmniTransactionInfo {
    private final Sha256Hash txId;
    private final Address sendingAddress;
    private final Address referenceAddress;
    private final boolean isMine;
    private final int confirmations;
    private final Coin fee;
    private final Instant blockTime;
    private final boolean valid;
    private final int positionInBlock;
    private final int version;
    private final int typeInt;
    private final String type;
    private final OmniValue amount;
    private final OmniValue totalAmount;
    private final boolean divisible;
    private final CurrencyID propertyId;
    private final Sha256Hash blockHash;
    private final int block;
    private final CurrencyID propertyIdForSale;
    private final CurrencyID propertyIdDesired;
    private final Map<String, Object> otherInfo;

    /* Add a map of additional properties or polymorphism, for now we're using @JsonIgnoreProperties */

    public OmniTransactionInfo(@JsonProperty("txid")                Sha256Hash  txId,
                               @JsonProperty("sendingaddress")      Address     sendingAddress,
                               @JsonProperty("referenceaddress")    Address     referenceAddress,
                               @JsonProperty("ismine")              boolean     isMine,
                               @JsonProperty("confirmations")       int         confirmations,
                               @JsonProperty("fee")                 String      fee,
                               @JsonProperty("blocktime")           long        blockTime,
                               @JsonProperty("valid")               boolean     valid,
                               @JsonProperty("positioninblock")     int         positionInBlock,
                               @JsonProperty("version")             int         version,
                               @JsonProperty("type_int")            int         typeInt,
                               @JsonProperty("type")                String      type,
                               @JsonProperty("amount")              OmniValue   amount,
                               @JsonProperty("totalamount")         OmniValue   totalAmount,
                               @JsonProperty("divisible")           boolean     divisible,
                               @JsonProperty("propertyid")          CurrencyID  propertyId,
                               @JsonProperty("blockhash")           Sha256Hash  blockHash,
                               @JsonProperty("block")               int         block,
                               @JsonProperty("propertyidforsale")   CurrencyID  propertyIdForSale,
                               @JsonProperty("propertyiddesired")   CurrencyID  propertyIdDesired)
    {
        this.txId = txId;
        this.sendingAddress = sendingAddress;
        this.referenceAddress = referenceAddress;
        this.isMine = isMine;
        this.confirmations = confirmations;
        this.fee = Coin.parseCoin(fee);
        this.blockTime = Instant.ofEpochSecond(blockTime);
        this.valid = valid;
        this.positionInBlock = positionInBlock;
        this.version = version;
        this.typeInt = typeInt;
        this.type = type;
        this.amount = amount;
        this.totalAmount = totalAmount;
        this.divisible = divisible;
        this.propertyId = propertyId;
        this.blockHash = blockHash;
        this.block = block;
        this.propertyIdForSale = propertyIdForSale;
        this.propertyIdDesired = propertyIdDesired;
        this.otherInfo = new HashMap<>();
    }

    @JsonAnySetter
    public void addOtherInfo(String propertyKey, Object value) {
        this.otherInfo.put(propertyKey, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getOtherInfo() {
        return otherInfo;
    }

    public Sha256Hash getTxId() {
        return txId;
    }

    public Address getSendingAddress() {
        return sendingAddress;
    }

    public Address getReferenceAddress() {
        return referenceAddress;
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

    public int getTypeInt() {
        return typeInt;
    }

    public String getType() {
        return type;
    }

    /**
     * Get the transaction type as an {@code Optional} {@link Transactions.TransactionType} or
     * as an {@link Optional#empty()} if it's a transaction type not (yet) included in
     * {@link Transactions.TransactionType}.
     *
     * @return The type or {@link Optional#empty()} if it's an unknown type
     */
    @JsonIgnore
    public Optional<Transactions.TransactionType> transactionType() {
        return Transactions.TransactionType.find(typeInt);
    }

    public OmniValue getAmount() {
        return amount;
    }

    public OmniValue getTotalAmount() {
        return totalAmount;
    }

    public boolean isDivisible() {
        return divisible;
    }

    public CurrencyID getPropertyId() {
        return propertyId;
    }

    public Sha256Hash getBlockHash() {
        return blockHash;
    }

    public int getBlock() {
        return block;
    }

    // Nullable, only non-null for supported Tx types
    public CurrencyID getPropertyIdForSale() {
        return propertyIdForSale;
    }

    // Nullable, only non-null for supported Tx types
    public CurrencyID getPropertyIdDesired() {
        return propertyIdDesired;
    }
}
