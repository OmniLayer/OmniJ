package foundation.omni.test.rpc.smartproperty

import foundation.omni.BaseRegTestSpec
import foundation.omni.CurrencyID
import foundation.omni.Ecosystem
import foundation.omni.PropertyType
import org.bitcoinj.core.Address
import org.bitcoinj.core.Sha256Hash
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Stepwise

@Stepwise
class ManagedPropertySpec extends BaseRegTestSpec {

    final static BigDecimal startBTC = 0.1
    final static BigDecimal zeroAmount = 0.0

    @Shared Address actorAddress
    @Shared Address otherAddress
    @Shared CurrencyID currencyID
    @Shared CurrencyID nonManagedID
    @Shared Sha256Hash creationTxid

    def setupSpec() {
        actorAddress = createFundedAddress(startBTC, zeroAmount)
        otherAddress = createFundedAddress(startBTC, zeroAmount)
        nonManagedID = fundNewProperty(actorAddress, 10.divisible, Ecosystem.MSC)
    }

    def "A managed property can be created with transaction type 54"() {
        when:
        creationTxid = createManagedProperty(actorAddress, Ecosystem.MSC, PropertyType.INDIVISIBLE, "Test Category",
                                             "Test Subcategory", "ManagedTokens", "http://www.omnilayer.org",
                                             "This is a test for managed properties")
        generateBlock()
        def creationTx = omniGetTransaction(creationTxid)
        currencyID = new CurrencyID(creationTx.propertyid as Long)

        then: "the transaction is valid"
        creationTx.valid

        and: "it has the specified values"
        creationTx.txid == creationTxid.toString()
        creationTx.type_int == 54
        creationTx.divisible == false
        creationTx.propertyname == "ManagedTokens"
        creationTx.amount as Integer == 0

        and: "there is a new property"
        omniListProperties().size() == old(omniListProperties().size()) + 1
    }

    def "A managed property has a category, subcategory, name, website and description"() {
        when:
        def propertyInfo = omniGetProperty(currencyID)

        then:
        propertyInfo.propertyid == currencyID.getValue()
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
        propertyInfo.fixedissuance == false
        propertyInfo.totaltokens as Integer == 0

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
        propertyInfo.issuer == actorAddress.toString()
        propertyInfo.creationtxid == creationTxid.toString()
    }

    def "New tokens can be granted with transaction type 55"() {
        when:
        def txid = grantTokens(actorAddress, currencyID, 100.indivisible)
        generateBlock()

        then:
        omniGetTransaction(txid).txid == txid.toString()
        omniGetTransaction(txid).valid
        omniGetTransaction(txid).type_int == 55
        omniGetTransaction(txid).propertyid == currencyID.getValue()
        omniGetTransaction(txid).divisible == false
        omniGetTransaction(txid).amount as Integer == 100
    }

    def "Granting tokens increases the total number of tokens"() {
        when:
        def propertyInfo = omniGetProperty(currencyID)

        then:
        propertyInfo.totaltokens as Integer == 100
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
        generateBlock()

        then:
        omniGetTransaction(txid).txid == txid.toString()
        omniGetTransaction(txid).valid
        omniGetTransaction(txid).type_int == 55
        omniGetTransaction(txid).propertyid == currencyID.getValue()
        omniGetTransaction(txid).divisible == false
        omniGetTransaction(txid).amount as Integer == 170

        and:
        omniGetProperty(currencyID).totaltokens as Integer == 270
        omniGetBalance(actorAddress, currencyID).balance == 270 as BigDecimal
    }

    def "It's impossible to grant tokens for an non-existing property"() {
        when:
        def txid = grantTokens(otherAddress, new CurrencyID(CurrencyID.MAX_REAL_ECOSYSTEM_VALUE), 1.indivisible)
        generateBlock()

        then:
        omniGetTransaction(txid).valid == false
    }

    def "Granting tokens for a property with fixed supply is invalid"() {
        when:
        def txid = grantTokens(actorAddress, nonManagedID, 1.indivisible)
        generateBlock()

        then:
        omniGetTransaction(txid).valid == false

        and:
        omniGetProperty(nonManagedID).totaltokens == old(omniGetProperty(nonManagedID)).totaltokens
        omniGetBalance(actorAddress, nonManagedID) == old(omniGetBalance(actorAddress, nonManagedID))
    }

    def "Tokens can only be granted by the issuer on record"() {
        when:
        def txid = grantTokens(otherAddress, currencyID, 500.indivisible)
        generateBlock()

        then:
        omniGetTransaction(txid).valid == false

        and:
        omniGetProperty(currencyID).totaltokens as Integer == 270
        omniGetBalance(actorAddress, currencyID).balance == 270 as BigDecimal
        omniGetBalance(otherAddress, currencyID).balance == zeroAmount
    }

    def "Up to a total of 9223372036854775807 tokens can be granted"() {
        when:
        def txid = grantTokens(actorAddress, currencyID, 9223372036854775537.indivisible) // MAX - already granted tokens
        generateBlock()

        then:
        omniGetTransaction(txid).valid
        omniGetTransaction(txid).amount as Long == 9223372036854775537L

        and:
        omniGetProperty(currencyID).totaltokens as Long == 9223372036854775807L
        omniGetBalance(actorAddress, currencyID).balance == 9223372036854775807.0
    }

    @Ignore
    def "Granting more than 9223372036854775807 tokens in total is invalid"() {
        when:
        def txid = grantTokens(actorAddress, currencyID, 1.indivisible)
        generateBlock()

        then:
        omniGetTransaction(txid).valid == false

        and:
        omniGetProperty(currencyID).totaltokens == old(omniGetProperty(currencyID)).totaltokens
        omniGetBalance(actorAddress, currencyID) == old(omniGetBalance(actorAddress, currencyID))
    }

    def "Granted tokens can be transfered as usual"() {
        when:
        def txid = omniSend(actorAddress, otherAddress, currencyID, 1.indivisible)
        generateBlock()

        then:
        omniGetTransaction(txid).valid

        and:
        omniGetProperty(currencyID).totaltokens as Long == 9223372036854775807L
        omniGetBalance(actorAddress, currencyID).balance == 9223372036854775806.0
        omniGetBalance(otherAddress, currencyID).balance == 1.0
    }

    @Ignore
    def "Sending of granted tokens does not remove the limit of total tokens"() {
        when:
        def txid = grantTokens(actorAddress, currencyID, 1.indivisible) // MAX < total + 1 (!)
        generateBlock()

        then:
        omniGetTransaction(txid).valid == false

        and:
        omniGetProperty(currencyID).totaltokens == old(omniGetProperty(currencyID)).totaltokens
        omniGetBalance(actorAddress, currencyID) == old(omniGetBalance(actorAddress, currencyID))
        omniGetBalance(otherAddress, currencyID) == old(omniGetBalance(otherAddress, currencyID))
    }

    def "Tokens of managed properties can be revoked with transaction type 56"() {
        when:
        def txid = revokeTokens(actorAddress, currencyID, 9223372036854775805.indivisible)
        generateBlock()

        then:
        omniGetTransaction(txid).txid == txid.toString()
        omniGetTransaction(txid).valid
        omniGetTransaction(txid).type_int == 56
        omniGetTransaction(txid).propertyid == currencyID.getValue()
        omniGetTransaction(txid).divisible == false
        omniGetTransaction(txid).amount as Long == 9223372036854775805L
    }

    def "Revoking tokens decreases the total number of tokens"() {
        when:
        def propertyInfo = omniGetProperty(currencyID)

        then:
        propertyInfo.totaltokens as Integer == 2
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
        generateBlock()

        then:
        omniGetTransaction(txid).valid == false
    }

    @Ignore
    def "Tokens can only be revoked by the issuer on record"() {
        when:
        def txid = revokeTokens(otherAddress, currencyID, 1.divisible)
        generateBlock()

        then:
        omniGetTransaction(txid).valid == false

        and:
        omniGetProperty(currencyID).totaltokens == old(omniGetProperty(currencyID)).totaltokens
        omniGetBalance(actorAddress, currencyID) == old(omniGetBalance(actorAddress, currencyID))
        omniGetBalance(otherAddress, currencyID) == old(omniGetBalance(otherAddress, currencyID))
    }

    def "Revoking tokens for a property with fixed supply is invalid"() {
        when:
        def txid = revokeTokens(actorAddress, nonManagedID, 1.divisible)
        generateBlock()

        then:
        omniGetTransaction(txid).valid == false

        and:
        omniGetProperty(nonManagedID).totaltokens == old(omniGetProperty(nonManagedID)).totaltokens
        omniGetBalance(actorAddress, nonManagedID) == old(omniGetBalance(actorAddress, nonManagedID))
    }

    def "Revoking more tokens than available is not possible"() {
        when:
        def txid = revokeTokens(actorAddress, currencyID, 100.divisible) // issuer has less than 100 tokens
        generateBlock()

        then:
        omniGetTransaction(txid).valid == false

        and:
        omniGetProperty(currencyID).totaltokens == old(omniGetProperty(currencyID)).totaltokens
        omniGetBalance(actorAddress, currencyID) == old(omniGetBalance(actorAddress, currencyID))
        omniGetBalance(otherAddress, currencyID) == old(omniGetBalance(otherAddress, currencyID))
    }

}
