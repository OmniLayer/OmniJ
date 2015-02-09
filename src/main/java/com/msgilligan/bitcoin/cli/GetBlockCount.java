package com.msgilligan.bitcoin.cli;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Sha256Hash;
import com.msgilligan.bitcoin.rpc.JsonRPCException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * A test CLI client that grew beyond just getting the blockCount
 */
public class GetBlockCount extends CliCommand {
    public final static String commandName = "getblockcount_needs_rename";

    public GetBlockCount(String[] args) {
        super(commandName, new CliOptions(), args);
    }

    public static void main(String[] args) {
        GetBlockCount tool = new GetBlockCount(args);
    }

    public void run() throws IOException {
        preflight();
        this.count();
        this.incrementAndCount(2);
        System.exit(0);
    }

    public void count() {
        Integer blockCount = -1;
        try {
            blockCount = client.getBlockCount();
        } catch (JsonRPCException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Starting Block count is: " + blockCount);
    }

    public void incrementAndCount(long blocksToGen) {
        Integer blockCount = -1;
        try {
            BigDecimal balance = client.getBalance();
            System.out.println("Starting balance: " + balance);
            client.setGenerate(true, 101L);
            balance = client.getBalance();
            System.out.println("Balance after mining 101 blocks: " + balance);

            List<Object> balances = client.listReceivedByAddress(1, false);
            System.out.println("balances: " + balances);

            client.setGenerate(true, blocksToGen);
            blockCount = client.getBlockCount();
            Address address1 = client.getNewAddress();
            Address address2 = client.getNewAddress();
            System.out.println("Address: " + address1);
            System.out.println("Address: " + address2);
            Sha256Hash txid = client.sendToAddress(address2, BigDecimal.valueOf(1), "comment", "comment-to");
            System.out.println("txid: " + txid);
            client.setGenerate(true, 6L);
            Map<String, Object> transaction = client.getTransaction(txid);
            System.out.println("transaction: " + transaction);

            balance = client.getBalance();
            System.out.println("Ending balance: " + balance);

            balances = client.listReceivedByAddress(0, true);
            System.out.println("balances: " + balances);

            blockCount = client.getBlockCount();
        } catch (JsonRPCException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Block count is: " + blockCount);
    }

}
