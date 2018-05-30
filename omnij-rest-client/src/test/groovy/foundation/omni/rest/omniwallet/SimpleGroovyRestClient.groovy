package foundation.omni.rest.omniwallet

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

import static java.net.HttpURLConnection.HTTP_OK

/**
 * A simple Groovy REST client with no dependencies beyond Groovy and Groovy JSON
 */
class SimpleGroovyRestClient {
    static final String userAgent = 'OmniJ/SimpleGroovyRestClient'
    final URL baseURL

    SimpleGroovyRestClient(URL baseURL) {
        this.baseURL = baseURL
    }

    Object getJson(String file) {
        URL endpoint = new URL(baseURL, file)
        HttpURLConnection conn = endpoint.openConnection()
        conn.setRequestProperty("User-Agent", userAgent)
        def rc = conn.getResponseCode()
        if (rc != HTTP_OK) {
            throw new RuntimeException("bad response code: ${rc}")
        }
        JsonSlurper slurper = new JsonSlurper()
        Object result = slurper.parse(conn.getInputStream())
        return result
    }

    /**
     * Post Form UrlEncoded data to get JSON
     * @param file full 'file' path from host
     * @param params  Map to Form UrlEncode
     * @return Object JSON as object
     */
    Object postForm(String file, Map params) {
        URL endpoint = new URL(baseURL, file)
        def contentString = formEncode(params)
        HttpURLConnection conn = endpoint.openConnection()
        conn.requestMethod = "POST"
        conn.doOutput = true
        conn.setRequestProperty("User-Agent", userAgent)
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        conn.setRequestProperty("Accept", "application/json")
        conn.outputStream.write(contentString.getBytes("UTF-8"));
        def rc = conn.getResponseCode()
        if (rc != HTTP_OK) {
            String responseMessage = conn.getResponseMessage()
            println "responseMessage = ${responseMessage}"
            throw new RuntimeException("bad response code: ${rc}")
        }
        JsonSlurper slurper = new JsonSlurper()
        Object result = slurper.parse(conn.getInputStream())
        return result
    }

    /**
     * Post JSON to get JSON
     * @param file full 'file' path from host
     * @param content  object to post as JSON
     * @return Object JSON as object
     */
    Object postJson(String file, Object body) {
        URL endpoint = new URL(baseURL, file)
        def bodyString = new JsonBuilder(body).toPrettyString()
        HttpURLConnection conn = endpoint.openConnection()
        conn.requestMethod = "POST"
        conn.doOutput = true
        conn.setRequestProperty("User-Agent", userAgent)
        conn.setRequestProperty("Content-Type", "application/json")
        conn.outputStream.write(bodyString.getBytes("UTF-8"));
        def rc = conn.getResponseCode()
        if (rc != HTTP_OK) {
            throw new RuntimeException("bad response code: ${rc}")
        }
        JsonSlurper slurper = new JsonSlurper()
        Object result = slurper.parse(conn.getInputStream())
        return result
    }

    /**
     * URL encode parameter map
     *
     * TODO: Use a better multimap implementation (is there one in Groovy core?)
     *
     * @param params is a quick-and-dirty multimap
     * @return params encoded as a string
     * @throws UnsupportedEncodingException
     */
    static String formEncode(Map<String, Object> params) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder()
        params.eachWithIndex { key, value, index ->
            if (index > 0) {
                sb <<= '&'
            }
            if (value instanceof String) {
                sb <<= URLEncoder.encode(key, 'UTF-8')
                sb <<= '='
                sb <<= URLEncoder.encode(value, 'UTF-8')
            } else {
                value.eachWithIndex { String arrayString, inner ->
                    if (inner > 0) {
                        sb <<= '&'
                    }
                    sb <<= URLEncoder.encode(key, 'UTF-8')
                    sb <<= '='
                    sb <<= URLEncoder.encode(arrayString, 'UTF-8')
                }
            }
        }
        String  result = sb.toString()
        //println "result: ${result}"
        return result
    }
}
