package com.msgilligan.bitcoin.rpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger log = LoggerFactory.getLogger(RPCClient.class);
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

    /**
     * Send a JSON-RPC request to the server and return a response.
     *
     * @param request JSON-RPC request in Map format
     * @return
     * @throws IOException when thrown by the underlying HttpURLConnection
     * @throws JsonRPCStatusException when the HTTP response code is other than 200
     */
    protected Map<String, Object> send(Map<String, Object> request) throws IOException, JsonRPCStatusException {
        HttpURLConnection connection = openConnection();

        // TODO: Make sure HTTP keep-alive will work
        // See: http://docs.oracle.com/javase/7/docs/technotes/guides/net/http-keepalive.html
        // http://developer.android.com/reference/java/net/HttpURLConnection.html
        // http://android-developers.blogspot.com/2011/09/androids-http-clients.html

        if (log.isDebugEnabled()) {
            log.debug("Req json: {}", mapper.writeValueAsString(request));
        }

        OutputStream requestStream = connection.getOutputStream();
        mapper.writeValue(requestStream, request);
        requestStream.close();

        int responseCode = connection.getResponseCode();
        log.debug("Response code: {}", responseCode);

        Map<String, Object> responseJson = null;
        if (responseCode == 200) {
            // Read JSON and return responseJson
            responseJson = mapper.readValue(connection.getInputStream(), Map.class);
        } else {
            // Prepare and throw JsonRPCStatusException with all relevant info
            String responseMessage = connection.getResponseMessage();
            String exceptionMessage = responseMessage;
            Map<String, Object> bodyJson = null;    // Body as JSON if available
            String bodyString = null;               // Body as String if not JSON
            if (connection.getContentType().equals("application/json")) {
                // We got a JSON error response, parse it
                bodyJson = mapper.readValue(connection.getErrorStream(), Map.class);
                Map <String, Object> error = (Map <String, Object>) bodyJson.get("error");
                if (error != null) {
                    // If there's a more specific message in the JSON use it instead.
                    exceptionMessage = (String) error.get("message");
                }
            } else {
                // No JSON, read response body as string
                InputStream errorStream = connection.getErrorStream();
                bodyString = new Scanner(errorStream,"UTF-8").useDelimiter("\\A").next();
                errorStream.close();
            }
            throw new JsonRPCStatusException(exceptionMessage, responseCode, responseMessage, bodyString, bodyJson);
        }

        log.debug("Resp json: {}", responseJson);

        connection.disconnect();
        return responseJson;
    }

    protected Map<String, Object> send(String method, List<Object> params) throws IOException, JsonRPCStatusException {
        Map<String, Object> request = new HashMap<String, Object>();
        request.put("jsonrpc", "1.0");
        request.put("method", method);
        request.put("id", Long.toString(requestId));

        if (params != null) {
            // TODO: Should only remove nulls from the end
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

        String auth = username + ":" + password;
        String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(auth.getBytes());
        connection.setRequestProperty ("Authorization", basicAuth);

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

    /**
     * Create a mutable param list (so send() can remove null parameters)
     */
    protected List<Object> createParamList(Object... parameters) {
        List<Object> paramList = new ArrayList<Object>(Arrays.asList(parameters));
        return paramList;
    }
}
