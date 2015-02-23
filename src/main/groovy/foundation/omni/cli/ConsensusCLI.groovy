package foundation.omni.cli

import com.msgilligan.bitcoin.cli.CliCommand
import com.msgilligan.bitcoin.cli.CliOptions
import com.msgilligan.bitcoin.rpc.BitcoinClient
import com.msgilligan.bitcoin.rpc.JsonRPCException
import foundation.omni.CurrencyID
import foundation.omni.consensus.ConsensusEntry
import foundation.omni.consensus.ConsensusFetcher
import foundation.omni.consensus.ConsensusSnapshot
import foundation.omni.consensus.OmniCoreConsensusTool
import foundation.omni.rpc.OmniClient
import groovy.transform.CompileStatic
import org.bitcoinj.core.Address

/**
 * Tool to get fetch consensus
 */
@CompileStatic
class ConsensusCLI extends CliCommand {
    public final static String commandName = "omni-consensus"

    public ConsensusCLI(String[] args) {
        super(commandName, new ConsensusCLIOptions(), args)
    }

    public static void main(String[] args) {
        ConsensusCLI command = new ConsensusCLI(args)
        def status = command.run()
        System.exit(status)
    }

    @Override
    public Integer runImpl() throws IOException, JsonRPCException {
        Long currencyIDNum =  line.args.length > 0 ? Long.parseLong(line.args[0], 10) : CurrencyID.MSC_VALUE
        CurrencyID currencyID = new CurrencyID(currencyIDNum)

        String fileName = line.getOptionValue("output")

        OmniCoreConsensusTool tool = new OmniCoreConsensusTool(this.getClient())


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
        output(snap, this.pwout, false)
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
            this.addOption("o", "output", true, "Output filename")
        }
    }
}
