package foundation.omni.consensus;

import foundation.omni.CurrencyID;
import foundation.omni.rpc.BalanceEntry;
import foundation.omni.rpc.ConsensusFetcher;
import foundation.omni.rpc.ConsensusSnapshot;
import org.bitcoinj.core.Address;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Interface with default methods for fetching Omni Protocol consensus data
 */
public interface ConsensusTool extends ConsensusFetcher {

    default void run(List<String> args) throws IOException, InterruptedException {
        long currencyIDNum =  (args.get(0) != null) ? Long.parseLong(args.get(0), 10) : CurrencyID.OMNI_VALUE;
        CurrencyID currencyID = new CurrencyID(currencyIDNum);

        String fileName = args.get(1);

        ConsensusSnapshot consensus = this.getConsensusSnapshot(currencyID);

        if (fileName != null) {
            File output = new File(fileName);
            this.save(consensus, output);
        } else {
            this.print(consensus);
        }
    }

    default void save(ConsensusSnapshot snap, File file) throws FileNotFoundException {
        output(snap, new PrintWriter(file), true);
    }

    default void print(ConsensusSnapshot snap) {
        output(snap, new PrintWriter(System.out), false);
    }

    /**
     * Output a ConsensusSnapshot to a PrintWriter.
     *
     * @param snap Snapshot to output
     * @param writer where to output
     * @param tsv if true, use Tab-separated values. else use colons
     */
    default void output(ConsensusSnapshot snap, PrintWriter writer, boolean tsv) {
        snap.getEntries().forEach((Address address, BalanceEntry entry) -> {
            if (tsv) {
                writer.println(address + "\t" + entry.getBalance() + "\t" + entry.getReserved());
            } else {
                writer.println(address + ":" + entry.getBalance() + ", " + entry.getReserved());
            }
        });
    }
}
