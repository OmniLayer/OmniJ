package foundation.omni.cli;

import foundation.omni.CurrencyID;
import foundation.omni.consensus.ConsensusToolOutput;
import foundation.omni.consensus.MultiPropertyComparison;
import foundation.omni.consensus.OmniCoreConsensusTool;
import foundation.omni.consensus.OmniwalletConsensusTool;
import foundation.omni.netapi.omnicore.RxOmniClient;
import foundation.omni.rpc.ConsensusFetcher;
import foundation.omni.rpc.ConsensusSnapshot;
import foundation.omni.rpc.OmniClient;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.consensusj.bitcoin.cli.BitcoinCLITool;
import org.consensusj.jsonrpc.cli.JavaLoggingSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.concurrent.ExecutionException;

/**
 * Tool to fetch Omni and optionally compare consensus data from Omni Core and/or Omniwallet.
 */
public class ConsensusCLI extends BitcoinCLITool {
    public static final String commandName = "omnij-consensus-tool";
    // omnij-consensus-tool [_OMNICORE_SETTING_]... _URL_OPTION_ -compare [-property id]
    public static final String commandUsage = commandName + " [omnicore-options...] [url-option] [options] [-? | -property <id> | -compare]";
    // For a GraalVM command-line tool we muse configure Java Logging in main
    // before initializing this Logger object
    private static Logger log;

    private final Options options = new OmniConsensusToolOptions();

    private boolean verboseOutput;

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
        JavaLoggingSupport.configure("foundation.omni.cli");
        log = LoggerFactory.getLogger(ConsensusCLI.class);
    }

    public static void main(String[] args) {
        try {
            ConsensusCLI command = new ConsensusCLI();
            OmniCoreCLICall call = new OmniCoreCLICall(command, System.out, System.err, args);
            command.run(call);
        } catch (ToolException te) {
            System.exit(te.resultCode);
        } catch (Throwable e) {
            e.printStackTrace();
            System.exit(1);
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
        } catch (IOException | InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void run(OmniCoreCLICall call) throws IOException, InterruptedException, ExecutionException {
        CommandLine line = call.line;
        verboseOutput = line.hasOption("verbose");
        // Must have -p (property id) or -x (compare)
        if (!(line.hasOption("p") || line.hasOption("x"))) {
            printError(call, "Must either specify a property id with -p and/or the -x/-compare option");
            printHelp(call, commandUsage);
            throw new ToolException(1, "usage error");
        }

        String property = line.getOptionValue("property");
        long currencyIDNum = property != null ? Long.parseLong(property, 10) : 1;
        CurrencyID currencyID = new CurrencyID(currencyIDNum);

        String fileName = line.getOptionValue("output");

        ConsensusFetcher tool1;
        ConsensusFetcher tool2 = null;
        URI uri1;
        URI uri2 = null;
        uri1 = call.rpcClient().getServerURI();
        tool1 = new OmniCoreConsensusTool((RxOmniClient) call.rpcClient());
        if (line.hasOption("omnicore-url")) {
            uri2 = URI.create(line.getOptionValue("omnicore-url"));
            tool2 = new OmniCoreConsensusTool(call.getRPCConfig().getNetParams(), uri2);
        } else if (line.hasOption("omniwallet-url")) {
            uri2 = URI.create(line.getOptionValue("omniwallet-url"));
            tool2 = new OmniwalletConsensusTool(uri2);
        }

        if (line.hasOption("compare") && (tool2 == null) ) {
            printError(call, "-omnicore-url or -omniwallet-url must be specified for -x/-compare option");
            printHelp(call, commandUsage);
            throw new ToolException(1, "usage error");
        } else if (line.hasOption("compare")) {
            MultiPropertyComparison multiComparison = new MultiPropertyComparison(tool1, tool2);
            if (line.hasOption("p")) {
                log.info("Comparing {} and {}, Property: {}", uri1, uri2,currencyID);
                long mismatchCount = multiComparison.compareProperty(currencyID);
                if (mismatchCount > 0) {
                    printError(call, "Failure: " + mismatchCount + " mismatched address");
                } else {
                    log.info("Servers {} and {} are IN CONSENSUS  for property: {}", uri1, uri2, currencyID);
                }
            } else {
                log.info("Comparing {} and {}, ALL PROPERTIES", uri1, uri2);
                long mismatchCount = multiComparison.compareAllProperties();
                if (mismatchCount > 0) {
                    printError(call, "Failure: " + mismatchCount + " mismatched properties");
                } else {
                    log.info("Servers {} and {} are IN CONSENSUS for all properties", uri1, uri2);
                }
            }
        } else {
            ConsensusSnapshot consensus = (tool2 == null) ? tool1.getConsensusSnapshot(currencyID) :
                                                            tool2.getConsensusSnapshot(currencyID);

            if (fileName != null) {
                ConsensusToolOutput.save(consensus, new File(fileName));
            } else {
                ConsensusToolOutput.print(consensus, call.out);
            }
        }
    }
}
