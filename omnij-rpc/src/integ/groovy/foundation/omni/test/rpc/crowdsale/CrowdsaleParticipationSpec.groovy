package foundation.omni.test.rpc.crowdsale

import foundation.omni.BaseRegTestSpec
import foundation.omni.CurrencyID
import spock.lang.Unroll

class CrowdsaleParticipationSpec extends BaseRegTestSpec {

    final static BigDecimal startBTC = 0.01
    final static BigDecimal startMSC = 0.00

    @Unroll
    def "Investing #amountToInvest MSC in a crowdsale with 0.00000001 MDiv per unit yields #expectedBalance MDiv"() {
        /*
            {
                "version" : 0,
                "type" : 51,
                "name" : "MDiv",
                "category" : "",
                "subcategory" : "",
                "data" : "",
                "url" : "",
                "divisible" : true,
                "data" : "",
                "propertyiddesired" : 1,
                "tokensperunit" : "0.00000001",
                "deadline" : 7731414000,
                "earlybonus" : 0,
                "percenttoissuer" : 0
            }
        */
        def rawTx = "000000330100020000000000004d44697600000000000001000000000000000100000001ccd403f00000"
        def issuerAddress = createFundedAddress(startBTC, startMSC, false)
        def investorAddress = createFundedAddress(startBTC, amountToInvest, false)
        def currencyMSC = CurrencyID.MSC

        when: "creating a new crowdsale with 0.00000001 MDiv per unit invested"
        def crowdsaleTxid = omniSendRawTx(issuerAddress, rawTx)
        generateBlock()

        then: "the crowdsale is active"
        def crowdsaleTx = omniGetTransaction(crowdsaleTxid)
        crowdsaleTx.confirmations == 1
        crowdsaleTx.valid == true
        def propertyId = new CurrencyID(crowdsaleTx.propertyid as Long)
        omniGetCrowdsale(propertyId).active == true

        when: "participant invests #amountToInvest MSC"
        def sendTxid = omniSend(investorAddress, issuerAddress, currencyMSC, amountToInvest)
        generateBlock()

        then: "the investor should get #expectedBalance MDiv"
        omniGetTransaction(sendTxid).valid == true
        omniGetBalance(investorAddress, propertyId).balance == expectedBalance

        and: "the issuer receives the invested amount"
        omniGetBalance(issuerAddress, currencyMSC).balance == startMSC + amountToInvest

        where:
        amountToInvest | expectedBalance
        0.00000001     | 0.0
        1.0            | 0.00000001
        2.99999999     | 0.00000002
    }

    @Unroll
    def "Investing #amountToInvest MSC in a crowdsale with 92233720368.54775807 MDiv per unit yields #expectedBalance MDiv"() {
        /*
            {
                "version" : 0,
                "type" : 51,
                "name" : "MDiv",
                "category" : "",
                "subcategory" : "",
                "data" : "",
                "url" : "",
                "divisible" : true,
                "data" : "",
                "propertyiddesired" : 1,
                "tokensperunit" : "92233720368.54775807",
                "deadline" : 7731414000,
                "earlybonus" : 0,
                "percenttoissuer" : 0
            }
        */
        def rawTx = "000000330100020000000000004d446976000000000000017fffffffffffffff00000001ccd403f00000"
        def issuerAddress = createFundedAddress(startBTC, startMSC, false)
        def investorAddress = createFundedAddress(startBTC, amountToInvest, false)
        def currencyMSC = CurrencyID.MSC

        when: "creating a new crowdsale with 0.00000001 MDiv per unit invested"
        def crowdsaleTxid = omniSendRawTx(issuerAddress, rawTx)
        generateBlock()

        then: "the crowdsale is active"
        def crowdsaleTx = omniGetTransaction(crowdsaleTxid)
        crowdsaleTx.confirmations == 1
        crowdsaleTx.valid == true
        def propertyId = new CurrencyID(crowdsaleTx.propertyid as Long)
        omniGetCrowdsale(propertyId).active == true

        when: "participant invests #amountToInvest MSC"
        def sendTxid = omniSend(investorAddress, issuerAddress, currencyMSC, amountToInvest)
        generateBlock()

        then: "the investor should get #expectedBalance MDiv"
        omniGetTransaction(sendTxid).valid == true
        omniGetBalance(investorAddress, propertyId).balance == expectedBalance

        and: "the issuer receives the invested amount"
        omniGetBalance(issuerAddress, currencyMSC).balance == startMSC + amountToInvest

        where:
        amountToInvest | expectedBalance
        0.01           | 922337203.68547758
        1.0            | 92233720368.54775807
        2.0            | 92233720368.54775807 // Cap of 2^63-1 issued units reached!
    }

    @Unroll
    def "Investing #amountToInvest TMSC in a crowdsale with 3400 TIndiv per unit yields #expectedBalance TIndiv"() {
        /*
            {
                "version" : 0,
                "type" : 51,
                "name" : "TIndiv",
                "category" : "",
                "subcategory" : "",
                "data" : "",
                "url" : "",
                "divisible" : false,
                "data" : "",
                "propertyiddesired" : 2,
                "tokensperunit" : "3400",
                "deadline" : 7731414000,
                "earlybonus" : 0,
                "percenttoissuer" : 0
            }
        */
        def rawTx = "0000003301000100000000000054496e646976000000000000020000000000000d4800000001ccd403f00000"
        def issuerAddress = createFundedAddress(startBTC, startMSC, false)
        def investorAddress = createFundedAddress(startBTC, amountToInvest, false)
        def currencyMSC = CurrencyID.TMSC

        when: "creating a new crowdsale with 3400 TIndiv per unit invested"
        def crowdsaleTxid = omniSendRawTx(issuerAddress, rawTx)
        generateBlock()

        then: "the crowdsale is active"
        def crowdsaleTx = omniGetTransaction(crowdsaleTxid)
        crowdsaleTx.confirmations == 1
        crowdsaleTx.valid == true
        def propertyId = new CurrencyID(crowdsaleTx.propertyid as Long)
        omniGetCrowdsale(propertyId).active == true

        when: "participant invests #amountToInvest TMSC"
        def sendTxid = omniSend(investorAddress, issuerAddress, currencyMSC, amountToInvest)
        generateBlock()

        then: "the investor should get #expectedBalance TIndiv"
        omniGetTransaction(sendTxid).valid == true
        omniGetBalance(investorAddress, propertyId).balance == expectedBalance as BigDecimal

        and: "the issuer receives the invested amount"
        omniGetBalance(issuerAddress, currencyMSC).balance == startMSC + amountToInvest

        where:
        amountToInvest | expectedBalance
        0.0025         | 8
        0.005          | 17
    }

}
