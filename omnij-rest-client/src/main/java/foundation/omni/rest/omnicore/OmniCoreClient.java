package foundation.omni.rest.omnicore;

import com.msgilligan.bitcoinj.rpc.JsonRPCException;
import foundation.omni.CurrencyID;
import foundation.omni.consensus.OmniCoreConsensusFetcher;
import foundation.omni.rest.ConsensusService;
import foundation.omni.rest.OmniJBalances;
import foundation.omni.rest.WalletAddressBalance;
import foundation.omni.rest.omniwallet.BalanceInfo;
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

/**
 *
 */
public class OmniCoreClient extends OmniCoreConsensusFetcher implements ConsensusService {
    public OmniCoreClient(NetworkParameters netParms, URI server, String rpcuser, String rpcpassword) {
        super(netParms, server, rpcuser, rpcpassword);
    }

    @Override
    public OmniJBalances balancesForAddresses(List<Address> addresses) {
        OmniJBalances balances = new OmniJBalances();
        addresses.stream().forEach(address -> {
            List<BalanceInfo> result = balancesForAddress(address);
            WalletAddressBalance bal = new WalletAddressBalance();
            result.stream().forEach(bi -> {
                bal.put(bi.id, bi.value);
            });
            balances.put(address, bal);
        });
        return balances;
    }

    private List<BalanceInfo> balancesForAddress(Address address) {
        SortedMap<CurrencyID, BalanceEntry> entries;
        try {
            entries = client.omniGetAllBalancesForAddress(address);
        } catch (JsonRPCException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        List<BalanceInfo> list = new ArrayList<>();
        entries.forEach((id, be) -> {
            BalanceInfo info = new BalanceInfo();
            info.id = id;
            info.value = be.getBalance().plus(be.getReserved());
            list.add(info);
        });
        return list;
    }
}

