package com.msgilligan.bitcoin.cli;

import com.msgilligan.bitcoin.rpc.BitcoinClient;
import com.msgilligan.bitcoin.rpc.RPCConfig;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * User: sean
 * Date: 7/7/14
 * Time: 9:01 AM
 */
public abstract class CliCommand {
    static final String defaultproto = "http";
    static final String defaulthost = "127.0.0.1";
    static final int defaultport = 8332;
    static final String defaultfile = "/";
    static final String rpcuser ="bitcoinrpc";
    static final String rpcpassword ="pass";

    protected CommandLine line = null;
    protected CommandLineParser parser = null;
    protected CliOptions options;
    protected String name;
    protected HelpFormatter formatter = null;
    protected BitcoinClient client = null;

    protected CliCommand(String name, CliOptions options, String[] args) {
        this.name = name;
        this.options = options;
        parser = new GnuParser();
        try {
            this.line = this.parser.parse(options, args);
        } catch (ParseException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public BitcoinClient getClient() {
        if (client == null) {
            System.out.println("Connecting to: " + getRPCConfig().getUrl());
            try {
                client = new BitcoinClient(getRPCConfig());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return client;
    }

    public void printHelp() {
        if (formatter == null) {
            formatter = new HelpFormatter();
        }
        formatter.printHelp(name, options, true);
    }

    public void preflight() {
        getClient();
        if (line.hasOption("help")) {
            printHelp();
            System.exit(0);
        }
        if (line.hasOption("rpcwait")) {
            Boolean available = client.waitForServer(60*60);   // Wait up to 1 hour
            if (!available) {
                System.out.println("Timeout error.");
                System.exit(1);
            }
        }
    }

    private URL getServerURL() {
        String proto = defaultproto;
        String host = defaulthost;
        int port = defaultport;
        String file = defaultfile;

        URL rpcServerURL = null;
        try {
            rpcServerURL = new URL(proto, host, port, file);
        } catch (MalformedURLException e) {
            // We should be careful that this never happens
            e.printStackTrace();
            // But if it does, throw an unchecked exception
            throw new RuntimeException(e);
        }
        return rpcServerURL;
    }

    private RPCConfig getRPCConfig() {
        URL url = getServerURL();
        RPCConfig cfg = new RPCConfig();
        cfg.setUrl(url);
        cfg.setUsername(rpcuser);
        cfg.setPassword(rpcpassword);
        return cfg;
    }


}
