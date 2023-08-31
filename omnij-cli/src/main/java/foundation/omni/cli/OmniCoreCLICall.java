package foundation.omni.cli;

import org.consensusj.bitcoin.jsonrpc.RpcConfig;
import foundation.omni.rpc.OmniClient;
import org.consensusj.bitcoin.cli.BitcoinCLITool;

import javax.net.ssl.SSLContext;
import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Subclass BitcoinCLICall to create/return/use the OmniClient subclass
 */
public class OmniCoreCLICall extends BitcoinCLITool.BitcoinCLICall {
    public OmniCoreCLICall(BitcoinCLITool tool, PrintWriter out, PrintWriter err, String[] args) {
        super(tool, out, err, args);
    }

    public OmniCoreCLICall(BitcoinCLITool tool, PrintStream out, PrintStream err, String[] args) {
        super(tool, new PrintWriter(out, true), new PrintWriter(err,true), args);
    }

    @Override
    public OmniClient createClient(SSLContext sslContext, RpcConfig config) {
        return new OmniClient(sslContext, config.network(), config.getURI(), config.getUsername(), config.getPassword(), false, false);
    }
}
