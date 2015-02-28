package foundation.omni.cli

import com.msgilligan.bitcoin.cli.CliCommand
import com.msgilligan.bitcoin.cli.CliOptions
import com.msgilligan.bitcoin.rpc.JsonRPCException
import foundation.omni.CurrencyID
import foundation.omni.consensus.ChestConsensusTool
import foundation.omni.consensus.ConsensusEntry
import foundation.omni.consensus.ConsensusSnapshot
import foundation.omni.consensus.ConsensusTool
import foundation.omni.consensus.OmniCoreConsensusTool
import foundation.omni.consensus.OmniwalletConsensusTool
import foundation.omni.rpc.OmniClient
import org.apache.commons.cli.OptionBuilder
import org.apache.commons.cli.OptionGroup
import org.bitcoinj.core.Address

/**
 * Tool to fetch Omni consensus from Omni Core or one of several other Omni APIs.
 */
class ConsensusCLI extends CliCommand {
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
    public Integer checkArgs() {
        Integer status = super.checkArgs()
        if (status != 0) {
            return status
        }
        // zero (extra) args
        if (line.args.length >= 1) {
            printHelp()
            return 1
        }
        if (!line.hasOption('p') ) {
            printHelp()
            return 1
        }
        return 0
    }

    @Override
    public Integer runImpl() throws IOException, JsonRPCException {
        String property = line.getOptionValue("property")
        Long currencyIDNum =  Long.parseLong(property, 10)
        CurrencyID currencyID = new CurrencyID(currencyIDNum)

        String fileName = line.getOptionValue("output")

        ConsensusTool tool
        if (line.hasOption("omniwallet-url")) {
            tool = new OmniwalletConsensusTool(line.getOptionValue("omniwallet-url").toURI())
        } else if (line.hasOption("omnichest-url")) {
            tool = new ChestConsensusTool(line.getOptionValue("omnichest-url").toURI())
        } else {
            tool = new OmniCoreConsensusTool(this.getClient())
        }

        def consensus = tool.getConsensusSnapshot(currencyID)

        if (fileName != null) {
            File output = new File(fileName)
            this.save(consensus, output)
        } else {
            this.print(consensus)
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
        snap.entries.each { Address address, ConsensusEntry entry ->
            String balance = entry.balance.toPlainString()
            String reserved = entry.reserved.toPlainString()
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
                // Technically the -rpc* options should also be mutually exclusive with these
                // but the current implmentation just ignores them if -ow or -oc is present.
                .addOptionGroup(new OptionGroup()
                    .addOption(OptionBuilder.withLongOpt('omniwallet-url')
                        .withDescription('Use Omniwallet API via URL')
                        .hasArg()
                        .withArgName('url')
                        .create('ow'))
                    .addOption(OptionBuilder.withLongOpt('omnichest-url')
                        .withDescription('Use Omnichest API via URL')
                        .hasArg()
                        .withArgName('url')
                        .create('oc')))
        }
    }
}
