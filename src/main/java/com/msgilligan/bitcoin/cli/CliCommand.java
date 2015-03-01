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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.msgilligan.bitcoin.rpc.BitcoinClient;
import com.msgilligan.bitcoin.rpc.RPCConfig;

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
    protected String usage;
    protected HelpFormatter formatter = null;

    protected InputStream in;
    protected PrintWriter pwout;
    protected PrintWriter pwerr;

    protected BitcoinClient client = null;

    protected CliCommand(String name, CliOptions options, String[] args) {
        this(name, null, options, args);
    }

    protected CliCommand(String name, String usage, CliOptions options, String[] args) {
        this.name = name;
        if (usage != null) {
            this.usage = usage;
        } else {
            this.usage = name;
        }
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
            System.out.println("Connecting to: " + getRPCConfig().getURI());
            try {
                client = new BitcoinClient(getRPCConfig());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return client;
    }

    /**
     * Can be used by subclasses to set a narrower type
     * @param client An instance of BitcoinClient or subclass
     */
    protected void setClient(BitcoinClient client) {
        this.client = client;
    }

    public void printHelp() {
        if (formatter == null) {
            formatter = new HelpFormatter();
            formatter.setLongOptPrefix("-");
        }
        int leftPad = 4;
        int descPad = 2;
        int helpWidth = 120;
        String header = "";
        String footer = "";
        formatter.printHelp(pwout, helpWidth, usage, header, options, leftPad, descPad, footer, false);
    }

    /**
     * Check Options and Arguments
     *
     * Override to customize behavior.
     *
     * @return status code
     */
    public Integer checkArgs() {
        if (line.hasOption("?")) {
            printHelp();
            // Return 1 so tool can exit
            return 1;
        }
        return 0;
    }

    /**
     * Initial a client and if rpcwait option set, make sure server is accepting connections.
     * @return status code
     */
    public Integer preflight() {
        getClient();
        if (line.hasOption("rpcwait")) {
            boolean available = false;   // Wait up to 1 hour
            try {
                available = client.waitForServer(60*60);
            } catch (JsonRPCException e) {
                this.pwerr.println("JSON-RPC Exception: " + e.getMessage());
                return 1;
            }
            if (!available) {
                this.pwerr.println("Timeout error.");
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

        Integer status = checkArgs();
        if (status != 0) {
            return status;
        }

        status = preflight();
        if (status != 0) {
            return status;
        }

        try {
            status = runImpl();
        } catch (IOException e) {
            e.printStackTrace();
            status = 1;
        } catch (JsonRPCException e) {
            e.printStackTrace();
            status = 1;
        } finally {
            client.closeConnection();
        }
        return status;
    }

    /**
     * Implement in subclasses
     * @return status code
     */
    abstract protected Integer runImpl() throws IOException, JsonRPCException;

    private URI getServerURI() {
        String proto = defaultproto;
        String host = defaulthost;
        int port = defaultport;
        String file = defaultfile;

        if (line.hasOption("rpcssl")) {
            proto = "https";
        }
        if (line.hasOption("rpcconnect")) {
            host = line.getOptionValue("rpcconnect", defaulthost);
        }
        if (line.hasOption("regtest") || line.hasOption("testnet")) {
            port = 18332;
        }
        URI rpcServerURI = null;
        try {
            rpcServerURI = new URI(proto, null, host, port, file, null, null);
        } catch (URISyntaxException e) {
            // We should be careful that this never happens
            e.printStackTrace();
            // But if it does, throw an unchecked exception
            throw new RuntimeException(e);
        }
        return rpcServerURI;
    }

    protected RPCConfig getRPCConfig() {
        URI uri = getServerURI();
        String user = line.getOptionValue("rpcuser", rpcuser);
        String pass = line.getOptionValue("rpcpassword", rpcpassword);
        RPCConfig cfg = new RPCConfig(uri, user, pass);
        return cfg;
    }


}
