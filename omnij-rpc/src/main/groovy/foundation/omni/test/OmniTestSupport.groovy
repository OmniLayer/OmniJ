package foundation.omni.test

import com.msgilligan.bitcoinj.test.BTCTestSupport
import foundation.omni.Ecosystem
import foundation.omni.OmniDivisibleValue
import foundation.omni.OmniValue
import foundation.omni.PropertyType
import foundation.omni.rpc.RawTxDelegate
import org.bitcoinj.core.Address
import org.bitcoinj.core.Coin
import org.bitcoinj.core.Sha256Hash
import foundation.omni.CurrencyID

/**
 * Test support functions intended to be mixed-in to Spock test specs
 */
trait OmniTestSupport implements BTCTestSupport, OmniTestClientDelegate, RawTxDelegate {

    Sha256Hash requestMSC(Address toAddress, OmniDivisibleValue requestedOmni) {
        return requestMSC(toAddress, requestedOmni, true)
    }

    /**
     * Request OMNI/Omni for testing
     *
     * TODO: Convert parameters and math to use OmniValue/Long
     * @param toAddress Address requesting OMNI/Omni
     * @param requestedMSC Amount requested
     * @param allowIntermediate allow intermediate receiver
     * @return txid
     */
    Sha256Hash requestMSC(Address toAddress, OmniDivisibleValue requestedMSC, Boolean allowIntermediate) {
        // For 1.0 BTC an amount of 100.0 OMNI is generated, resulting in a minimal purchase amount of
        // 0.00000100 OMNI for 0.00000001 BTC
        Coin btcForMSC = (requestedMSC.willetts / 100).setScale(0, BigDecimal.ROUND_UP).satoshi
        OmniDivisibleValue actualMSC = OmniDivisibleValue.ofWilletts(btcForMSC.value * 100)

        if (!allowIntermediate) {
            assert actualMSC == requestedMSC
        }

        requestBitcoin(toAddress, btcForMSC + stdTxFee)
        def txid = sendBitcoin(toAddress, omniNetParams.moneyManAddress, btcForMSC)

        if (actualMSC.willetts != requestedMSC.willetts) {
            def excessiveMSC = actualMSC - requestedMSC

            // TODO: avoid magic numbers for dust calculation
            // TODO: convert calculations to Coin type or integer
            BigDecimal dustForExodus = ((((148 + 34) * 3) / 1000) * stdRelayTxFee.value).setScale(8, BigDecimal.ROUND_UP)
            BigDecimal dustForReference = ((((148 + 34) * 3) / 1000) * stdRelayTxFee.value).setScale(8, BigDecimal.ROUND_UP)
            BigDecimal dustForPayload = ((((148 + 80) * 3) / 1000) * stdRelayTxFee.value).setScale(8, BigDecimal.ROUND_UP)

            // Simple send transactions have a dust output for the receiver reference, a marker output and an output
            // for the actual payload. OMNI and TOMNI are forwarded in two transactions, so this amount, as well as the
            // transaction fee, have to be paid twice
            BigDecimal additionalRequiredDecimal = (dustForExodus + dustForReference + dustForPayload + stdTxFee.value) * 2
            Coin additionalRequired = additionalRequiredDecimal.satoshi
            log.debug "requestMSC: requesting ${additionalRequired} additional bitcoin"
            requestBitcoin(toAddress, additionalRequired)

            // The excessive amount of OMNI is sent to a new address to get rid of it
            def junkAddress = newAddress

            // TODO: can we always get away with not generating a block inbetween?
            def extraTxidMSC = omniSend(toAddress, junkAddress, CurrencyID.OMNI, excessiveMSC)
            def extraTxidTMSC = omniSend(toAddress, junkAddress, CurrencyID.TOMNI, excessiveMSC)
        }

        // TODO: when using an intermediate receiver, this txid doesn't reflect the whole picture
        return txid
    }

    @Deprecated
    Address createFundedAddress(BigDecimal requestedBTC, BigDecimal requestedMSC) {
        return createFundedAddress(btcToCoin(requestedBTC), OmniDivisibleValue.of(requestedMSC), true)
    }

    @Deprecated
    Address createFundedAddress(BigDecimal requestedBTC, BigDecimal requestedMSC, Boolean confirmTransactions) {
        return createFundedAddress(btcToCoin(requestedBTC), OmniDivisibleValue.of(requestedMSC), confirmTransactions)
    }

    Address createFundedAddress(Coin requestedBTC, OmniValue requestedMSC) {
        return createFundedAddress(requestedBTC, requestedMSC, true)
    }

    Address createFundedAddress(Coin requestedBTC, OmniValue requestedMSC, Boolean confirmTransactions) {
        log.debug "createFundedAddress: requestedBTC: {}, requestedMSC: {}, confirm: {}", requestedBTC.toFriendlyString(), requestedMSC, confirmTransactions
        def fundedAddress = newAddress

        if (requestedMSC.willetts > 0) {
            def txidMSC = requestMSC(fundedAddress, (OmniDivisibleValue) requestedMSC)
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
            assert omniGetBalance(fundedAddress, CurrencyID.OMNI).balance >= requestedMSC.numberValue()
            log.debug "balances verified, fundedAddress has {} and {} Omni", requestedBTC.toFriendlyString(), requestedMSC
        }

        return fundedAddress
    }

    @Deprecated
    CurrencyID fundNewProperty(Address address, BigDecimal amountBD, PropertyType type, Ecosystem ecosystem) {
        return fundNewProperty(address, OmniValue.of(amountBD, type), ecosystem)
    }

    CurrencyID fundNewProperty(Address address, OmniValue amount, Ecosystem ecosystem) {
        def txidCreation = omniSendIssuanceFixed(address,
                ecosystem,
                amount.getPropertyType(),
                new CurrencyID(0),  // previousId
                "",                 // category
                "",                 // subCategory
                "name",             // name
                "",                 // url
                "",                 // data
                amount);

        generateBlocks(1)
        def txCreation = omniGetTransaction(txidCreation)
        assert txCreation.valid == true
        assert txCreation.confirmations == 1
        return new CurrencyID(txCreation.propertyid as long)
    }

    CurrencyID fundManagedProperty(Address address, PropertyType type, Ecosystem ecosystem) {
        def txidCreation = omniSendIssuanceManaged(address,
                ecosystem,
                type,
                new CurrencyID(0),  // previousId
                "",                 // category
                "",                 // subCategory
                "MSP",
                "",                 // url
                "")                 // data
        generateBlocks(1)
        def txCreation = omniGetTransaction(txidCreation)
        assert txCreation.valid == true
        assert txCreation.confirmations == 1
        return new CurrencyID(txCreation.propertyid as long)
    }

}
