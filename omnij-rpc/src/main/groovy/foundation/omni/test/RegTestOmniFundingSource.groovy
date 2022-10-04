package foundation.omni.test

import foundation.omni.rpc.test.OmniTestClient;
import foundation.omni.rpc.test.OmniTestClientAccessor
import org.consensusj.bitcoin.jsonrpc.test.FundingSource;
import org.consensusj.bitcoin.jsonrpc.test.RegTestFundingSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This should be converted to Java.
 * So let's keep it in Java-like Groovy until then
 */
public class RegTestOmniFundingSource extends RegTestFundingSource implements OmniTestClientAccessor, OmniTestSupport, OmniFundingSource  {
    private static final Logger log = LoggerFactory.getLogger(RegTestFundingSource.class);
    RegTestFundingSource fundingSource;

    public RegTestOmniFundingSource(OmniTestClient client, RegTestFundingSource fundingSource) {
        super(client);          // Set BitcoinExtendedClient in superclass
        this.fundingSource = fundingSource;
    }

    OmniTestClient client() {
        return client;
    }

    @Override
    FundingSource fundingSource() {
        return fundingSource
    }
}
