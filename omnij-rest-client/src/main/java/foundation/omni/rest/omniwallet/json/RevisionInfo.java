package foundation.omni.rest.omniwallet.json;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Jackson POJO wrapper for raw Omniwallet response
 */
public class RevisionInfo {
    private final String lastBlock;
    private final String lastParsed;

    public RevisionInfo(@JsonProperty("last_block") String lastBlock,
                        @JsonProperty("last_parsed") String lastParsed) {
        this.lastBlock = lastBlock;
        this.lastParsed = lastParsed;
    }

    public String getLastBlock() {
        return lastBlock;
    }

    public String getLastParsed() {
        return lastParsed;
    }
}
