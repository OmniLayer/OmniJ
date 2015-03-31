package com.msgilligan.bitcoin.cli;

import com.msgilligan.bitcoin.rpc.JsonRPCException;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 * An attempt at cloning the bitcoin-cli tool, but using Java and bitcoinj
 *
 */
public class BitcoinJCli extends CliCommand {
    public final static String commandName = "bitcoinj-cli";

    public BitcoinJCli(String[] args) {
        super(commandName, new CliOptions(), args);
    }

    public static void main(String[] args) {
        BitcoinJCli command = new BitcoinJCli(args);
        Integer status = command.run();
        System.exit(status);
    }

    public Integer runImpl() throws IOException {
        // Hacked together parsing that barely supports the two RPC methods in the Spock test
        // TODO: Make this better and complete
        List args = line.getArgList();
        String method = (String) args.get(0);
        args.remove(0); // remove method from list
        if (args.size() > 0 && args.get(0) != null) {
            args.set(0, true);
        }
        Object result = null;
        try {
            result = client.cliSend(method, args.toArray());
        } catch (JsonRPCException e) {
            e.printStackTrace();
            return 1;
        }
        if (result != null) {
            pwout.println(result.toString());
        }
        return 0;
    }
}
