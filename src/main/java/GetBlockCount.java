import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import com.msgilligan.bitcoin.rpc.BitcoinClient;
import com.msgilligan.bitcoin.rpc.CliTool;

public class GetBlockCount extends CliTool {

    public GetBlockCount(String[] args) {
        super(args);
    }

    public static void main(String[] args) {
        GetBlockCount tool = new GetBlockCount(args);
        tool.count();
        tool.incrementAndCount(2);
    }

    public void count() {
        Integer blockCount = -1;
        try {
            blockCount = client.getBlockCount();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Starting Block count is: " + blockCount);
    }

    public void incrementAndCount(long blocksToGen) {
        Integer blockCount = -1;
        try {
            BigDecimal balance = client.getBalance(null, null);
            System.out.println("Starting balance: " + balance);
            client.setGenerate(true, 101L);
            balance = client.getBalance(null, null);
            System.out.println("Balance after mining 101 blocks: " + balance);

            List<Object> balances = client.listReceivedByAddress(1, false);
            System.out.println("balances: " + balances);

            client.setGenerate(true, blocksToGen);
            blockCount = client.getBlockCount();
            String address1 = client.getNewAddress();
            String address2 = client.getNewAddress();
            System.out.println("Address: " + address1);
            System.out.println("Address: " + address2);
            String txid = client.sendToAddress(address2, BigDecimal.valueOf(1), "comment", "comment-to");
            System.out.println("txid: " + txid);
            client.setGenerate(true, 6L);
            Map<String, Object> transaction = client.getTransaction(txid);
            System.out.println("transaction: " + transaction);

            balance = client.getBalance(null, null);
            System.out.println("Ending balance: " + balance);

            balances = client.listReceivedByAddress(0, true);
            System.out.println("balances: " + balances);

            blockCount = client.getBlockCount();

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Block count is: " + blockCount);
    }

}
