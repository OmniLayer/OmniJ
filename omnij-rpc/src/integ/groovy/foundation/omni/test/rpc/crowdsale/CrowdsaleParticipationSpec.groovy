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
        def investorAddress = createFundedAddress(startBTC, amountToInvest.bigDecimalValue(), false)
        def currencyMSC = CurrencyID.MSC

        when: "creating a new crowdsale with 0.00000001 MDiv per unit invested"
        def crowdsaleTxid = omniSendRawTx(issuerAddress, rawTx)
        generateBlock()

        then: "the crowdsale is valid"
        def crowdsaleTx = omniGetTransaction(crowdsaleTxid)
        crowdsaleTx.confirmations == 1
        crowdsaleTx.valid

        and: "the crowdsale is active"
        def propertyId = new CurrencyID(crowdsaleTx.propertyid as Long)
        omniGetCrowdsale(propertyId).active

        when: "participant invests #amountToInvest MSC"
        def sendTxid = omniSend(investorAddress, issuerAddress, currencyMSC, amountToInvest.bigDecimalValue())
        generateBlock()

        then: "the investor should get #expectedBalance MDiv"
        omniGetBalance(investorAddress, propertyId).balance == expectedBalance.bigDecimalValue()

        and: "the issuer receives the invested amount"
        omniGetBalance(issuerAddress, currencyMSC).balance == startMSC + amountToInvest.bigDecimalValue()

        when: "retrieving the transaction"
        def sendTx = omniGetTransaction(sendTxid)

        then: "it's a valid crowdsale participation transaction"
        sendTx.txid == sendTxid.toString()
        sendTx.sendingaddress == investorAddress.toString()
        sendTx.referenceaddress == issuerAddress.toString()
        sendTx.version == 0
        sendTx.type_int == 0
        sendTx.type == "Crowdsale Purchase"
        sendTx.propertyid == currencyMSC.getValue()
        sendTx.divisible
        sendTx.amount as BigDecimal == amountToInvest.bigDecimalValue()
        sendTx.purchasedpropertyid == propertyId.getValue()
        sendTx.purchasedpropertyname == "MDiv"
        sendTx.purchasedpropertydivisible
        sendTx.purchasedtokens as BigDecimal == expectedBalance.bigDecimalValue()
        sendTx.issuertokens as BigDecimal == 0.0 // no bonus applied
        sendTx.valid

        when: "retrieving information about the crowdsale"
        def crowdsale = omniGetCrowdsale(propertyId)

        then: "the information matches the initial creation"
        crowdsale.propertyid == propertyId.getValue()
        crowdsale.name == "MDiv"
        crowdsale.active
        crowdsale.issuer == issuerAddress.toString()
        crowdsale.propertyiddesired == currencyMSC.getValue()
        crowdsale.tokensperunit as BigDecimal == 0.00000001.divisible.bigDecimalValue()
        crowdsale.earlybonus == 0
        crowdsale.percenttoissuer == 0
        crowdsale.deadline == 7731414000
        crowdsale.tokensissued as BigDecimal == expectedBalance.bigDecimalValue()
        crowdsale.addedissuertokens as BigDecimal == 0.divisible.bigDecimalValue() // no bonus applied


        where:
        amountToInvest       | expectedBalance
        0.00000001.divisible | 0.0.divisible
        1.0.divisible        | 0.00000001.divisible
        2.99999999.divisible | 0.00000002.divisible
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
        def investorAddress = createFundedAddress(startBTC, amountToInvest.bigDecimalValue(), false)
        def currencyMSC = CurrencyID.MSC

        when: "creating a new crowdsale with 0.00000001 MDiv per unit invested"
        def crowdsaleTxid = omniSendRawTx(issuerAddress, rawTx)
        generateBlock()

        then: "the crowdsale is valid"
        def crowdsaleTx = omniGetTransaction(crowdsaleTxid)
        crowdsaleTx.confirmations == 1
        crowdsaleTx.valid

        and: "the crowdsale is active"
        def propertyId = new CurrencyID(crowdsaleTx.propertyid as Long)
        omniGetCrowdsale(propertyId).active

        when: "participant invests #amountToInvest MSC"
        def sendTxid = omniSend(investorAddress, issuerAddress, currencyMSC, amountToInvest.bigDecimalValue())
        generateBlock()

        then: "the investor should get #expectedBalance MDiv"
        omniGetBalance(investorAddress, propertyId).balance == expectedBalance.bigDecimalValue()

        and: "the issuer receives the invested amount"
        omniGetBalance(issuerAddress, currencyMSC).balance == startMSC + amountToInvest.bigDecimalValue()

        when: "retrieving the transaction"
        def sendTx = omniGetTransaction(sendTxid)

        then: "it's a valid crowdsale participation transaction"
        sendTx.txid == sendTxid.toString()
        sendTx.sendingaddress == investorAddress.toString()
        sendTx.referenceaddress == issuerAddress.toString()
        sendTx.version == 0
        sendTx.type_int == 0
        sendTx.type == "Crowdsale Purchase"
        sendTx.propertyid == currencyMSC.getValue()
        sendTx.divisible
        sendTx.amount as BigDecimal == amountToInvest.bigDecimalValue()
        sendTx.purchasedpropertyid == propertyId.getValue()
        sendTx.purchasedpropertyname == "MDiv"
        sendTx.purchasedpropertydivisible
        sendTx.purchasedtokens as BigDecimal == expectedBalance.bigDecimalValue()
        sendTx.issuertokens as BigDecimal == 0.0 // no bonus applied
        sendTx.valid

        when: "retrieving information about the crowdsale"
        def crowdsale = omniGetCrowdsale(propertyId)

        then: "the information matches the initial creation"
        crowdsale.propertyid == propertyId.getValue()
        crowdsale.name == "MDiv"
        crowdsale.active == !crowdsaleMaxed
        crowdsale.issuer == issuerAddress.toString()
        crowdsale.propertyiddesired == currencyMSC.getValue()
        crowdsale.tokensperunit as BigDecimal == 92233720368.54775807.divisible.bigDecimalValue()
        crowdsale.earlybonus == 0
        crowdsale.percenttoissuer == 0
        crowdsale.deadline == 7731414000
        if (crowdsaleMaxed) {
            assert crowdsale.closedearly
            assert crowdsale.maxtokens
        }
        crowdsale.tokensissued as BigDecimal == expectedBalance.bigDecimalValue()
        crowdsale.addedissuertokens as BigDecimal == 0.divisible.bigDecimalValue() // no bonus applied

        where:
        amountToInvest | expectedBalance                | crowdsaleMaxed
        0.01.divisible | 922337203.68547758.divisible   | false
        1.0.divisible  | 92233720368.54775807.divisible | false
        2.0.divisible  | 92233720368.54775807.divisible | true // Cap of 2^63-1 issued units reached!
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
        def investorAddress = createFundedAddress(startBTC, amountToInvest.bigDecimalValue(), false)
        def currencyMSC = CurrencyID.TMSC

        when: "creating a new crowdsale with 3400 TIndiv per unit invested"
        def crowdsaleTxid = omniSendRawTx(issuerAddress, rawTx)
        generateBlock()

        then: "the crowdsale is valid"
        def crowdsaleTx = omniGetTransaction(crowdsaleTxid)
        crowdsaleTx.confirmations == 1
        crowdsaleTx.valid

        and: "the crowdsale is active"
        def propertyId = new CurrencyID(crowdsaleTx.propertyid as Long)
        omniGetCrowdsale(propertyId).active

        when: "participant invests #amountToInvest TMSC"
        def sendTxid = omniSend(investorAddress, issuerAddress, currencyMSC, amountToInvest.bigDecimalValue())
        generateBlock()

        then: "the investor should get #expectedBalance TIndiv"
        omniGetBalance(investorAddress, propertyId).balance == expectedBalance.bigDecimalValue()

        and: "the issuer receives the invested amount"
        omniGetBalance(issuerAddress, currencyMSC).balance == startMSC + amountToInvest.bigDecimalValue()

        when: "retrieving the transaction"
        def sendTx = omniGetTransaction(sendTxid)

        then: "it's a valid crowdsale participation transaction"
        sendTx.txid == sendTxid.toString()
        sendTx.sendingaddress == investorAddress.toString()
        sendTx.referenceaddress == issuerAddress.toString()
        sendTx.version == 0
        sendTx.type_int == 0
        sendTx.type == "Crowdsale Purchase"
        sendTx.propertyid == currencyMSC.getValue()
        sendTx.divisible
        sendTx.amount as BigDecimal == amountToInvest.bigDecimalValue()
        sendTx.purchasedpropertyid == propertyId.getValue()
        sendTx.purchasedpropertyname == "TIndiv"
        !sendTx.purchasedpropertydivisible
        sendTx.purchasedtokens as BigDecimal == expectedBalance.bigDecimalValue()
        sendTx.issuertokens as BigDecimal == 0.0 // no bonus applied
        sendTx.valid

        when: "retrieving information about the crowdsale"
        def crowdsale = omniGetCrowdsale(propertyId)

        then: "the information matches the initial creation"
        crowdsale.propertyid == propertyId.getValue()
        crowdsale.name == "TIndiv"
        crowdsale.active
        crowdsale.issuer == issuerAddress.toString()
        crowdsale.propertyiddesired == currencyMSC.getValue()
        crowdsale.tokensperunit as BigDecimal == 3400.indivisible.bigDecimalValue()
        crowdsale.earlybonus == 0
        crowdsale.percenttoissuer == 0
        crowdsale.deadline == 7731414000
        crowdsale.tokensissued as BigDecimal == expectedBalance.bigDecimalValue()
        crowdsale.addedissuertokens as BigDecimal == 0.divisible.bigDecimalValue() // no bonus applied

        where:
        amountToInvest   | expectedBalance
        0.0025.divisible | 8.indivisible
        0.005.divisible  | 17.indivisible
    }

}
