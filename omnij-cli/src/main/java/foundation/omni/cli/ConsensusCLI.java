package foundation.omni.cli;

import foundation.omni.CurrencyID;
import foundation.omni.consensus.MultiPropertyComparison;
import foundation.omni.consensus.OmniCoreConsensusTool;
import foundation.omni.consensus.OmniwalletConsensusTool;
import foundation.omni.rpc.ConsensusFetcher;
import foundation.omni.rpc.ConsensusSnapshot;
import foundation.omni.rpc.OmniClient;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.consensusj.bitcoin.cli.BitcoinCLITool;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;

/**
 * Tool to fetch Omni consensus from Omni Core or one of several other Omni APIs.
 * This is in Groovy because it calls some tools that ARE/WERE written in Groovy (e.g. OmniwalletConsensusTool)
 * TODO: Convert to Java now that OmniwalletConsensusTool is in Java
 */
public class ConsensusCLI extends BitcoinCLITool {
    public static final String commandName = "omni-consensus";
    public static final String commandUsage = commandName + " [options] -property <id>";
    private final Options options = new OmniConsensusToolOptions();

    @Override
    public String name() {
        return commandName;
    }

    @Override
    public String usage() {
        return commandUsage;
    }

    @Override
    public Options options() {
        return options;
    }

    public ConsensusCLI() {
        super();
    }

    public static void main(String[] args) {
        ConsensusCLI command = new ConsensusCLI();
        OmniCoreCLICall call = new OmniCoreCLICall(command, System.out, System.err, args);
        /* int status */
        try {
            command.run(call);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    @Override
    public OmniCoreCLICall createCall(PrintWriter out, PrintWriter err, String... args) {
        return new OmniCoreCLICall(this, out, err, args);
    }

    @Override
    public void run(Call call) {
        try {
            run((OmniCoreCLICall) call);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void run(OmniCoreCLICall call) throws IOException, InterruptedException {
        CommandLine line = call.line;
        // Must have -p (property id) or -x (compare), but not both
        // TODO: This will change when we allow download of all property ids or comparison of a single one
        if (!(line.hasOption("p") ^ line.hasOption("x"))) {
            printError(call, "Must either specify a property id with -p or the -x/-compare option, but not both");
            printHelp(call, commandUsage);
            throw new ToolException(1, "usage error");
        }

        String property = line.getOptionValue("property");
        long currencyIDNum = property != null ? Long.parseLong(property, 10) : 1;
        CurrencyID currencyID = new CurrencyID(currencyIDNum);

        String fileName = line.getOptionValue("output");

        ConsensusFetcher tool1;
        ConsensusFetcher tool2;
        if (line.hasOption("omnicore-url")) {
            tool1 = new OmniCoreConsensusTool(call.getRPCConfig().getNetParams(), URI.create(line.getOptionValue("omnicore-url")));
        } else if (line.hasOption("omniwallet-url")) {
            tool1 = new OmniwalletConsensusTool(URI.create(line.getOptionValue("omniwallet-url")));
        } else {
            tool1 = new OmniCoreConsensusTool((OmniClient) call.rpcClient());
        }


        if (line.hasOption("compare")) {
            // TODO: Make sure that if compare option is used one of the above xxx-url options is also chosen
            tool2 = new OmniCoreConsensusTool((OmniClient) call.rpcClient());
            //pwerr.println "Comparing ${tool2.serverURI} with ${tool1.serverURI}"
            MultiPropertyComparison multiComparison = new MultiPropertyComparison(tool2, tool1);
            multiComparison.compareAllProperties();
            //multiComparison.compareProperty(CurrencyID.OMNI)
        } else {
            ConsensusSnapshot consensus = tool1.getConsensusSnapshot(currencyID);

            if (fileName != null) {
                File output = new File(fileName);
                save(consensus, output);
            } else {
                print(consensus, call.out);
            }

        }

    }

    public static void save(ConsensusSnapshot snap, File file) throws IOException {
        PrintWriter pw = new PrintWriter(file);
        output(snap, pw, true);
        pw.flush();
    }

    public void print(ConsensusSnapshot snap, PrintWriter pwout) {
        output(snap, pwout, false);
    }

    public static void output(ConsensusSnapshot snap, final PrintWriter writer, final boolean tsv) {
        snap.getEntries().forEach((address, balanceEntry) -> {
            final String balance = balanceEntry.getBalance().toString();
            final String reserved = balanceEntry.getReserved().toString();
            if (tsv) {
                writer.println(address + "\t" + balance + "\t" + reserved);
            } else {
                writer.println(address + ": " + balance + ", " + reserved);
            }
        });
    }
}
