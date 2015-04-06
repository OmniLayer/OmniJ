package foundation.omni.test

import com.msgilligan.bitcoin.test.BTCTestSupport
import foundation.omni.rpc.ExtendedTransactions
import org.bitcoinj.core.Address
import org.bitcoinj.core.Sha256Hash
import foundation.omni.CurrencyID
import foundation.omni.net.OmniNetworkParameters
import foundation.omni.net.OmniRegTestParams
import foundation.omni.rpc.OmniClientDelegate

import java.security.SecureRandom

/**
 * Test support functions intended to be mixed-in to Spock test specs
 */
trait OmniTestSupport implements BTCTestSupport, OmniClientDelegate, ExtendedTransactions {


    Sha256Hash requestMSC(Address toAddress, BigDecimal requestedMSC) {
        return requestMSC(toAddress, requestedMSC, true)
    }

    Sha256Hash requestMSC(Address toAddress, BigDecimal requestedMSC, Boolean allowIntermediate) {
        final OmniNetworkParameters params = OmniRegTestParams.get()  // Hardcoded for RegTest for now

        // For 1.0 BTC an amount of 100.0 MSC is generated, resulting in a minimal purchase amount of
        // 0.00000100 MSC for 0.00000001 BTC
        def btcForMSC = (requestedMSC / 100.0).setScale(8, BigDecimal.ROUND_UP)
        def actualMSC = btcForMSC * 100.0

        if (!allowIntermediate) {
            assert actualMSC == requestedMSC
        }

        requestBitcoin(toAddress, btcForMSC + stdTxFee)
        def txid = sendBitcoin(toAddress, params.moneyManAddress, btcForMSC)

        if (actualMSC != requestedMSC) {
            def excessiveMSC = actualMSC - requestedMSC

            // TODO: avoid magic numbers for dust calculation
            def dustForExodus = ((((148 + 34) * 3) / 1000) * stdRelayTxFee).setScale(8, BigDecimal.ROUND_UP)
            def dustForReference = ((((148 + 34) * 3) / 1000) * stdRelayTxFee).setScale(8, BigDecimal.ROUND_UP)
            def dustForPayload = ((((148 + 80) * 3) / 1000) * stdRelayTxFee).setScale(8, BigDecimal.ROUND_UP)

            // Simple send transactions have a dust output for the receiver reference, a marker output and an output
            // for the actual payload. MSC and TMSC are forwarded in two transactions, so this amount, as well as the
            // transaction fee, have to be paid twice
            def additionalRequiredBTC = 2 * (dustForExodus + dustForReference + dustForPayload + stdTxFee)
            requestBitcoin(toAddress, additionalRequiredBTC)

            // The excessive amount of MSC is sent to a new address to get rid of it
            def junkAddress = newAddress

            // TODO: can we always get away with not generating a block inbetween?
            def extraTxidMSC = send_MP(toAddress, junkAddress, CurrencyID.MSC, excessiveMSC)
            def extraTxidTMSC = send_MP(toAddress, junkAddress, CurrencyID.TMSC, excessiveMSC)
        }

        // TODO: when using an intermediate receiver, this txid doesn't reflect the whole picture
        return txid
    }

    Address createFundedAddress(BigDecimal requestedBTC, BigDecimal requestedMSC) {
        return createFundedAddress(requestedBTC, requestedMSC, true)
    }

    Address createFundedAddress(BigDecimal requestedBTC, BigDecimal requestedMSC, Boolean confirmTransactions) {
        def fundedAddress = newAddress

        if (requestedMSC > 0.0) {
            def txidMSC = requestMSC(fundedAddress, requestedMSC)
        }

        if (requestedBTC > 0.0) {
            def txidBTC = requestBitcoin(fundedAddress, requestedBTC)
        }

        if (confirmTransactions) {
            generateBlock()
        }

        // TODO: maybe add assertions to check correct funding amounts?

        return fundedAddress
    }
}
