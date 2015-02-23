package com.msgilligan.bitcoin.cli;

import com.msgilligan.bitcoin.rpc.JsonRPCException;

import java.io.IOException;
import java.util.Map;

/**
 * A command-line client that prints some basic information retrieved via RPC
 */
public class Status extends CliCommand {
    public final static String commandName = "btcstatus";

    public Status(String[] args) {
        super(commandName, new CliOptions(), args);
    }

    public static void main(String[] args) {
        Status command = new Status(args);
        Integer status = command.run();
        System.exit(status);
    }

    @Override
    public Integer runImpl() throws IOException {
        Map<String, Object> info = null;
        try {
            info = client.getInfo();
        } catch (JsonRPCException e) {
            e.printStackTrace();
            return 1;
        }

        Integer bitcoinVersion = (Integer) info.get("version");
        Integer blocks = (Integer) info.get("blocks");

        pwout.println("Bitcoin Core Version: " + bitcoinVersion);
        pwout.println("Block count: " + blocks);
        return 0;
    }
}
