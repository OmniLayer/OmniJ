package foundation.omni.consensus;

import foundation.omni.rpc.BalanceEntry;
import foundation.omni.rpc.ConsensusSnapshot;
import org.bitcoinj.core.Address;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 *
 */
public interface ConsensusToolOutput {
    static void save(ConsensusSnapshot snap, File file) throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(file);
        output(snap, pw, true);
        pw.flush();
    }

    static void print(ConsensusSnapshot snap, PrintWriter pwout) {
        output(snap, pwout, false);
    }

    /**
     * Output a ConsensusSnapshot to a PrintWriter.
     *
     * @param snap Snapshot to output
     * @param writer where to output
     * @param tsv if true, use Tab-separated values. else use colons
     */
    static void output(ConsensusSnapshot snap, PrintWriter writer, boolean tsv) {
        snap.getEntries().forEach((Address address, BalanceEntry entry) -> {
            if (tsv) {
                writer.println(address + "\t" + entry.getBalance().toJsonFormattedString() +
                        "\t" + entry.getReserved().toJsonFormattedString() +
                        "\t" + entry.getFrozen().toJsonFormattedString());
            } else {
                writer.println(address + ": " + entry.getBalance().toJsonFormattedString() +
                        ", " + entry.getReserved().toJsonFormattedString() +
                        ", " + entry.getFrozen().toJsonFormattedString());
            }
        });
    }
}
