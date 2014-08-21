package com.msgilligan.bitcoin.cli;

import com.msgilligan.bitcoin.rpc.BitcoinClient;

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

    public static void main(String[] args) throws IOException {
        Status command = new Status(args);
        command.run();
    }

    public void run() throws IOException {
        preflight();
        Map<String, Object> info = client.getInfo();

        Integer bitcoinVersion = (Integer) info.get("version");
        Integer masterCoreVersion = (Integer) info.get("mastercoreversion");
        Integer blocks = (Integer) info.get("blocks");

        System.out.println("Bitcoin Core Version: " + bitcoinVersion);
        if (masterCoreVersion != null) {
            System.out.println("Master Core version: " + masterCoreVersion);
        }
        System.out.println("Block count: " + blocks);
        System.exit(0);
    }

}
