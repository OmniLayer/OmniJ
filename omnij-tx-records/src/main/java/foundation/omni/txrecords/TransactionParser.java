package foundation.omni.txrecords;

import foundation.omni.tx.PayloadParser;
import foundation.omni.tx.Transactions;
import org.bitcoinj.base.Address;

/**
 *
 */
public class TransactionParser {

     public static Transactions.OmniTx parse(byte[] payload, Address refAddress) {
          PayloadParser parser = PayloadParser.create(payload);
          short version       = parser.getVersion();
          short txCode        = parser.getTransaction();
          return switch (Transactions.TransactionType.valueOf(txCode)) {

               case SIMPLE_SEND         -> new TransactionRecords.SimpleSend(
                                             version,
                                             refAddress,
                                             parser.getCurrencyID(),
                                             parser.getWilletts());

               case SEND_TO_OWNERS      -> new TransactionRecords.SendToOwners(
                                             version,
                                             parser.getCurrencyID(),
                                             parser.getWilletts());

               default                  -> throw new IllegalArgumentException("invalid tx");
          };
     }

}
