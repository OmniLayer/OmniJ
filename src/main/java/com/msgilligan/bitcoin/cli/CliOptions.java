package com.msgilligan.bitcoin.cli;

import com.msgilligan.bitcoin.rpc.RPCConfig;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.HelpFormatter;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * User: sean
 * Date: 7/5/14
 * Time: 7:59 PM
 */
public class CliOptions extends Options {


    public CliOptions() {
        super();
        this.addOption("?", "help", false, "Display help message");
        this.addOption("c", "conf", true, "Specify configuration file (default: bitcoin.conf)");
        this.addOption("d", "datadir", true, "Specify data directory");
        this.addOption("t", "testnet", false, "Use the test network");
        this.addOption("r", "regtest", false, "Enter regression test mode");
        this.addOption("i", "rpcconnect", true, "Send commands to node running on <ip> (default: 127.0.0.1)");
        this.addOption("n", "rpcport", true, "Connect to JSON-RPC on <port> (default: 8332 or testnet: 18332)");
        this.addOption("w", "rpcwait", false, "Wait for RPC server to start");
        this.addOption("u", "rpcuser", true, "Username for JSON-RPC connections");
        this.addOption("p", "rpcpassword", true, "Password for JSON-RPC connections");
        this.addOption("ssl", "rpcssl", true, "Use https for JSON-RPC connections");
    }

}
