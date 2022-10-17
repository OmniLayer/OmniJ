package foundation.omni.test.rpc.smartproperty

import foundation.omni.BaseRegTestSpec
import foundation.omni.CurrencyID
import foundation.omni.Ecosystem
import foundation.omni.OmniDivisibleValue
import foundation.omni.PropertyType
import org.bitcoinj.core.Address
import org.bitcoinj.core.Coin
import org.bitcoinj.core.Sha256Hash
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Stepwise

@Stepwise
class ManagedPropertySpec extends BaseRegTestSpec {

    final static Coin startBTC = 0.1.btc
    final static OmniDivisibleValue zeroAmount = 0.0.divisible

    @Shared Address actorAddress
    @Shared Address otherAddress
    @Shared CurrencyID currencyID
    @Shared CurrencyID nonManagedID
    @Shared Sha256Hash creationTxid

    def setupSpec() {
        actorAddress = createFundedAddress(startBTC, zeroAmount)
        otherAddress = createFundedAddress(startBTC, zeroAmount)
        nonManagedID = fundNewProperty(actorAddress, 10.divisible, Ecosystem.OMNI)
    }

    def "A managed property can be created with transaction type 54"() {
        when:
        creationTxid = createManagedProperty(actorAddress, Ecosystem.OMNI, PropertyType.INDIVISIBLE, "Test Category",
                                             "Test Subcategory", "ManagedTokens", "http://www.omnilayer.org",
                                             "This is a test for managed properties")
        generateBlocks(1)
        def creationTx = omniGetTransaction(creationTxid)
        currencyID = creationTx.propertyId

        then: "the transaction is valid"
        creationTx.valid

        and: "it has the specified values"
        creationTx.txId == creationTxid
        creationTx.typeInt == 54
        creationTx.divisible == false
        creationTx.otherInfo.propertyname == "ManagedTokens"
        creationTx.amount == 0

        and: "there is a new property"
        omniListProperties().size() == old(omniListProperties().size()) + 1
    }

    def "A managed property has a category, subcategory, name, website and description"() {
        when:
        def propertyInfo = omniGetProperty(currencyID)

        then:
        propertyInfo.propertyid == currencyID
        propertyInfo.divisible == false
        propertyInfo.name == "ManagedTokens"
        propertyInfo.category == "Test Category"
        propertyInfo.subcategory == "Test Subcategory"
        propertyInfo.url == "http://www.omnilayer.org"
        propertyInfo.data == "This is a test for managed properties"
    }

    def "A managed property has no fixed supply and starts with 0 tokens"() {
        when:
        def propertyInfo = omniGetProperty(currencyID)

        then:
        !propertyInfo.isFixedissuance()
        propertyInfo.totaltokens == 0

        when:
        def balanceForId = omniGetAllBalancesForId(currencyID)
        def balanceForAddress = omniGetBalance(actorAddress, currencyID)

        then:
        balanceForId.size() == 0
        balanceForAddress.balance == zeroAmount
        balanceForAddress.reserved == zeroAmount
    }

    def "A reference to the issuer and creation transaction is available"() {
        when:
        def propertyInfo = omniGetProperty(currencyID)

        then:
        propertyInfo.issuer == actorAddress
        propertyInfo.creationtxid == creationTxid
    }

    def "New tokens can be granted with transaction type 55"() {
        when:
        def txid = grantTokens(actorAddress, currencyID, 100.indivisible)
        generateBlocks(1)

        then:
        omniGetTransaction(txid).txId == txid
        omniGetTransaction(txid).valid
        omniGetTransaction(txid).typeInt == 55
        omniGetTransaction(txid).propertyId == currencyID
        !omniGetTransaction(txid).divisible
        omniGetTransaction(txid).amount == 100
    }

    def "Granting tokens increases the total number of tokens"() {
        when:
        def propertyInfo = omniGetProperty(currencyID)

        then:
        propertyInfo.totaltokens == 100
    }

    def "Granting tokens increases the issuer's balance"() {
        when:
        def balance = omniGetBalance(actorAddress, currencyID)

        then:
        balance.balance == 100 as BigDecimal
        balance.reserved == 0 as BigDecimal
    }

    def "Tokens can be granted several times"() {
        when:
        def txid = grantTokens(actorAddress, currencyID, 170.indivisible)
        generateBlocks(1)

        then:
        omniGetTransaction(txid).txId == txid
        omniGetTransaction(txid).valid
        omniGetTransaction(txid).typeInt == 55
        omniGetTransaction(txid).propertyId == currencyID
        !omniGetTransaction(txid).divisible
        omniGetTransaction(txid).amount == 170

        and:
        omniGetProperty(currencyID).totaltokens == 270
        omniGetBalance(actorAddress, currencyID).balance == 270 as BigDecimal
    }

    def "It's impossible to grant tokens for an non-existing property"() {
        when:
        def txid = grantTokens(otherAddress, new CurrencyID(CurrencyID.MAX_REAL_ECOSYSTEM_VALUE), 1.indivisible)
        generateBlocks(1)

        then:
        !omniGetTransaction(txid).valid
    }

    def "Granting tokens for a property with fixed supply is invalid"() {
        when:
        def txid = grantTokens(actorAddress, nonManagedID, 1.indivisible)
        generateBlocks(1)

        then:
        !omniGetTransaction(txid).valid

        and:
        omniGetProperty(nonManagedID).totaltokens == old(omniGetProperty(nonManagedID)).totaltokens
        omniGetBalance(actorAddress, nonManagedID) == old(omniGetBalance(actorAddress, nonManagedID))
    }

    def "Tokens can only be granted by the issuer on record"() {
        when:
        def txid = grantTokens(otherAddress, currencyID, 500.indivisible)
        generateBlocks(1)

        then:
        !omniGetTransaction(txid).valid

        and:
        omniGetProperty(currencyID).totaltokens == 270
        omniGetBalance(actorAddress, currencyID).balance == 270 as BigDecimal
        omniGetBalance(otherAddress, currencyID).balance == zeroAmount
    }

    def "Up to a total of 9223372036854775807 tokens can be granted"() {
        when:
        def txid = grantTokens(actorAddress, currencyID, 9223372036854775537.indivisible) // MAX - already granted tokens
        generateBlocks(1)

        then:
        omniGetTransaction(txid).valid
        omniGetTransaction(txid).amount.longValueExact() == 9223372036854775537L

        and:
        omniGetProperty(currencyID).totaltokens == 9223372036854775807L
        omniGetBalance(actorAddress, currencyID).balance == 9223372036854775807.0
    }

    @Ignore
    def "Granting more than 9223372036854775807 tokens in total is invalid"() {
        when:
        def txid = grantTokens(actorAddress, currencyID, 1.indivisible)
        generateBlocks(1)

        then:
        !omniGetTransaction(txid).valid

        and:
        omniGetProperty(currencyID).totaltokens == old(omniGetProperty(currencyID)).totaltokens
        omniGetBalance(actorAddress, currencyID) == old(omniGetBalance(actorAddress, currencyID))
    }

    def "Granted tokens can be transferred as usual"() {
        when:
        def txid = omniSend(actorAddress, otherAddress, currencyID, 1.indivisible)
        generateBlocks(1)

        then:
        omniGetTransaction(txid).valid

        and:
        omniGetProperty(currencyID).totaltokens as Long == 9223372036854775807L
        omniGetBalance(actorAddress, currencyID).balance == 9223372036854775806L
        omniGetBalance(otherAddress, currencyID).balance == 1
    }

    @Ignore
    def "Sending of granted tokens does not remove the limit of total tokens"() {
        when:
        def txid = grantTokens(actorAddress, currencyID, 1.indivisible) // MAX < total + 1 (!)
        generateBlocks(1)

        then:
        !omniGetTransaction(txid).valid

        and:
        omniGetProperty(currencyID).totaltokens == old(omniGetProperty(currencyID)).totaltokens
        omniGetBalance(actorAddress, currencyID) == old(omniGetBalance(actorAddress, currencyID))
        omniGetBalance(otherAddress, currencyID) == old(omniGetBalance(otherAddress, currencyID))
    }

    def "Tokens of managed properties can be revoked with transaction type 56"() {
        when:
        def txid = revokeTokens(actorAddress, currencyID, 9223372036854775805.indivisible)
        generateBlocks(1)

        then:
        omniGetTransaction(txid).txId == txid
        omniGetTransaction(txid).valid
        omniGetTransaction(txid).typeInt == 56
        omniGetTransaction(txid).propertyId == currencyID
        !omniGetTransaction(txid).divisible
        omniGetTransaction(txid).amount.longValueExact() == 9223372036854775805L
    }

    def "Revoking tokens decreases the total number of tokens"() {
        when:
        def propertyInfo = omniGetProperty(currencyID)

        then:
        propertyInfo.totaltokens == 2
    }

    def "Revoking tokens decreases the issuer's balance"() {
        when:
        def balance = omniGetBalance(actorAddress, currencyID)

        then:
        balance.balance == 1 as BigDecimal
    }

    def "It's impossible to revoke tokens for an non-existing property"() {
        when:
        def txid = revokeTokens(otherAddress, new CurrencyID(CurrencyID.MAX_REAL_ECOSYSTEM_VALUE), 1.divisible)
        generateBlocks(1)

        then:
        !omniGetTransaction(txid).valid
    }

    def "Tokens can be revoked by non-issuer owners"() {
        when:
        var txid = revokeTokens(otherAddress, currencyID, 1.indivisible)
        generateBlocks(1)
        var txInfo = omniGetTransaction(txid)

        then:
        txInfo.valid
        txInfo.propertyId == currencyID

        and:
        omniGetProperty(currencyID).totaltokens == old(omniGetProperty(currencyID)).totaltokens - 1.indivisible
        omniGetBalance(actorAddress, currencyID) == old(omniGetBalance(actorAddress, currencyID))
        omniGetBalance(otherAddress, currencyID).balance == old(omniGetBalance(otherAddress, currencyID)).balance - 1.indivisible
        omniGetBalance(otherAddress, currencyID).reserved == old(omniGetBalance(otherAddress, currencyID)).reserved
        omniGetBalance(otherAddress, currencyID).frozen == old(omniGetBalance(otherAddress, currencyID)).frozen
    }

    def "Tokens can't be revoked when there is an insufficient balance"() {
        when:
        var txid = revokeTokens(otherAddress, currencyID, 1.indivisible)
        generateBlocks(1)
        var txInfo = omniGetTransaction(txid)

        then:
        !txInfo.valid
        txInfo.propertyId == currencyID
        txInfo.otherInfo.get("invalidreason") == "Sender has insufficient balance"

        and:
        omniGetProperty(currencyID).totaltokens == old(omniGetProperty(currencyID)).totaltokens
        omniGetBalance(actorAddress, currencyID) == old(omniGetBalance(actorAddress, currencyID))
        omniGetBalance(otherAddress, currencyID) == old(omniGetBalance(otherAddress, currencyID))
    }

    def "Revoking tokens for a property with fixed supply is invalid"() {
        when:
        def txid = revokeTokens(actorAddress, nonManagedID, 1.divisible)
        generateBlocks(1)

        then:
        !omniGetTransaction(txid).valid

        and:
        omniGetProperty(nonManagedID).totaltokens == old(omniGetProperty(nonManagedID)).totaltokens
        omniGetBalance(actorAddress, nonManagedID) == old(omniGetBalance(actorAddress, nonManagedID))
    }

    def "Revoking more tokens than available is not possible"() {
        when:
        def txid = revokeTokens(actorAddress, currencyID, 100.divisible) // issuer has less than 100 tokens
        generateBlocks(1)

        then:
        !omniGetTransaction(txid).valid

        and:
        omniGetProperty(currencyID).totaltokens == old(omniGetProperty(currencyID)).totaltokens
        omniGetBalance(actorAddress, currencyID) == old(omniGetBalance(actorAddress, currencyID))
        omniGetBalance(otherAddress, currencyID) == old(omniGetBalance(otherAddress, currencyID))
    }

}
