package foundation.omni.test

import foundation.omni.netapi.omnicore.RxOmniTestClient;
import org.consensusj.bitcoin.jsonrpc.test.RegTestFundingSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This should be converted to Java.
 * So let's keep it in Java-like Groovy until then
 */
public class RegTestOmniFundingSource extends RegTestFundingSource implements OmniTestSupport, OmniFundingSource  {
    private static final Logger log = LoggerFactory.getLogger(RegTestFundingSource.class);

    public RegTestOmniFundingSource(RxOmniTestClient client) {
        super(client);          // Set BitcoinExtendedClient in superclass
        foundation_omni_test_OmniTestClientDelegate__client = client;   // Set Omni client here
    }
}
