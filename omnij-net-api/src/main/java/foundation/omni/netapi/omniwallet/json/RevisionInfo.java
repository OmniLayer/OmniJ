package foundation.omni.netapi.omniwallet.json;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nonnull;

/**
 * Jackson POJO wrapper for raw Omniwallet response
 */
public class RevisionInfo {
    private final @Nonnull Integer lastBlock;
    private final String lastParsed;

    public RevisionInfo(@Nonnull @JsonProperty("last_block") Integer lastBlock,
                        @JsonProperty("last_parsed") String lastParsed) {
        // Null check here?
        this.lastBlock = lastBlock;
        this.lastParsed = lastParsed;
    }

    public @Nonnull Integer getLastBlock() {
        return lastBlock;
    }

    public String getLastParsed() {
        return lastParsed;
    }
}
