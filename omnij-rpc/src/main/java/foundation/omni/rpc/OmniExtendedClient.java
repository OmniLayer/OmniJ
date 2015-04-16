package foundation.omni.rpc;

import com.msgilligan.bitcoin.BTC;
import com.msgilligan.bitcoin.rpc.JsonRPCException;
import com.msgilligan.bitcoin.rpc.RPCConfig;
import foundation.omni.CurrencyID;
import foundation.omni.Ecosystem;
import foundation.omni.PropertyType;
import foundation.omni.tx.RawTxBuilder;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Sha256Hash;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;

/**
 * <p>OmniClient that adds "extended" methods for Omni transactions that lack
 * RPCs in Omni Core 0.9.0</p>
 *
 * <p>Raw transactions are created and sent via sendrawtx_MP
 *
 */
public class OmniExtendedClient extends OmniClient {
    RawTxBuilder builder = new RawTxBuilder();

    public OmniExtendedClient(RPCConfig config) throws IOException {
        super(config);
    }

    public OmniExtendedClient(URI server, String rpcuser, String rpcpassword) throws IOException {
        super(server, rpcuser, rpcpassword);
    }

    /**
     * Creates and broadcasts a "send to owners" transaction.
     *
     * @param currencyId  The identifier of the currency
     * @param amount      The number of tokens to distribute
     * @return The transaction hash
     */
    Sha256Hash sendToOwners(Address address, CurrencyID currencyId, Long amount) throws JsonRPCException, IOException {
        String rawTxHex = builder.createSendToOwnersHex(currencyId, amount);
        Sha256Hash txid = sendrawtx_MP(address, rawTxHex);
        return txid;
    }

    /**
     * Creates an offer on the traditional distributed exchange.
     *
     * @param address        The address
     * @param currencyId     The identifier of the currency for sale
     * @param amountForSale  The amount of currency (BigDecimal coins)
     * @param amountDesired  The amount of desired Bitcoin (in BTC)
     * @param paymentWindow  The payment window measured in blocks
     * @param commitmentFee  The minimum transaction fee required to be paid as commitment when accepting this offer
     * @param action         The action applied to the offer (1 = new, 2 = update, 3 = cancel)
     * @return The transaction hash
     */
    Sha256Hash createDexSellOffer(Address address, CurrencyID currencyId, BigDecimal amountForSale,
                                  BigDecimal amountDesired, Byte paymentWindow, BigDecimal commitmentFee,
                                  Byte action) throws JsonRPCException, IOException {
        Long satoshisForSale = BTC.btcToSatoshis(amountForSale).longValue();
        Long satoshisDesired = BTC.btcToSatoshis(amountDesired).longValue();
        Long satoshisFee = BTC.btcToSatoshis(commitmentFee).longValue();
        String rawTxHex = builder.createDexSellOfferHex(
                currencyId, satoshisForSale, satoshisDesired, paymentWindow, satoshisFee, action);
        Sha256Hash txid = sendrawtx_MP(address, rawTxHex);
        return txid;
    }

    /**
     * Creates an offer on the MetaDex exchange (aka Dex Phase II) (tx 21).
     *
     * <p>Note: Currently assumes divisible currencies</p>
     * <p>Note: Untested</p>
     *
     * @param address           The address
     * @param currencyForSale   The identifier of the currency for sale
     * @param amountForSale     The amount of currency (BigDecimal coins)
     * @param currencyDesired   The identifier of the currency for sale
     * @param amountDesired     The amount of desired Currency (divisible token, decimal format)
     * @param action            The action applied to the offer (1 = new, 2 = update, 3 = cancel)
     * @return The transaction hash
     */
    Sha256Hash createMetaDexSellOffer(Address address, CurrencyID currencyForSale, BigDecimal amountForSale,
                                      CurrencyID currencyDesired, BigDecimal amountDesired,
                                      Byte action) throws JsonRPCException, IOException {
        Long willetsForSale = BTC.btcToSatoshis(amountForSale).longValue();  // Assume divisible property
        Long willetsDesired = BTC.btcToSatoshis(amountDesired).longValue();  // Assume divisible property
        String rawTxHex = builder.createMetaDexSellOfferHex(
                currencyForSale, willetsForSale, currencyDesired, willetsDesired, action);
        Sha256Hash txid = sendrawtx_MP(address, rawTxHex);
        return txid;
    }

    /**
     * Creates a smart property with fixed supply.
     *
     * @param address    The issuance address
     * @param ecosystem  The ecosystem to create the property in
     * @param type       The property type
     * @param amount     The number of units to create
     * @return The transaction hash
     */
    Sha256Hash createProperty(Address address, Ecosystem ecosystem, PropertyType type, Long amount) throws JsonRPCException, IOException {
        return createProperty(address, ecosystem, type, amount, "SP");
    }

    /**
     * Creates a smart property with fixed supply.
     *
     * @param address    The issuance address
     * @param ecosystem  The ecosystem to create the property in
     * @param type       The property type
     * @param amount     The number of units to create
     * @param label      The label or title of the property
     * @return The transaction hash
     */
    Sha256Hash createProperty(Address address, Ecosystem ecosystem, PropertyType type, Long amount, String label) throws JsonRPCException, IOException {
        String rawTxHex = builder.createPropertyHex(ecosystem, type, 0L, "", "", label, "", "", amount);
        Sha256Hash txid = sendrawtx_MP(address, rawTxHex);
        return txid;
    }
}
