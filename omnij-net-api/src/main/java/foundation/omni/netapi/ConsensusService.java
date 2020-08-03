package foundation.omni.netapi;

import foundation.omni.CurrencyID;
import foundation.omni.json.pojo.OmniPropertyInfo;
import foundation.omni.rpc.BalanceEntry;
import foundation.omni.rpc.ConsensusFetcher;
import org.bitcoinj.core.Address;

import java.io.IOException;
import java.util.List;
import java.util.SortedMap;
import java.util.concurrent.CompletableFuture;

/**
 * Consensus service for light (ADAP) wallets, etc. This is the interface used by
 * OmniPortfolio, for example.
 */
public interface ConsensusService extends OmniBalanceService, ConsensusFetcher {
}
