package foundation.omni.cli

import org.consensusj.bitcoin.cli.BitcoinCliCommand
import org.consensusj.bitcoin.cli.CliOptions
import org.consensusj.jsonrpc.JsonRpcException
import foundation.omni.CurrencyID
import foundation.omni.consensus.ExplorerConsensusTool
import foundation.omni.rpc.ConsensusSnapshot
import foundation.omni.consensus.ConsensusTool
import foundation.omni.consensus.MultiPropertyComparison
import foundation.omni.consensus.OmniCoreConsensusTool
import foundation.omni.consensus.OmniwalletConsensusTool
import foundation.omni.rpc.BalanceEntry
import foundation.omni.rpc.OmniClient
import org.apache.commons.cli.OptionBuilder
import org.apache.commons.cli.OptionGroup
import org.bitcoinj.core.Address

/**
 * Tool to fetch Omni consensus from Omni Core or one of several other Omni APIs.
 */
//@CompileStatic
class ConsensusCLI extends BitcoinCliCommand {
    public final static String commandName = "omni-consensus"
    public final static String commandUsage = "${commandName} [options] -property <id>"

    public ConsensusCLI(String[] args) {
        super(commandName, commandUsage, new ConsensusCLIOptions(), args)
    }

    public static void main(String[] args) {
        ConsensusCLI command = new ConsensusCLI(args)
        def status = command.run()
        System.exit(status)
    }

    @Override
    public int checkArgs() {
        Integer status = super.checkArgs()
        if (status != 0) {
            return status
        }
        // zero (extra) args
        if (line.args.length >= 1) {
            printHelp()
            return 1
        }
        // Must have -p (property id) or -x (compare), but not both
        // TODO: This will change when we allow download of all property ids or comparison of a single one
        if (!(line.hasOption('p') ^ line.hasOption('x'))) {
            printError("Must either specify a property id with -p or the -x/-compare option, but not both")
            printHelp()
            return 1
        }
        return 0
    }

    @Override
    public Integer runImpl() throws IOException, JsonRpcException {
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
            tool1 = new OmniCoreConsensusTool(this.getClient())
        }

        if (line.hasOption("compare")) {
            tool2 = new OmniCoreConsensusTool(this.getClient())
            //pwerr.println "Comparing ${tool2.serverURI} with ${tool1.serverURI}"
            MultiPropertyComparison multiComparison = new MultiPropertyComparison(tool2, tool1);
            multiComparison.compareAllProperties()
        } else {
            def consensus = tool1.getConsensusSnapshot(currencyID)

            if (fileName != null) {
                File output = new File(fileName)
                this.save(consensus, output)
            } else {
                this.print(consensus)
            }
        }

        return 0;
    }

    @Override
    public OmniClient getClient() {
        if (super.client == null) {
            try {
                super.client = new OmniClient(getRPCConfig())
            } catch (IOException e) {
                e.printStackTrace()
            }
        }
        return (OmniClient) super.client
    }

    void save(ConsensusSnapshot snap, File file) {
        PrintWriter pw = file.newPrintWriter()
        output(snap, pw, true)
        pw.flush()
    }

    void print(ConsensusSnapshot snap) {
        output(snap, this.pwout, true)
    }

    void output(ConsensusSnapshot snap, PrintWriter writer, boolean tsv) {
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


    public static class ConsensusCLIOptions extends CliOptions {
        public ConsensusCLIOptions() {
            super()
            this.addOption(OptionBuilder.withLongOpt('output')
                    .withDescription('Output filename')
                    .hasArg()
                    .withArgName('filename')
                    .create('o'))
                .addOption(OptionBuilder.withLongOpt('property')
                    .withDescription('Omni property/currency id (numeric)')
                    .hasArg()
                    .withArgName('id')
                    .create('p'))
                .addOption(OptionBuilder.withLongOpt('compare')
                    .withDescription('Compare properties from two URLs')
                    .create('x'))
                .addOptionGroup(new OptionGroup()
                    .addOption(OptionBuilder.withLongOpt('omnicore-url')
                        .withDescription('Use Omni Core API via URL')
                        .hasArg()
                        .withArgName('url')
                        .create('core'))
                    .addOption(OptionBuilder.withLongOpt('omniwallet-url')
                        .withDescription('Use Omniwallet API via URL')
                        .hasArg()
                        .withArgName('url')
                        .create('wallet'))
                    .addOption(OptionBuilder.withLongOpt('omnichest-url')
                        .withDescription('Use Omnichest API via URL')
                        .hasArg()
                        .withArgName('url')
                        .create('chest')))
        }
    }
}
