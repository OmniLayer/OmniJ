package foundation.omni.netapi.omniwallet;

import foundation.omni.CurrencyID;
import foundation.omni.OmniDivisibleValue;
import foundation.omni.OmniIndivisibleValue;
import foundation.omni.OmniValue;
import foundation.omni.PropertyType;
import foundation.omni.json.pojo.OmniPropertyInfo;
import foundation.omni.netapi.ConsensusService;
import foundation.omni.netapi.OmniJBalances;
import foundation.omni.netapi.WalletAddressBalance;
import foundation.omni.netapi.omniwallet.json.AddressVerifyInfo;
import foundation.omni.netapi.omniwallet.json.OmniwalletAddressBalance;
import foundation.omni.netapi.omniwallet.json.OmniwalletAddressPropertyBalance;
import foundation.omni.netapi.omniwallet.json.OmniwalletPropertiesListResponse;
import foundation.omni.netapi.omniwallet.json.OmniwalletPropertyInfo;
import foundation.omni.rpc.AddressBalanceEntry;
import foundation.omni.rpc.BalanceEntry;
import foundation.omni.rpc.ConsensusSnapshot;
import foundation.omni.rpc.SmartPropertyListInfo;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.NetworkParameters;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Base class for Omniwallet Client implementations
 * TODO: We should probably use composition rather than inheritance and have
 * a RawOmniwallet interface and inject an implementation of
 * that into the constructor of an OmniwalletConsensusService type.
 */
public abstract class OmniwalletAbstractClient implements ConsensusService {
    public static final URI omniwalletBase =  URI.create("https://www.omniwallet.org");
    public static final URI omniwalletApiBase =  URI.create("https://api.omniwallet.org");
    public static final URI omniExplorerApiBase =  URI.create("https://api.omniexplorer.info");
    public static final URI stagingBase = URI.create("https://staging.omniwallet.org");
    static public final int BALANCES_FOR_ADDRESSES_MAX_ADDR = 20;
    static public final int CONNECT_TIMEOUT_MILLIS = 15 * 1000; // 15s
    static public final int READ_TIMEOUT_MILLIS = 120 * 1000; // 120s (long enough to load USDT rich list)
    protected final URI baseURI;
    private boolean debug;
    protected boolean strictMode;
    /**
     * netParams, if non-null, is used for validating addresses during deserialization
     */
    protected final NetworkParameters netParams;

    protected final Map<CurrencyID, PropertyType> cachedPropertyTypes = new HashMap<>();

    public OmniwalletAbstractClient(URI baseURI, boolean debug, boolean strictMode) {
        this(baseURI, debug, strictMode, null);
    }

    public OmniwalletAbstractClient(URI baseURI, boolean debug, boolean strictMode, NetworkParameters netParams) {
        this.baseURI = baseURI;
        this.debug = debug;
        this.strictMode = strictMode;
        this.netParams = netParams;
    }

    @Override
    public Integer currentBlockHeight() throws InterruptedException, IOException  {
        Integer height;
        try {
            height = currentBlockHeightAsync().get();
        } catch (ExecutionException ee) {
            throw new IOException(ee);
        }
        return height;
    }

    protected abstract  CompletableFuture<OmniwalletPropertiesListResponse> propertiesList();

    @Override
    public List<SmartPropertyListInfo> listProperties() throws InterruptedException, IOException {
        OmniwalletPropertiesListResponse listResponse;
        try {
            listResponse = propertiesList().get();
        } catch (ExecutionException e) {
            throw new IOException(e);
        }
        List<OmniwalletPropertyInfo> properties = listResponse.getPropertyInfoList();
        List<SmartPropertyListInfo> result = new ArrayList<>();
        properties.forEach(prop -> {
            SmartPropertyListInfo info = mapToSmartPropertyListInfo(prop);
            if (info != null)  {
                result.add(info);
            }
        });
        return result;
    }

    @Override
    public CompletableFuture<List<OmniPropertyInfo>> listSmartProperties() throws InterruptedException, IOException {
        return propertiesList().thenApply(response -> response.getPropertyInfoList().stream()
                    .map(OmniwalletAbstractClient::mapToOmniPropertyInfo)
                    .collect(Collectors.toList()));
    }

    @Override
    public ConsensusSnapshot getConsensusSnapshot(CurrencyID currencyID) throws IOException, InterruptedException {
        /* Since getConsensusForCurrency() doesn't return the blockHeight, we have to check
         * blockHeight before and after the call to make sure it didn't change.
         *
         * Note: Omniwallet blockheight can lag behind Blockchain.info and Omni Core and this
         * loop does not resolve that issue, it only makes sure the reported block height
         * matches the data returned.
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
        ConsensusSnapshot snap = new ConsensusSnapshot(currencyID,
                curBlockHeight,
                "Omniwallet",
                consensusURI(currencyID),
                entries);
        return snap;
    }


    @Override
    public SortedMap<Address, BalanceEntry> getConsensusForCurrency(CurrencyID currencyID) throws InterruptedException, IOException {
        List<AddressVerifyInfo> balances;
        try {
            balances = verifyAddresses(currencyID).get();
        } catch (ExecutionException e) {
            throw new IOException(e);
        }
        // TODO: We need an accurate, efficient way of determining divisible vs indivisible
        final PropertyType propertyType = lookupPropertyType(currencyID);
        return balances.stream()
                .map(bal -> balanceMapper(bal, propertyType))
                .collect(Collectors.toMap(
                        AddressBalanceEntry::getAddress,
                        address -> new BalanceEntry(address.getBalance(),
                                address.getReserved(),
                                address.getFrozen()),
                        (a,b)->a,
                        TreeMap::new)
                );
    }

    @Override
    public OmniJBalances balancesForAddresses(List<Address> addresses) throws InterruptedException, IOException {
        try {
            return balancesForAddressesAsync(addresses).get();
        } catch (ExecutionException e) {
            throw new IOException(e);
        }
    }

    @Override
    public CompletableFuture<OmniJBalances> balancesForAddressesAsync(List<Address> addresses) {
        if (addresses.size() > BALANCES_FOR_ADDRESSES_MAX_ADDR) {
            throw new IllegalArgumentException("Exceeded number of allowable addresses");
        }
        return balanceMapForAddresses(addresses).thenApply(map -> {
            OmniJBalances balances = new OmniJBalances();
            map.forEach((address, owb) ->
                    balances.put(address, balanceEntryMapper(owb)));
            return balances;
        });
    }

    @Override
    public WalletAddressBalance balancesForAddress(Address address) throws InterruptedException, IOException {
        Map<Address, OmniwalletAddressBalance> resultMap;
        try {
            resultMap = balanceMapForAddress(address).get();
        } catch (ExecutionException e) {
            throw new IOException(e);
        }
        OmniwalletAddressBalance result = resultMap.get(address);
        return balanceEntryMapper(result);
    }

    protected abstract CompletableFuture<Map<Address, OmniwalletAddressBalance>> balanceMapForAddress(Address address);
    protected abstract CompletableFuture<Map<Address, OmniwalletAddressBalance>> balanceMapForAddresses(List<Address> addresses);


    protected abstract CompletableFuture<List<AddressVerifyInfo>> verifyAddresses(CurrencyID currencyID);

    protected AddressBalanceEntry balanceMapper(AddressVerifyInfo item, PropertyType propertyType) {

        Address address = item.getAddress();
        String balanceStr = item.getBalance();
        String reservedStr = item.getReservedBalance();

        OmniValue balance = toOmniValue(balanceStr, propertyType);
        OmniValue reserved = toOmniValue(reservedStr, propertyType);
        OmniValue frozen = OmniValue.of(0, propertyType);   // Placeholder
        return new AddressBalanceEntry(address, balance, reserved, frozen);
    }

    protected static OmniValue stringToOmniValue(String valueString) {
        boolean divisible = valueString.contains(".");  // Divisible amounts always contain a decimal point
        if (divisible) {
            return OmniValue.of(new BigDecimal(valueString), PropertyType.DIVISIBLE);
        } else {
            return OmniValue.of(Long.parseLong(valueString), PropertyType.INDIVISIBLE);
        }
    }

    protected SmartPropertyListInfo mapToSmartPropertyListInfo(OmniwalletPropertyInfo property) {
        return new SmartPropertyListInfo(property.getPropertyid(),
                    property.getName(),
                    property.getCategory(),
                    property.getSubcategory(),
                    property.getData(),
                    property.getUrl(),
                    property.isDivisible());
    }

    static OmniPropertyInfo mapToOmniPropertyInfo(OmniwalletPropertyInfo property) {
        return new OmniPropertyInfo(property.getPropertyid(),
                property.getName(),
                property.getCategory(),
                property.getSubcategory(),
                property.getData(),
                property.getUrl(),
                property.isDivisible(),
                property.getIssuer(),
                property.getCreationTxId(),
                property.isFixedIssuance(),
                property.isManagedIssuance(),
                property.isFreezingEnabled(),
                property.getTotalTokens());
    }

    protected PropertyType lookupPropertyType(CurrencyID propertyID) throws IOException, InterruptedException {
        if (!cachedPropertyTypes.containsKey(propertyID)) {
            // If we don't have the info, fetch info for all properties from server and update
            List<SmartPropertyListInfo> infos = listProperties();
            infos.forEach(info -> {
                cachedPropertyTypes.put(info.getPropertyid(), divisibleToPropertyType(info.getDivisible()));
            });
        }
        return cachedPropertyTypes.get(propertyID);
    }

    protected WalletAddressBalance balanceEntryMapper(OmniwalletAddressBalance owb) {
        WalletAddressBalance wab = new WalletAddressBalance();

        for (OmniwalletAddressPropertyBalance pb : owb.getBalance()) {
            CurrencyID id = pb.getId();
            PropertyType type =  pb.isDivisible() ? PropertyType.DIVISIBLE : PropertyType.INDIVISIBLE;
            OmniValue value = pb.getValue();
            OmniValue zero = OmniValue.ofWilletts(0, type);
            if (!pb.isError()) {
                wab.put(id, new BalanceEntry(value, zero, zero));
            }
        }
        return wab;
    }


    protected URI consensusURI(CurrencyID currencyID) {
        return baseURI.resolve("/v1/mastercoin_verify/addresses?currency_id=" + currencyID.getValue());
    }

    protected OmniValue toOmniValue(String input, PropertyType type) {
        if (strictMode) {
            /* Validate string */
        }
        return type.isDivisible() ?
                toOmniDivisibleValue(input) : toOmniIndivisibleValue(input);
    }

    protected OmniDivisibleValue toOmniDivisibleValue(String inputStr) {
        return toOmniDivisibleValue(new BigDecimal(inputStr));
    }

    protected OmniDivisibleValue toOmniDivisibleValue(BigDecimal input) {
        if (input.compareTo(OmniDivisibleValue.MAX_VALUE) > 0) {
            if (strictMode) {
                throw new ArithmeticException("too big");
            }
            return OmniDivisibleValue.MAX;
        } else if (input.compareTo(OmniDivisibleValue.MIN_VALUE) < 0) {
            if (strictMode) {
                throw new ArithmeticException("too small");
            }
            return OmniDivisibleValue.MIN;
        } else {
            return OmniDivisibleValue.of(input);
        }
    }

    protected OmniIndivisibleValue toOmniIndivisibleValue(String input) {
        return toOmniIndivisibleValue(new BigInteger(input));
    }

    protected OmniIndivisibleValue toOmniIndivisibleValue(BigInteger input) {
        if (input.compareTo(OmniIndivisibleValue.MAX_BIGINT) > 0) {
            if (strictMode) {
                throw new ArithmeticException("too big");
            }
            return OmniIndivisibleValue.MAX;
        } else if (input.compareTo(OmniIndivisibleValue.MIN_BIGINT) < 0) {
            if (strictMode) {
                throw new ArithmeticException("too small");
            }
            return OmniIndivisibleValue.MIN;
        } else {
            return OmniIndivisibleValue.of(input);
        }
    }

    protected PropertyType divisibleToPropertyType(boolean divisible) {
        PropertyType type =  divisible ? PropertyType.DIVISIBLE : PropertyType.INDIVISIBLE;
        return type;
    }

}
