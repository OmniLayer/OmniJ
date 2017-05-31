package foundation.omni.rest.omnicore;

import com.msgilligan.bitcoinj.json.pojo.AddressGroupingItem;
import com.msgilligan.bitcoinj.rpc.JsonRPCException;
import foundation.omni.CurrencyID;
import foundation.omni.consensus.OmniCoreConsensusFetcher;
import foundation.omni.rest.ConsensusService;
import foundation.omni.rest.OmniJBalances;
import foundation.omni.rest.WalletAddressBalance;
import foundation.omni.rpc.BalanceEntry;
import foundation.omni.rpc.OmniClient;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.NetworkParameters;

import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Omni Core "REST" client that implements same interfaces as Omniwallet REST client
 */
public class OmniCoreClient extends OmniCoreConsensusFetcher implements ConsensusService {
    public OmniCoreClient(NetworkParameters netParms, URI server, String rpcuser, String rpcpassword) {
        super(netParms, server, rpcuser, rpcpassword);
    }

    @Override
    public OmniJBalances balancesForAddresses(List<Address> addresses) {
        OmniJBalances balances = new OmniJBalances();
        addresses.forEach(address -> {
            WalletAddressBalance bal = balancesForAddress(address);
            balances.put(address, bal);
        });
        return balances;
    }

    /**
     * Get all addresses in a wallet
     * Requires wallet support to be active
     * @return A list of addresses
     */
    public List<Address> getWalletAddresses() {
        // Get a list of every address
        List<Address> addresses = new ArrayList<>();
        try {
            List<List<AddressGroupingItem>> addressItems = client.listAddressGroupings();
            addressItems.forEach(group -> group.forEach( item -> {
                addresses.add(item.getAddress());
            }));
        } catch (JsonRPCException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return addresses;
    }

    @Override
    public WalletAddressBalance balancesForAddress(Address address) {
        SortedMap<CurrencyID, BalanceEntry> entries = new TreeMap<>();
        try {
            entries = client.omniGetAllBalancesForAddress(address);
        } catch (JsonRPCException e) {
            if (!e.getMessage().equals("Address not found")) {
                throw new RuntimeException(e);
            }
            // otherwise return empty entries map
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        WalletAddressBalance result = new WalletAddressBalance();
        entries.forEach((id, be) -> {
            result.put(id, be.getBalance().plus(be.getReserved()));
        });
        return result;
    }
}

