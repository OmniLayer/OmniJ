package foundation.omni.cli;


import com.msgilligan.bitcoin.cli.CliCommand;
import com.msgilligan.bitcoin.cli.CliOptions;
import com.msgilligan.bitcoin.rpc.JsonRPCException;

import java.io.IOException;
import java.util.Map;

/**
 *  Unfinished command-line Omni Core consensus tool.
 *  <p>
 *      TODO: Merge code from OmniCoreConsensusTool
 *  </p>
 */
public class Consensus extends CliCommand {
    public final static String commandName = "omni-consensus";

    public Consensus(String[] args) {
        super(commandName, new CliOptions(), args);
    }

    public static void main(String[] args) {
        Consensus command = new Consensus(args);
        command.run();
    }

    public Integer runImpl() throws IOException, JsonRPCException {
        return 0;
    }

}
