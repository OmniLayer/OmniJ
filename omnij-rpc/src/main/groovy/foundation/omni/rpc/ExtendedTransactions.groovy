package foundation.omni.rpc

import com.msgilligan.bitcoin.BTC
import foundation.omni.CurrencyID
import foundation.omni.Ecosystem
import foundation.omni.PropertyType
import org.bitcoinj.core.Address
import org.bitcoinj.core.Sha256Hash

/**
 * Extended Omni Transactions without direct RPCs
 *
 * Raw transactions are created and sent via sendrawtx_MP
 *
 */
trait ExtendedTransactions implements OmniClientDelegate, RawTxDelegate {

    /**
     * Creates and broadcasts a "send to owners" transaction.
     *
     * @param currencyId  The identifier of the currency
     * @param amount      The number of tokens to distribute
     * @return The transaction hash
     */
    Sha256Hash sendToOwners(Address address, CurrencyID currencyId, Long amount) {
        String rawTxHex = createSendToOwnersHex(currencyId, amount);
        Sha256Hash txid = sendrawtx_MP(address, rawTxHex)
        return txid
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
                                  Byte action) {
        Long satoshisForSale = BTC.btcToSatoshis(amountForSale)
        Long satoshisDesired = BTC.btcToSatoshis(amountDesired)
        Long satoshisFee = BTC.btcToSatoshis(commitmentFee)
        String rawTxHex = createDexSellOfferHex(
                currencyId, satoshisForSale, satoshisDesired, paymentWindow, satoshisFee, action);
        Sha256Hash txid = sendrawtx_MP(address, rawTxHex)
        return txid
    }

    /**
     * Creates an offer on the MetaDex exchange (aka Dex Phase II) (tx 21).
     *
     * <p>Note: Currently assumes divisible currencies</p>
     * <p>Note: Untested</p>
     *
     * @param address        The address
     * @param currencyId     The identifier of the currency for sale
     * @param amountForSale  The amount of currency (BigDecimal coins)
     * @param amountDesired  The amount of desired Currency (divisible token, decimal format)
     * @param paymentWindow  The payment window measured in blocks
     * @param action         The action applied to the offer (1 = new, 2 = update, 3 = cancel)
     * @return The transaction hash
     */
    Sha256Hash createMetaDexSellOffer(Address address, CurrencyID currencyId, BigDecimal amountForSale,
                                  BigDecimal amountDesired, Byte paymentWindow, BigDecimal commitmentFee,
                                  Byte action) {
        Long willetsForSale = BTC.btcToSatoshis(amountForSale)  // Assume divisible property
        Long willetsDesired = BTC.btcToSatoshis(amountDesired)  // Assume divisible property
        String rawTxHex = createMetaDexSellOfferHex(
                currencyId, willetsForSale, willetsDesired, paymentWindow, action);
        Sha256Hash txid = sendrawtx_MP(address, rawTxHex)
        return txid
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
    Sha256Hash createProperty(Address address, Ecosystem ecosystem, PropertyType type, Long amount) {
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
    Sha256Hash createProperty(Address address, Ecosystem ecosystem, PropertyType type, Long amount, String label) {
        String rawTxHex = createPropertyHex(ecosystem, type, 0L, "", "", label, "", "", amount);
        Sha256Hash txid = sendrawtx_MP(address, rawTxHex)
        return txid
    }
}