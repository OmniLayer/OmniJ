package com.msgilligan.bitcoin.cli;

import com.msgilligan.bitcoin.rpc.JsonRPCException;

import java.io.IOException;

/**
 * A test CLI client that simply gets the blockCount
 */
public class GetBlockCount extends CliCommand {
    public final static String commandName = "getblockcount";

    public GetBlockCount(String[] args) {
        super(commandName, new CliOptions(), args);
    }

    public static void main(String[] args) throws Exception {
        GetBlockCount command = new GetBlockCount(args);
        Integer status = command.run();
        System.exit(status);
    }

    @Override
    public Integer runImpl() throws IOException {
        Integer blockCount = -1;
        try {
            blockCount = client.getBlockCount();
        } catch (JsonRPCException e) {
            e.printStackTrace();
            return 1;
        } catch (IOException e) {
            e.printStackTrace();
            return 1;
        }
        pwout.println("Block count: " + blockCount);
        return 0;
    }
}
