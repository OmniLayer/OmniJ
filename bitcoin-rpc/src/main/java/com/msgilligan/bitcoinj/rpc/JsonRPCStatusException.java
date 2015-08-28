package com.msgilligan.bitcoinj.rpc;

import java.util.Map;

/**
 * JSON-RPC returned HTTP status other than 200
 * Additional information is usually in JSON-RPC response
 */
public class JsonRPCStatusException extends JsonRPCException {
    public final String httpMessage;
    public final int httpCode;
    public final int jsonRPCCode;
    public final String response;
    public final Map<String, Object> responseJson;

    /**
     * Default Constructor
     *
     * @param message Error message from Json if available, else http status message
     * @param httpCode HTTP status code, e.g. 404
     * @param httpMessage HTTP status message, e.g. "Not found"
     * @param jsonRPCCode Integer error code in JSON response, if any
     * @param responseBody responseBody body as string
     * @param responseBodyJson responseBody body as Json Map
     */
    public JsonRPCStatusException(String message, int httpCode, String httpMessage, int jsonRPCCode, String responseBody, Map<String, Object> responseBodyJson ) {
        super(message);
        this.httpCode = httpCode;
        this.httpMessage = httpMessage;
        this.jsonRPCCode = jsonRPCCode;
        this.response = responseBody;
        this.responseJson = responseBodyJson;
    }
}
