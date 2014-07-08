package com.msgilligan.bitcoin.rpc;

import java.net.URL;

/**
 * User: sean
 * Date: 7/6/14
 * Time: 12:21 PM
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
