package foundation.omni.netapi;

import foundation.omni.rpc.ConsensusFetcher;
import org.consensusj.bitcoin.rx.ChainTipService;

/**
 * Consensus service for light (ADAP) wallets, etc. This is the interface used by
 * OmniPortfolio, for example.
 */
public interface ConsensusService extends OmniBalanceService, ConsensusFetcher, ChainTipService {
}
