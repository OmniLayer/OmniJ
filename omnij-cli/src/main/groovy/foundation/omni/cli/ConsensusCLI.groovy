package foundation.omni.cli

import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Options
import org.consensusj.bitcoin.cli.BitcoinCLITool
import foundation.omni.CurrencyID
import foundation.omni.consensus.ExplorerConsensusTool
import foundation.omni.rpc.ConsensusSnapshot
import foundation.omni.consensus.ConsensusTool
import foundation.omni.consensus.MultiPropertyComparison
import foundation.omni.consensus.OmniCoreConsensusTool
import foundation.omni.consensus.OmniwalletConsensusTool
import foundation.omni.rpc.BalanceEntry
import foundation.omni.rpc.OmniClient
import org.bitcoinj.core.Address

/**
 * Tool to fetch Omni consensus from Omni Core or one of several other Omni APIs.
 * This is in Groovy because it calls some tools that ARE/WERE written in Groovy (e.g. OmniwalletConsensusTool)
 * TODO: Convert to Java now that OmniwalletConsensusTool is in Java
 */
//@CompileStatic
public class ConsensusCLI extends BitcoinCLITool {
    public final static String commandName = "omni-consensus"
    public final static String commandUsage = "${commandName} [options] -property <id>"
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
        super()
    }

    public static void main(String[] args) {
        ConsensusCLI command = new ConsensusCLI()
        def status = command.run(System.out, System.err, args)
        System.exit(status)
    }

    @Override
    public OmniCoreCLICall createCall(PrintWriter out, PrintWriter err, String... args) {
        return new OmniCoreCLICall(this, out, err, args);
    }

    @Override
    public void run(Call call) {
        run((OmniCoreCLICall) call);
    }


    public void run(OmniCoreCLICall call) {
        CommandLine line = call.line;
        // Must have -p (property id) or -x (compare), but not both
        // TODO: This will change when we allow download of all property ids or comparison of a single one
        if (!(line.hasOption('p') ^ line.hasOption('x'))) {
            printError(call,"Must either specify a property id with -p or the -x/-compare option, but not both")
            printHelp(call, commandUsage)
            throw new ToolException(1, "usage error");
        }
        String property = line.getOptionValue("property")
        Long currencyIDNum =  property ? Long.parseLong(property, 10) : 1
        CurrencyID currencyID = new CurrencyID(currencyIDNum)

        String fileName = line.getOptionValue("output")

        ConsensusTool tool1, tool2
        if (line.hasOption("omnicore-url")) {
            tool1 = new OmniCoreConsensusTool(getRPCConfig().netParams, line.getOptionValue("omnicore-url").toURI())
        } else if (line.hasOption("omniwallet-url")) {
            tool1 = new OmniwalletConsensusTool(line.getOptionValue("omniwallet-url").toURI())
        } else if (line.hasOption("omnichest-url")) {
            tool1 = new ExplorerConsensusTool(line.getOptionValue("omnichest-url").toURI())
        } else {
            tool1 = new OmniCoreConsensusTool((OmniClient)call.rpcClient())
        }

        if (line.hasOption("compare")) {
            // TODO: Make sure that if compare option is used one of the above xxx-url options is also chosen
            tool2 = new OmniCoreConsensusTool((OmniClient) call.rpcClient())
            //pwerr.println "Comparing ${tool2.serverURI} with ${tool1.serverURI}"
            MultiPropertyComparison multiComparison = new MultiPropertyComparison(tool2, tool1);
            multiComparison.compareAllProperties()
            //multiComparison.compareProperty(CurrencyID.OMNI)
        } else {
            def consensus = tool1.getConsensusSnapshot(currencyID)

            if (fileName != null) {
                File output = new File(fileName)
                save(consensus, output)
            } else {
                print(consensus, call.out)
            }
        }
    }
    
    static void save(ConsensusSnapshot snap, File file) {
        PrintWriter pw = file.newPrintWriter()
        output(snap, pw, true)
        pw.flush()
    }

    void print(ConsensusSnapshot snap, PrintWriter pwout) {
        output(snap, pwout, true)
    }

    static void output(ConsensusSnapshot snap, PrintWriter writer, boolean tsv) {
        snap.entries.each { Address address, BalanceEntry entry ->
            String balance = entry.balance.toString()
            String reserved = entry.reserved.toString()
            if (tsv) {
                writer.println("${address}\t${balance}\t${reserved}")
            } else {
                writer.println("${address}: ${balance}, ${reserved}")
            }
        }
    }
}
