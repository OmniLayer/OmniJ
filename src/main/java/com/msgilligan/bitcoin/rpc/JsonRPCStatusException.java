package com.msgilligan.bitcoin.rpc;

import java.util.Map;

/**
 * Json RPC returned HTTP status other than 200
 * Additional information is usually in Json response
 */
public class JsonRPCStatusException extends JsonRPCException {
    public final String httpMessage;
    public final int httpCode;
    public final String response;
    public final Map<String, Object> responseJson;

    /**
     *
     * @param message Error message from Json if available, else http status message
     * @param httpCode HTTP status code, e.g. 404
     * @param httpMessage HTTP status message, e.g. "Not found"
     * @param response response body as string
     * @param responseJson response body as Json Map
     */
    public JsonRPCStatusException(String message, int httpCode, String httpMessage, String response, Map<String, Object> responseJson ) {
        super(message);
        this.httpCode = httpCode;
        this.httpMessage = httpMessage;
        this.response = response;
        this.responseJson = responseJson;
    }
}
