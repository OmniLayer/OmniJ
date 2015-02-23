package com.msgilligan.bitcoin.cli;

import com.msgilligan.bitcoin.rpc.JsonRPCException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import com.msgilligan.bitcoin.rpc.BitcoinClient;
import com.msgilligan.bitcoin.rpc.RPCConfig;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;

/**
 * Base class for CLI commands that use Bitcoin RPC
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

    protected InputStream in;
    protected PrintWriter pwout;
    protected PrintWriter pwerr;

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
        formatter.printUsage(pwout, HelpFormatter.DEFAULT_WIDTH, name, options);
    }

    public Integer preflight() {
        if (line.hasOption("help")) {
            printHelp();
            // Return 1 so tool can exit (but 1 will be status code TODO: fix that)
            return 1;
        }
        getClient();
        if (line.hasOption("rpcwait")) {
            boolean available = client.waitForServer(60*60);   // Wait up to 1 hour
            if (!available) {
                System.out.println("Timeout error.");
                return 1;
            }
        }
        return 0;
    }

    public Integer run() {
        return run(System.in, System.out, System.err);
    }

    public Integer run(InputStream in, PrintStream out, PrintStream err) {
        this.in = in;
        this.pwout = new PrintWriter(out, true);
        this.pwerr = new PrintWriter(err, true);

        Integer status = preflight();
        if (status != 0) {
            return status;
        }

        try {
            return runImpl();
        } catch (IOException e) {
            e.printStackTrace();
            return 1;
        } catch (JsonRPCException e) {
            e.printStackTrace();
            return 1;
        }
    }

    /**
     * Implement in subclasses
     * @return status code
     */
    abstract protected Integer runImpl() throws IOException, JsonRPCException;

    private URL getServerURL() {
        String proto = defaultproto;
        String host = defaulthost;
        int port = defaultport;
        String file = defaultfile;

        if (line.hasOption("regtest") || line.hasOption("testnet")) {
            port = 18332;
        }
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

        String user = line.getOptionValue("rpcuser", rpcuser);
        String pass = line.getOptionValue("rpcpassword", rpcpassword);
        cfg.setUsername(user);
        cfg.setPassword(pass);
        return cfg;
    }


}
