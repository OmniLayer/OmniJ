package foundation.omni.netapi.omnicore;

import foundation.omni.rpc.OmniClient;
import io.reactivex.rxjava3.core.Flowable;
import org.bitcoinj.core.NetworkParameters;
import org.consensusj.bitcoin.json.pojo.ChainTip;
import org.consensusj.bitcoin.rx.ChainTipService;
import org.consensusj.bitcoin.rx.jsonrpc.PollingChainTipServiceImpl;
import org.consensusj.bitcoin.rx.jsonrpc.RxJsonChainTipClient;
import org.consensusj.bitcoin.rx.zeromq.RxBitcoinZmqService;

import javax.net.ssl.SSLSocketFactory;
import java.net.URI;

/**
 *
 */
public class RxOmniClient extends OmniClient implements RxJsonChainTipClient, OmniProxyMethods {
    ChainTipService chainTipService;

    public RxOmniClient(NetworkParameters netParams, URI server, String rpcuser, String rpcpassword) {
        this(netParams, server, rpcuser, rpcpassword, true);
    }

    public RxOmniClient(NetworkParameters netParams, URI server, String rpcuser, String rpcpassword, boolean useZmq) {
        this((SSLSocketFactory)SSLSocketFactory.getDefault(), netParams, server, rpcuser, rpcpassword, useZmq);
    }

    public RxOmniClient(SSLSocketFactory sslSocketFactory,  NetworkParameters netParams, URI server, String rpcuser, String rpcpassword, boolean useZmq) {
        super(sslSocketFactory, netParams, server, rpcuser, rpcpassword);
        if (useZmq) {
            chainTipService = new RxBitcoinZmqService(this);
        } else {
            // TODO: Set polling  interval and disable polling
            chainTipService = new PollingChainTipServiceImpl(this);
        }
    }

    @Override
    public Flowable<ChainTip> chainTipPublisher() {
        return Flowable.fromPublisher(chainTipService.chainTipPublisher());
    }
}
