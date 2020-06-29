package foundation.omni.cli;

import com.msgilligan.bitcoinj.rpc.RpcConfig;
import foundation.omni.rpc.OmniClient;
import org.consensusj.bitcoin.cli.BitcoinCLITool;

import java.io.PrintWriter;

/**
 * Subclass BitcoinCLICall to create/return/use the OmniClient subclass
 */
public class OmniCoreCLICall extends BitcoinCLITool.BitcoinCLICall {
    public OmniCoreCLICall(BitcoinCLITool tool, PrintWriter out, PrintWriter err, String[] args) {
        super(tool, out, err, args);
    }

    @Override
    public OmniClient createClient(RpcConfig config) {
        return new OmniClient(config.getNetParams(), config.getURI(), config.getUsername(), config.getPassword());
    }
}
