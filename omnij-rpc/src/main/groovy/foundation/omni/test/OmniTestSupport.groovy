package foundation.omni.test

import org.consensusj.bitcoin.jsonrpc.groovy.test.BTCTestSupport
import foundation.omni.Ecosystem
import foundation.omni.OmniDivisibleValue
import foundation.omni.OmniValue
import foundation.omni.PropertyType
import org.bitcoinj.core.Address
import org.bitcoinj.core.Coin
import org.bitcoinj.core.Sha256Hash
import foundation.omni.CurrencyID
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.math.RoundingMode

/**
 * Test support functions intended to be mixed-in to Spock test specs
 */
trait OmniTestSupport implements BTCTestSupport, OmniTestClientDelegate {
    private static final Logger log = LoggerFactory.getLogger(OmniTestSupport.class);

    /**
     * Delay long enough to avoid Duplicate block errors when resubmitting blocks in
     * RegTest mode after invalidating a block. See OmniJ Issue #185.
     */
    void delayAfterInvalidate() {
        sleep(2_000)
    }

    /**
     * Longer delay to avoid Duplicate block errors when resubmitting blocks in
     * RegTest mode after invalidating a block. See OmniJ Issue #185.
     */
    void longerDelayAfterInvalidate() {
        sleep(120_000)
    }

    Sha256Hash requestOmni(Address toAddress, OmniDivisibleValue requestedOmni) {
        return requestOmni(toAddress, requestedOmni, true)
    }

    /**
     * Request OMNI/Omni for testing
     *
     * TODO: Convert parameters and math to use OmniValue/Long
     * @param toAddress Address requesting OMNI/Omni
     * @param requestedOmni Amount requested
     * @param allowIntermediate allow intermediate receiver
     * @return txid
     */
    Sha256Hash requestOmni(Address toAddress, OmniDivisibleValue requestedOmni, Boolean allowIntermediate) {
        // For 1.0 BTC an amount of 100.0 OMNI is generated, resulting in a minimal purchase amount of
        // 0.00000100 OMNI for 0.00000001 BTC
        Coin btcForOmni = (requestedOmni.willetts / 100).setScale(0, RoundingMode.UP).satoshi
        OmniDivisibleValue actualOmni = OmniDivisibleValue.ofWilletts(btcForOmni.value * 100)

        if (!allowIntermediate) {
            assert actualOmni == requestedOmni
        }

        requestBitcoin(toAddress, btcForOmni + stdTxFee)
        def txid = sendBitcoin(toAddress, omniNetParams.moneyManAddress, btcForOmni)

        if (actualOmni.willetts != requestedOmni.willetts) {
            def excessiveOmni = actualOmni - requestedOmni

            // TODO: avoid magic numbers for dust calculation
            // TODO: convert calculations to Coin type or integer
            BigDecimal dustForExodus = ((((148 + 34) * 3) / 1000) * stdRelayTxFee.value).setScale(8, RoundingMode.UP)
            BigDecimal dustForReference = ((((148 + 34) * 3) / 1000) * stdRelayTxFee.value).setScale(8, RoundingMode.UP)
            BigDecimal dustForPayload = ((((148 + 80) * 3) / 1000) * stdRelayTxFee.value).setScale(8, RoundingMode.UP)

            // Simple send transactions have a dust output for the receiver reference, a marker output and an output
            // for the actual payload. OMNI and TOMNI are forwarded in two transactions, so this amount, as well as the
            // transaction fee, have to be paid twice
            BigDecimal additionalRequiredDecimal = (dustForExodus + dustForReference + dustForPayload + stdTxFee.value) * 2
            Coin additionalRequired = additionalRequiredDecimal.satoshi
            log.debug "requestOmni: requesting ${additionalRequired} additional bitcoin"
            requestBitcoin(toAddress, additionalRequired)

            // The excessive amount of OMNI is sent to a new address to get rid of it
            def junkAddress = newAddress

            // TODO: can we always get away with not generating a block inbetween?
            def extraTxidOmni = omniSend(toAddress, junkAddress, CurrencyID.OMNI, excessiveOmni)
            def extraTxidTOmni = omniSend(toAddress, junkAddress, CurrencyID.TOMNI, excessiveOmni)
        }

        // TODO: when using an intermediate receiver, this txid doesn't reflect the whole picture
        return txid
    }

    Address createFundedAddress(Coin requestedBTC, OmniValue requestedOmni) {
        return createFundedAddress(requestedBTC, requestedOmni, true)
    }

    Address createFundedAddress(Coin requestedBTC, OmniValue requestedOmni, Boolean confirmTransactions) {
        log.debug "createFundedAddress: requestedBTC: {}, requestedOmni: {}, confirm: {}", requestedBTC.toFriendlyString(), requestedOmni, confirmTransactions
        def fundedAddress = newAddress

        if (requestedOmni.willetts > 0) {
            def txidOmni = requestOmni(fundedAddress, (OmniDivisibleValue) requestedOmni)
        }

        if (requestedBTC.value > 0) {
            def txidBTC = requestBitcoin(fundedAddress, requestedBTC)
        }

        if (confirmTransactions) {
            generateBlocks(1)
        }

        // TODO: maybe add assertions to check correct funding amounts?
        Boolean check = true
        if (check && confirmTransactions) {
            assert getBitcoinBalance(fundedAddress).value >= requestedBTC.value
            assert omniGetBalance(fundedAddress, CurrencyID.OMNI).balance >= requestedOmni.numberValue()
            log.debug "balances verified, fundedAddress has {} and {} Omni", requestedBTC.toFriendlyString(), requestedOmni
        }

        return fundedAddress
    }

    CurrencyID fundNewProperty(Address address, OmniValue amount, Ecosystem ecosystem) {
        def txidCreation = omniSendIssuanceFixed(address,
                ecosystem,
                amount.getPropertyType(),
                CurrencyID.of(0),   // previousId (0 means new token)
                "",
                "",
                "name",
                "",
                "",
                amount);

        generateBlocks(1)
        def txCreation = omniGetTransaction(txidCreation)
        assert txCreation.valid
        assert txCreation.confirmations == 1
        return txCreation.propertyId
    }

    CurrencyID fundManagedProperty(Address address, PropertyType type, Ecosystem ecosystem) {
        def txidCreation = omniSendIssuanceManaged(address,
                ecosystem,
                type,
                CurrencyID.of(0),   // previousId (0 means new token)
                "",
                "",
                "MSP",
                "",
                "")
        generateBlocks(1)
        def txCreation = omniGetTransaction(txidCreation)
        assert txCreation.valid
        assert txCreation.confirmations == 1
        return new CurrencyID(txCreation.propertyId as long)
    }
}
