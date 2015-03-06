package com.msgilligan.bitcoin.rpc;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * JSON-RPC Client
 */
public class RPCClient {
    private URI serverURI;
    private String username;
    private String password;
    private ObjectMapper mapper;
    private long requestId;
    private static final boolean disableSslVerification = true;

    static {
        if (disableSslVerification) {
            // Disable checks that prevent using a self-signed SSL certificate
            // TODO: Should checks be enabled by default for security reasons?
            disableSslVerification();
        }
    }

    public RPCClient(RPCConfig config) {
        this(config.getURI(), config.getUsername(), config.getPassword());
    }

    public RPCClient(URI server, final String rpcuser, final String rpcpassword) {
        serverURI = server;
        username = rpcuser;
        password = rpcpassword;
        requestId = 0;
        mapper = new ObjectMapper();
    }

    public URI getServerURI() {
        return serverURI;
    }

    public Map<String, Object> send(Map<String, Object> request) throws IOException, JsonRPCException {
        HttpURLConnection connection = openConnection();

        String userpass = username + ":" + password;
        String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.getBytes());
        connection.setRequestProperty ("Authorization", basicAuth);

        OutputStream requestStream = connection.getOutputStream();
        String reqString = mapper.writeValueAsString(request);
//        System.out.println("Req json = " + reqString);
         try {
             mapper.writeValue(requestStream, request);
             requestStream.close();
         }
         catch (IOException logOrIgnore) {
             System.out.println("Exception: " + logOrIgnore);
         }

        InputStream responseStream = null;
        int code = connection.getResponseCode();
        String message = connection.getResponseMessage();
//        System.out.println("Response code: " + code);
        if (code == 200) {
            try {
                responseStream = connection.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
                throw new JsonRPCException("IOException reading response stream", e);
            }
        } else {
            responseStream = connection.getErrorStream();
        }
        String contentType = connection.getContentType();

        String responseString;
        Map<String, Object> responseMap = null;
        if (responseStream != null) {
            responseString = new Scanner(responseStream,"UTF-8").useDelimiter("\\A").next();
            if (contentType.equals("application/json")) {
                responseMap = mapper.readValue(responseString, Map.class);
            }
        } else {
            responseString = "";
            responseMap = new HashMap<String, Object>();
        }

        if (code != 200) {
            String exceptionMessage = message; // Default to HTTP result message

            if (responseMap != null) {
                Map <String, Object> error = (Map <String, Object>) responseMap.get("error");
                if (error != null) {
                    // If there's a more specific message in the JSON use it instead.
                    exceptionMessage = (String) error.get("message");
                }
            }
            throw new JsonRPCStatusException(exceptionMessage, code, message, responseString, responseMap);
        }

        connection.disconnect();
        return responseMap;
    }

    public Map<String, Object> send(String method, List<Object> params) throws IOException, JsonRPCException  {
        Map<String, Object> request = new HashMap<String, Object>();
        request.put("jsonrpc", "1.0");
        request.put("method", method);
        request.put("id", Long.toString(requestId));

        if (params != null) {
            params.removeAll(Collections.singleton(null));  // Remove null entries (should only be at end)
        }
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

    public Object cliSend(String method, List<Object> params) throws IOException, JsonRPCException {
        Map<String, Object> response = send(method, params);
        return response.get("result");
    }

    public Object cliSend(String method, Object... params) throws IOException, JsonRPCException {
        Map<String, Object> response = send(method, Arrays.asList(params));
        return response.get("result");
    }

    public Object cliSend(String method) throws IOException, JsonRPCException {
        Map<String, Object> response = send(method, null);
        return response.get("result");
    }

    private HttpURLConnection openConnection() throws IOException {
        HttpURLConnection connection =  (HttpURLConnection) serverURI.toURL().openConnection();
        connection.setDoOutput(true); // For writes
        connection.setRequestMethod("POST");
//        connection.setRequestProperty("Accept-Charset", StandardCharsets.UTF_8.toString());
//        connection.setRequestProperty("Content-Type", " application/json;charset=" + StandardCharsets.UTF_8.toString());
        connection.setRequestProperty("Accept-Charset", "UTF-8");
        connection.setRequestProperty("Content-Type", "application/json;charset=" +  "UTF-8");
        connection.setRequestProperty("Connection", "close");   // Avoid EOFException: http://stackoverflow.com/questions/19641374/android-eofexception-when-using-httpurlconnection-headers
        return connection;
    }

    private static void disableSslVerification() {
        try
        {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
            };

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }
}
