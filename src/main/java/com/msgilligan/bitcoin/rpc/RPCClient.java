package com.msgilligan.bitcoin.rpc;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: sean
 * Date: 4/25/14
 * Time: 10:29 AM
 */
public class RPCClient {
    private URL serverURL;
    private HttpURLConnection connection;
    private ObjectMapper mapper;
    private long requestId;

    public RPCClient(RPCConfig config) {
        this(config.getUrl(), config.getUsername(), config.getPassword());
    }

    public RPCClient(URL server, final String rpcuser, final String rpcpassword) {
        Authenticator.setDefault(new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(rpcuser, rpcpassword.toCharArray());
            }
        });

        serverURL = server;
        requestId = 0;
        mapper = new ObjectMapper();
    }

    public URL getServerURL() {
        return serverURL;
    }

    public Map<String, Object> send(Map<String, Object> request) throws IOException {
        openConnection();
        OutputStream output = connection.getOutputStream();
        String reqString = mapper.writeValueAsString(request);
//        System.out.println("Req json = " + reqString);
         try {
             mapper.writeValue(output, request);
             output.close();
         }
         catch (IOException logOrIgnore) {
             System.out.println("Exception: " + logOrIgnore);
         }

        int code = connection.getResponseCode();
        if (code != 200) {
            System.out.println("Response code: " + code);
            // Should probably throw exception based upon response code right here
        }
        InputStream responseStream = null;
        try {
            responseStream = connection.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }

        Map<String, Object> responseMap = null;
        if (responseStream != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> r = mapper.readValue(responseStream, Map.class);
            responseMap = r;
        }
        closeConnection();
        return responseMap;
    }

    public Map<String, Object> send(String method, List<Object> params) throws IOException {
        Map<String, Object> request = new HashMap<String, Object>();
        params.removeAll(Collections.singleton(null));  // Remove null entries (should only be at end)
        request.put("jsonrpc", "1.0");
        request.put("method", method);
        request.put("id", Long.toString(requestId));

        request.put("params", params);

        Map<String, Object> response = send(request);

//        assert response != null;
//        assert response.get("jsonrpc") != null;
//        assert response.get("jsonrpc").equals("2.0");
//        assert response.get("id") != null;
//        assert response.get("id").equals(Long.toString(requestId++));

        requestId++;

        return response;
    }

    public Object cliSend(String method, List<Object> params) throws IOException {
        Map<String, Object> response = send(method, params);
        return response.get("result");
    }

    public Object cliSend(String method, Object... params) throws IOException {
        Map<String, Object> response = send(method, Arrays.asList(params));
        return response.get("result");
    }

    private void openConnection() throws IOException {
        connection =  (HttpURLConnection) serverURL.openConnection();
        connection.setDoOutput(true); // For writes
        connection.setRequestMethod("POST");
//        connection.setRequestProperty("Accept-Charset", StandardCharsets.UTF_8.toString());
//        connection.setRequestProperty("Content-Type", " application/json;charset=" + StandardCharsets.UTF_8.toString());
        connection.setRequestProperty("Accept-Charset", "UTF-8");
        connection.setRequestProperty("Content-Type", "application/json;charset=" +  "UTF-8");
        connection.setRequestProperty("Connection", "close");   // Avoid EOFException: http://stackoverflow.com/questions/19641374/android-eofexception-when-using-httpurlconnection-headers
    }

    private void closeConnection() {
        connection.disconnect();
    }
}
