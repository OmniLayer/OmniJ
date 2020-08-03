package foundation.omni.consensus;

import foundation.omni.rest.omniwallet.mjdk.OmniwalletModernJDKClient;
import org.bitcoinj.params.MainNetParams;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutionException;

/**
 * Command-line tool and class for fetching OmniWallet consensus data
 */
public class OmniwalletConsensusTool extends OmniwalletModernJDKClient implements ConsensusTool {
    public OmniwalletConsensusTool() {
        super(omniwalletBase, true, true, MainNetParams.get());
    }

    public OmniwalletConsensusTool(URI hostURI) {
        super(hostURI, true, true, MainNetParams.get());
    }

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        OmniwalletConsensusTool tool = new OmniwalletConsensusTool();
        tool.run(DefaultGroovyMethods.toList(args));
    }

}
