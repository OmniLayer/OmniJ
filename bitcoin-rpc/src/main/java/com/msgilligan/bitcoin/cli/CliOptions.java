package com.msgilligan.bitcoin.cli;

import com.msgilligan.bitcoin.rpc.RPCConfig;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
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
        this.addOption("?", null, false, "This help message")
// 'conf' and 'datadir' aren't implemented yet.
//            .addOption("c", "conf", true, "Specify configuration file (default: bitcoin.conf)")
//            .addOption("d", "datadir", true, "Specify data directory")
            .addOptionGroup(new OptionGroup()
                    .addOption(new Option(null, "testnet", false, "Use the test network"))
                    .addOption(new Option(null, "regtest", false, "Enter regression test mode")))
            .addOption(OptionBuilder.withLongOpt("rpcconnect")
                    .withDescription("Send commands to node running on <ip> (default: 127.0.0.1)")
                    .hasArg()
                    .withArgName("ip")
                    .create())
                .addOption(OptionBuilder.withLongOpt("rpcport")
                        .withDescription("Connect to JSON-RPC on <port> (default: 8332 or testnet: 18332)")
                        .hasArg()
                        .withArgName("port")
                        .create())
                .addOption(null, "rpcwait", false, "Wait for RPC server to start")
                .addOption(OptionBuilder.withLongOpt("rpcuser")
                        .withDescription("Username for JSON-RPC connections")
                        .hasArg()
                        .withArgName("user")
                        .create())
                .addOption(OptionBuilder.withLongOpt("rpcpassword")
                        .withDescription("Password for JSON-RPC connections")
                        .hasArg()
                        .withArgName("pw")
                        .create())
            .addOption(null, "rpcssl", false, "Use https for JSON-RPC connections");
    }

}
