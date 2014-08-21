package com.msgilligan.bitcoin.rpc;

import java.net.URL;

/**
 * Configuration class for JSON-RPC client
 *
 * Contains complete URL, username, and password.
 *
 */
public class RPCConfig {

    public URL      url;
    public String   username;
    public String   password;

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
