package com.msgilligan.bitcoin.cli;

import com.msgilligan.bitcoin.rpc.RPCConfig;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.HelpFormatter;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Command-line options for tools that communicate with Bitcoin RPC
 */
public class CliOptions extends Options {


    public CliOptions() {
        super();
        this.addOption("?", "help", false, "Display help message")
            .addOption("c", "conf", true, "Specify configuration file (default: bitcoin.conf)")
            .addOption("d", "datadir", true, "Specify data directory")
                .addOptionGroup(new OptionGroup()
                    .addOption(new Option("t", "testnet", false, "Use the test network"))
                    .addOption(new Option("r", "regtest", false, "Enter regression test mode")))
            .addOption("i", "rpcconnect", true, "Send commands to node running on <ip> (default: 127.0.0.1)")
            .addOption("n", "rpcport", true, "Connect to JSON-RPC on <port> (default: 8332 or testnet: 18332)")
            .addOption("w", "rpcwait", false, "Wait for RPC server to start")
            .addOption("u", "rpcuser", true, "Username for JSON-RPC connections")
            .addOption("p", "rpcpassword", true, "Password for JSON-RPC connections")
            .addOption("ssl", "rpcssl", true, "Use https for JSON-RPC connections");
    }

}
