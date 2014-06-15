import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.msgilligan.bitcoin.rpc.RPCClient;

public class GetBlockCount {
    static final String rpcuser ="bitcoinrpc";
    static final String rpcpassword ="pass";
    static RPCClient client;

    public static void main(String[] args) {

        String blockCount = "<error>";
        try {
//            URL rpcServerURL = new URL("http://localhost:8332");
//            URL rpcServerURL = new URL("http://localhost:8080/bitcoin-rpc");
            URL rpcServerURL = new URL("http://127.0.0.1:28332");
            System.out.println("Connecting to: " + rpcServerURL);
            client = new RPCClient(rpcServerURL, rpcuser, rpcpassword);
            blockCount = getBlockCount().toString();
            System.out.println("Block count is: " + blockCount);
            setGenerate();
            blockCount = getBlockCount().toString();
            System.out.println("Block count is: " + blockCount);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Integer getBlockCount() throws IOException {
        Map<String, Object> jsonrpcReq = new HashMap<>();
        jsonrpcReq.put("jsonrpc", "2.0");
        jsonrpcReq.put("method", "getblockcount");
        jsonrpcReq.put("id", "1");

        Map<String, Object> response = client.send(jsonrpcReq);

        assert response.get("jsonrpc").equals("2.0");
        assert response.get("id").equals("1");

        Integer blockCount = (Integer) response.get("result");
        return blockCount;
    }

    public static String setGenerate() throws IOException {
        Map<String, Object> jsonrpcReq = new HashMap<>();
        jsonrpcReq.put("jsonrpc", "2.0");
        jsonrpcReq.put("method", "setgenerate");
        jsonrpcReq.put("id", "2");

        List<Object> params = new ArrayList<>();
        params.add(true);
        params.add(101);

        jsonrpcReq.put("params", params);

        Map<String, Object> response = client.send(jsonrpcReq);

        assert response.get("jsonrpc").equals("2.0");
        assert response.get("id").equals("2");

        String result = (String) response.get("result");
        return result;
    }

}
