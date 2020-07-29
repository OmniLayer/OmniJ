package foundation.omni.consensus;

import foundation.omni.rest.omniwallet.OmniwalletClient;
import org.bitcoinj.params.MainNetParams;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import java.io.IOException;
import java.net.URI;

/**
 * Command-line tool and class for fetching OmniWallet consensus data
 * TODO: Migrate to OmniwalletModernJDKClient ?
 */
public class OmniwalletConsensusTool extends OmniwalletClient implements ConsensusTool {
    public OmniwalletConsensusTool() {
        super(omniwalletBase, true, true, MainNetParams.get());
    }

    public OmniwalletConsensusTool(URI hostURI) {
        super(hostURI, true, true, MainNetParams.get());
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        OmniwalletConsensusTool tool = new OmniwalletConsensusTool();
        tool.run(DefaultGroovyMethods.toList(args));
    }

}
