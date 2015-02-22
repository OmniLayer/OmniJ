package com.msgilligan.bitcoin.cli;

import com.msgilligan.bitcoin.rpc.JsonRPCException;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
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

    public static void main(String[] args) throws JsonRPCException, IOException {
        BitcoinJCli command = new BitcoinJCli(args);
        Integer status = command.run();
        System.exit(status);
    }

    public Integer run() throws JsonRPCException, IOException {
        return run(System.in, System.out, System.err);
    }

    public Integer run(InputStream in, PrintStream out, PrintStream err) throws JsonRPCException, IOException {
        Integer status = preflight();
        if (status != 0) {
            return status;
        }

        // Hacked together parsing that barely supports two RPC methods
        // TODO: Make this better
        List args = line.getArgList();
        String method = (String) args.get(0);
        args.remove(0); // remove method from list
        if (args.size() > 0 && args.get(0) != null) {
            args.set(0, true);
        }
        Object result = client.cliSend(method, args);
        if (result != null) {
            out.println(result.toString());
        }
        return 0;
    }
}
