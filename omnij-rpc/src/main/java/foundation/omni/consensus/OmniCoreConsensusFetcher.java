package foundation.omni.consensus;

import com.msgilligan.jsonrpc.JsonRPCException;
import com.msgilligan.bitcoinj.rpc.RPCURI;
import foundation.omni.CurrencyID;
import foundation.omni.rpc.BalanceEntry;
import foundation.omni.rpc.ConsensusFetcher;
import foundation.omni.rpc.ConsensusSnapshot;
import foundation.omni.rpc.OmniClient;
import foundation.omni.rpc.SmartPropertyListInfo;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.NetworkParameters;

import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.util.List;
import java.util.SortedMap;

/**
 *
 */
public class OmniCoreConsensusFetcher implements ConsensusFetcher {
    protected OmniClient client;

    public OmniCoreConsensusFetcher(NetworkParameters netParams, URI coreURI, String user, String pass) {
        OmniClient client = null;
        try {
            client = new OmniClient(netParams, coreURI, user, pass);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.client = client;
    }

    /**
     * URI Constructor
     *
     * @param netParams *bitcoinj* NetworkParameters for server to connect to
     * @param coreURI URI to connect to - user/pass if required, must be encoded in URL
     */
    public OmniCoreConsensusFetcher(NetworkParameters netParams, URI coreURI)
    {
        this(netParams, coreURI, coreURI.getUserInfo().split(":")[0], coreURI.getUserInfo().split(":")[1]);
    }

    /**
     * Constructor that takes an existing OmniClient
     *
     * @param client An existing client instance
     */
    public OmniCoreConsensusFetcher(OmniClient client)
    {
        this.client = client;
    }

    public OmniClient getOmniClient() {
        return client;
    }

    public SortedMap<Address, BalanceEntry> getConsensusForCurrency(CurrencyID currencyID) {
        SortedMap<Address, BalanceEntry> balances = null;
        try {
            balances = client.omniGetAllBalancesForId(currencyID);
        } catch (JsonRPCException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (AddressFormatException e) {
            throw new RuntimeException(e);
        }
        // TODO: Filter out empty address strings or 0 balances?
        return balances;
    }

    @Override
    public Integer currentBlockHeight() {
        try {
            return client.getBlockCount();
        } catch (JsonRPCException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<SmartPropertyListInfo> listProperties() {
        List<SmartPropertyListInfo> props = null;
        try {
            props = client.omniListProperties();
        } catch (JsonRPCException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return props;
    }

    @Override
    public ConsensusSnapshot getConsensusSnapshot(CurrencyID currencyID) {
        /* Since omni_getallbalancesforid doesn't return the blockHeight, we have to check
         * blockHeight before and after the call to make sure it didn't change.
         */
        int beforeBlockHeight = currentBlockHeight();
        int curBlockHeight;
        SortedMap<Address, BalanceEntry> entries;
        while (true) {
            entries = this.getConsensusForCurrency(currencyID);
            curBlockHeight = currentBlockHeight();
            if (curBlockHeight == beforeBlockHeight) {
                // If blockHeight didn't change, we're done
                break;
            }
            // Otherwise we have to try again
            beforeBlockHeight = curBlockHeight;
        }
        ConsensusSnapshot snap = new ConsensusSnapshot(currencyID, curBlockHeight, "Omni Core", client.getServerURI(), entries);
        return snap;
    }

}
