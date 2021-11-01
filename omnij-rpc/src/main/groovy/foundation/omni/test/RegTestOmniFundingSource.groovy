package foundation.omni.test;

import org.consensusj.bitcoin.test.RegTestFundingSource;
import foundation.omni.rpc.test.OmniTestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This should be converted to Java.
 * So let's keep it in Java-like Groovy until then
 */
public class RegTestOmniFundingSource extends RegTestFundingSource implements OmniTestSupport, OmniFundingSource  {
    private static final Logger log = LoggerFactory.getLogger(RegTestFundingSource.class);

    public RegTestOmniFundingSource(OmniTestClient client) {
        super(client);          // Set BitcoinExtendedClient in superclass
        foundation_omni_test_OmniTestClientDelegate__client = client;   // Set Omni client here
    }
}
