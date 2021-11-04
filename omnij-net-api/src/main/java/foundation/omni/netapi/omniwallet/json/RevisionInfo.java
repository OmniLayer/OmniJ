package foundation.omni.netapi.omniwallet.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nonnull;

/**
 * Jackson POJO wrapper for raw Omniwallet response
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RevisionInfo {
    private final String blockTime;
    private final @Nonnull Integer lastBlock;
    private final String lastParsed;

    public RevisionInfo(@JsonProperty("block_time") String blockTime,
                        @Nonnull @JsonProperty("last_block") Integer lastBlock,
                        @JsonProperty("last_parsed") String lastParsed) {
        // Null check here?
        this.blockTime = blockTime;
        this.lastBlock = lastBlock;
        this.lastParsed = lastParsed;
    }

    public String getBlockTime() {
        return blockTime;
    }

    public @Nonnull Integer getLastBlock() {
        return lastBlock;
    }

    public String getLastParsed() {
        return lastParsed;
    }
}
