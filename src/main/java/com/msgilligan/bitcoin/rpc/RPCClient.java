package com.msgilligan.bitcoin.rpc;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
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

    public RPCClient(URL server, final String rpcuser, final String rpcpassword) throws IOException {
        Authenticator.setDefault(new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(rpcuser, rpcpassword.toCharArray());
            }
        });

        serverURL = server;
        mapper = new ObjectMapper();
    }

    public Map<String, Object> send(Map<String, Object> request) throws IOException {
        openConnection();
        OutputStream output = connection.getOutputStream();
        String reqString = mapper.writeValueAsString(request);
        System.out.println("Req json = " + reqString);
        mapper.writeValue(output, request);
         try {
             output.close();
         }
         catch (IOException logOrIgnore) {
             System.out.println("Exception: " + logOrIgnore);
         }

        int code = connection.getResponseCode();
        System.out.println("Response code: " + code);
        InputStream responseStream = connection.getInputStream();

        Map<String, Object> responseMap = mapper.readValue(responseStream, Map.class);
        closeConnection();
        return responseMap;
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
