package foundation.omni.consensus;

import org.consensusj.bitcoin.rpc.RpcURI;
import foundation.omni.netapi.omnicore.OmniCoreClient;
import foundation.omni.rpc.OmniClient;
import foundation.omni.rpc.test.TestServers;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutionException;

/**
 * Command-line tool and class for fetching Omni Core consensus data
 */
public class OmniCoreConsensusTool extends OmniCoreClient implements ConsensusTool {
    /**
     * URI Constructor
     *
     * @param netParams *bitcoinj* NetworkParameters for server to connect to
     * @param coreURI URI to connect to - user/pass if required, must be encoded in URL
     */
    public OmniCoreConsensusTool(NetworkParameters netParams, URI coreURI) {
        super(netParams, coreURI, coreURI.getUserInfo().split(":")[0], coreURI.getUserInfo().split(":")[1]);
    }

    /**
     * Constructor that takes an existing OmniClient
     *
     * @param client An existing client instance
     */
    public OmniCoreConsensusTool(OmniClient client) {
        super(client);
    }

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        OmniClient client = new OmniClient(MainNetParams.get(), RpcURI.getDefaultMainNetURI(), TestServers.getInstance().getRpcTestUser(), TestServers.getInstance().getRpcTestPassword());
        OmniCoreConsensusTool tool = new OmniCoreConsensusTool(client);
        tool.run(DefaultGroovyMethods.toList(args));
    }

    protected OmniClient client;
}
