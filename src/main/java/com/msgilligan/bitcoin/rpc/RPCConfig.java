package com.msgilligan.bitcoin.rpc;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/**
 * Configuration class for JSON-RPC client
 *
 * Contains complete URL, username, and password.
 *
 */
public class RPCConfig {

    private final URI uri;
    private final String   username;
    private final String   password;

    public RPCConfig(URI uri, String username, String password) {
        this.uri = uri;
        this.username = username;
        this.password = password;
    }

    public URI getURI() {
        return uri;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
