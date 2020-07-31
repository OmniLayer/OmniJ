package foundation.omni.consensus;

import foundation.omni.CurrencyID;
import foundation.omni.rpc.ConsensusFetcher;
import foundation.omni.rpc.ConsensusSnapshot;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Interface with default {@code run} method for fetching and outputting Omni Protocol consensus data
 */
public interface ConsensusTool extends ConsensusFetcher {

    default void run(List<String> args) throws IOException, InterruptedException, ExecutionException {
        long currencyIDNum =  (args.get(0) != null) ? Long.parseLong(args.get(0), 10) : CurrencyID.OMNI_VALUE;
        CurrencyID currencyID = new CurrencyID(currencyIDNum);

        String fileName = args.get(1);

        ConsensusSnapshot consensus = this.getConsensusSnapshot(currencyID);

        if (fileName != null) {
            ConsensusToolOutput.save(consensus, new File(fileName));
        } else {
            ConsensusToolOutput.print(consensus, new PrintWriter(System.out, true));
        }
    }
}
