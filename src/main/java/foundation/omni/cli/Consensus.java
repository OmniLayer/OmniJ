package foundation.omni.cli;


import com.msgilligan.bitcoin.cli.CliCommand;
import com.msgilligan.bitcoin.cli.CliOptions;
import com.msgilligan.bitcoin.rpc.JsonRPCException;

import java.io.IOException;
import java.util.Map;

/**
 *  Unfinished command-line Master Core consensus tool.
 *  <p>
 *      TODO: Merge code from MasterCoreConsensusTool
 *  </p>
 */
public class Consensus extends CliCommand {
    public final static String commandName = "msc-consensus";

    public Consensus(String[] args) {
        super(commandName, new CliOptions(), args);
    }

    public static void main(String[] args) throws JsonRPCException, IOException {
        Consensus command = new Consensus(args);
        command.run();
    }

    public void run() throws JsonRPCException, IOException {
        preflight();
        Map<String, Object> info = client.getInfo();

        Integer bitcoinVersion = (Integer) info.get("version");
        Integer masterCoreVersion = (Integer) info.get("mastercoreversion");
        Integer blocks = (Integer) info.get("blocks");

        System.out.println("Bitcoin Core Version: " + bitcoinVersion);
        if (masterCoreVersion != null) {
            System.out.println("Master Core version: " + masterCoreVersion);
        }
        System.out.println("Block count: " + blocks);
        System.exit(0);
    }

}
