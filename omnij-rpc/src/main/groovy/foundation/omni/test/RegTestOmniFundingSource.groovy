package foundation.omni.test

import foundation.omni.rpc.test.OmniTestClient;
import org.consensusj.bitcoin.jsonrpc.test.RegTestFundingSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This should be converted to Java.
 * So let's keep it in Java-like Groovy until then
 */
public class RegTestOmniFundingSource extends RegTestFundingSource {
    private static final Logger log = LoggerFactory.getLogger(RegTestOmniFundingSource.class);

    public RegTestOmniFundingSource(OmniTestClient client) {
        super(client);
    }
}
