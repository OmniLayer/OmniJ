package foundation.omni.test.rpc.crowdsale

import foundation.omni.BaseRegTestSpec
import foundation.omni.CurrencyID
import spock.lang.Unroll

class CrowdsaleParticipationSpec extends BaseRegTestSpec {

    final static startBTC = 0.01.btc
    final static startOmni = 0.divisible

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
        def issuerAddress = createFundedAddress(startBTC, startOmni, false)
        def investorAddress = createFundedAddress(startBTC, amountToInvest, false)
        def currencyMSC = CurrencyID.OMNI

        when: "creating a new crowdsale with 0.00000001 MDiv per unit invested"
        def crowdsaleTxid = omniSendRawTx(issuerAddress, rawTx)
        generateBlocks(1)

        then: "the crowdsale is valid"
        def crowdsaleTx = omniGetTransaction(crowdsaleTxid)
        crowdsaleTx.confirmations == 1
        crowdsaleTx.valid

        and: "the crowdsale is active"
        def propertyId = crowdsaleTx.propertyId
        omniGetCrowdsale(propertyId).active

        when: "participant invests #amountToInvest OMNI"
        def sendTxid = omniSend(investorAddress, issuerAddress, currencyMSC, amountToInvest)
        generateBlocks(1)

        then: "the investor should get #expectedBalance MDiv"
        omniGetBalance(investorAddress, propertyId).balance.equals(expectedBalance)

        and: "the issuer receives the invested amount"
        omniGetBalance(issuerAddress, currencyMSC).balance.equals(startOmni + amountToInvest)

        when: "retrieving the transaction"
        def sendTx = omniGetTransaction(sendTxid)

        then: "it's a valid crowdsale participation transaction"
        sendTx.txId == sendTxid
        sendTx.sendingAddress == investorAddress
        sendTx.referenceAddress == issuerAddress
        sendTx.version == 0
        sendTx.typeInt == 0
        sendTx.type == "Crowdsale Purchase"
        sendTx.propertyId == currencyMSC
        sendTx.divisible
        sendTx.amount == amountToInvest
        sendTx.otherInfo.purchasedpropertyid == propertyId.getValue()
        sendTx.otherInfo.purchasedpropertyname == "MDiv"
        sendTx.otherInfo.purchasedpropertydivisible
        sendTx.otherInfo.purchasedtokens as BigDecimal == expectedBalance.bigDecimalValue()
        sendTx.otherInfo.issuertokens as BigDecimal == 0.0 // no bonus applied
        sendTx.valid

        when: "retrieving information about the crowdsale"
        def crowdsale = omniGetCrowdsale(propertyId)

        then: "the information matches the initial creation"
        crowdsale.propertyid == propertyId.getValue()
        crowdsale.name == "MDiv"
        crowdsale.active
        crowdsale.issuer == issuerAddress.toString()
        crowdsale.propertyiddesired == currencyMSC.getValue()
        crowdsale.tokensperunit as BigDecimal == 0.00000001.divisible.numberValue()
        crowdsale.earlybonus == 0
        crowdsale.percenttoissuer == 0
        crowdsale.deadline == 7731414000
        crowdsale.tokensissued as BigDecimal == expectedBalance.numberValue()
        crowdsale.addedissuertokens as BigDecimal == 0.divisible.numberValue() // no bonus applied

        when:
        def txidClose = closeCrowdsale(issuerAddress, propertyId) // bypass RPC layer
        generateBlocks(1)

        then:
        omniGetTransaction(txidClose).valid

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
        def issuerAddress = createFundedAddress(startBTC, startOmni, false)
        def investorAddress = createFundedAddress(startBTC, amountToInvest, false)
        def currencyMSC = CurrencyID.OMNI

        when: "creating a new crowdsale with 0.00000001 MDiv per unit invested"
        def crowdsaleTxid = omniSendRawTx(issuerAddress, rawTx)
        generateBlocks(1)

        then: "the crowdsale is valid"
        def crowdsaleTx = omniGetTransaction(crowdsaleTxid)
        crowdsaleTx.confirmations == 1
        crowdsaleTx.valid

        and: "the crowdsale is active"
        def propertyId = crowdsaleTx.propertyId
        omniGetCrowdsale(propertyId).active

        when: "participant invests #amountToInvest OMNI"
        def sendTxid = omniSend(investorAddress, issuerAddress, currencyMSC, amountToInvest)
        generateBlocks(1)

        then: "the investor should get #expectedBalance MDiv"
        omniGetBalance(investorAddress, propertyId).balance.equals(expectedBalance)

        and: "the issuer receives the invested amount"
        omniGetBalance(issuerAddress, currencyMSC).balance.equals(startOmni + amountToInvest)

        when: "retrieving the transaction"
        def sendTx = omniGetTransaction(sendTxid)

        then: "it's a valid crowdsale participation transaction"
        sendTx.txId == sendTxid
        sendTx.sendingAddress == investorAddress
        sendTx.referenceAddress == issuerAddress
        sendTx.version == 0
        sendTx.typeInt == 0
        sendTx.type == "Crowdsale Purchase"
        sendTx.propertyId == currencyMSC
        sendTx.divisible
        sendTx.amount == amountToInvest
        sendTx.otherInfo.purchasedpropertyid == propertyId.getValue()
        sendTx.otherInfo.purchasedpropertyname == "MDiv"
        sendTx.otherInfo.purchasedpropertydivisible
        sendTx.otherInfo.purchasedtokens as BigDecimal == expectedBalance.bigDecimalValue()
        sendTx.otherInfo.issuertokens as BigDecimal == 0.0 // no bonus applied
        sendTx.valid

        when: "retrieving information about the crowdsale"
        def crowdsale = omniGetCrowdsale(propertyId)

        then: "the information matches the initial creation"
        crowdsale.propertyid == propertyId.getValue()
        crowdsale.name == "MDiv"
        crowdsale.active == !crowdsaleMaxed
        crowdsale.issuer == issuerAddress.toString()
        crowdsale.propertyiddesired == currencyMSC.getValue()
        crowdsale.tokensperunit as BigDecimal == 92233720368.54775807.divisible.numberValue()
        crowdsale.earlybonus == 0
        crowdsale.percenttoissuer == 0
        crowdsale.deadline == 7731414000
        if (crowdsaleMaxed) {
            assert crowdsale.closedearly
            assert crowdsale.maxtokens
        }
        crowdsale.tokensissued as BigDecimal == expectedBalance.numberValue()
        crowdsale.addedissuertokens as BigDecimal == 0.divisible.numberValue() // no bonus applied

        when:
        def txidClose = closeCrowdsale(issuerAddress, propertyId) // bypass RPC layer
        generateBlocks(1)

        then:
        omniGetTransaction(txidClose).valid == !crowdsaleMaxed

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
        def rawTx = "0000003302000100000000000054496e646976000000000000020000000000000d4800000001ccd403f00000"
        def issuerAddress = createFundedAddress(startBTC, startOmni, false)
        def investorAddress = createFundedAddress(startBTC, amountToInvest, false)
        def currencyMSC = CurrencyID.TOMNI

        when: "creating a new crowdsale with 3400 TIndiv per unit invested"
        def crowdsaleTxid = omniSendRawTx(issuerAddress, rawTx)
        generateBlocks(1)

        then: "the crowdsale is valid"
        def crowdsaleTx = omniGetTransaction(crowdsaleTxid)
        crowdsaleTx.confirmations == 1
        crowdsaleTx.valid

        and: "the crowdsale is active"
        def propertyId = crowdsaleTx.propertyId
        omniGetCrowdsale(propertyId).active

        when: "participant invests #amountToInvest TOMNI"
        def sendTxid = omniSend(investorAddress, issuerAddress, currencyMSC, amountToInvest)
        generateBlocks(1)

        then: "the investor should get #expectedBalance TIndiv"
        omniGetBalance(investorAddress, propertyId).balance.longValue() == expectedBalance.longValue()

        and: "the issuer receives the invested amount"
        omniGetBalance(issuerAddress, currencyMSC).balance.numberValue() == startOmni.numberValue() + amountToInvest.numberValue()

        when: "retrieving the transaction"
        def sendTx = omniGetTransaction(sendTxid)

        then: "it's a valid crowdsale participation transaction"
        sendTx.txId == sendTxid
        sendTx.sendingAddress == investorAddress
        sendTx.referenceAddress == issuerAddress
        sendTx.version == 0
        sendTx.typeInt == 0
        sendTx.type == "Crowdsale Purchase"
        sendTx.propertyId == currencyMSC
        sendTx.divisible
        sendTx.amount == amountToInvest
        sendTx.otherInfo.purchasedpropertyid == propertyId.getValue()
        sendTx.otherInfo.purchasedpropertyname == "TIndiv"
        !sendTx.otherInfo.purchasedpropertydivisible
        sendTx.otherInfo.purchasedtokens as Long == expectedBalance.numberValue()
        sendTx.otherInfo.issuertokens as BigDecimal == 0.0 // no bonus applied
        sendTx.valid

        when: "retrieving information about the crowdsale"
        def crowdsale = omniGetCrowdsale(propertyId)

        then: "the information matches the initial creation"
        crowdsale.propertyid == propertyId.getValue()
        crowdsale.name == "TIndiv"
        crowdsale.active
        crowdsale.issuer == issuerAddress.toString()
        crowdsale.propertyiddesired == currencyMSC.getValue()
        crowdsale.tokensperunit as Long == 3400.indivisible.numberValue()
        crowdsale.earlybonus == 0
        crowdsale.percenttoissuer == 0
        crowdsale.deadline == 7731414000
        crowdsale.tokensissued as Long == expectedBalance.numberValue()
        crowdsale.addedissuertokens as BigDecimal == 0.divisible.numberValue() // no bonus applied

        when:
        def txidClose = closeCrowdsale(issuerAddress, propertyId) // bypass RPC layer
        generateBlocks(1)

        then:
        omniGetTransaction(txidClose).valid

        where:
        amountToInvest   | expectedBalance
        0.0025.divisible | 8.indivisible
        0.005.divisible  | 17.indivisible
    }

}
