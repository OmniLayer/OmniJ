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
        nonManagedID = fundNewProperty(actorAddress, 10.0, PropertyType.DIVISIBLE, Ecosystem.MSC)
    }

    def "A managed property can be created with transaction type 54"() {
        when:
        creationTxid = createManagedProperty(actorAddress, Ecosystem.MSC, PropertyType.INDIVISIBLE, "Test Category",
                                             "Test Subcategory", "ManagedTokens", "http://www.omnilayer.org",
                                             "This is a test for managed properties")
        generateBlock()
        def creationTx = getTransactionMP(creationTxid)
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
        listproperties_MP().size() == old(listproperties_MP().size()) + 1
    }

    def "A managed property has a category, subcategory, name, website and description"() {
        when:
        def propertyInfo = getproperty_MP(currencyID)

        then:
        propertyInfo.propertyid == currencyID.longValue()
        propertyInfo.divisible == false
        propertyInfo.name == "ManagedTokens"
        propertyInfo.category == "Test Category"
        propertyInfo.subcategory == "Test Subcategory"
        propertyInfo.url == "http://www.omnilayer.org"
        propertyInfo.data == "This is a test for managed properties"
    }

    def "A managed property has no fixed supply and starts with 0 tokens"() {
        when:
        def propertyInfo = getproperty_MP(currencyID)

        then:
        propertyInfo.fixedissuance == false
        propertyInfo.totaltokens as Integer == 0

        when:
        def balanceForId = getallbalancesforid_MP(currencyID)
        def balanceForAddress = getbalance_MP(actorAddress, currencyID)

        then:
        balanceForId.size() == 0
        balanceForAddress.balance == zeroAmount
        balanceForAddress.reserved == zeroAmount
    }

    def "A reference to the issuer and creation transaction is available"() {
        when:
        def propertyInfo = getproperty_MP(currencyID)

        then:
        propertyInfo.issuer == actorAddress.toString()
        propertyInfo.creationtxid == creationTxid.toString()
    }

    def "New tokens can be granted with transaction type 55"() {
        when:
        def txid = grantTokens(actorAddress, currencyID, 100)
        generateBlock()

        then:
        getTransactionMP(txid).txid == txid.toString()
        getTransactionMP(txid).valid
        getTransactionMP(txid).type_int == 55
        getTransactionMP(txid).propertyid == currencyID.longValue()
        getTransactionMP(txid).divisible == false
        getTransactionMP(txid).amount as Integer == 100
    }

    def "Granting tokens increases the total number of tokens"() {
        when:
        def propertyInfo = getproperty_MP(currencyID)

        then:
        propertyInfo.totaltokens as Integer == 100
    }

    def "Granting tokens increases the issuer's balance"() {
        when:
        def balance = getbalance_MP(actorAddress, currencyID)

        then:
        balance.balance == 100 as BigDecimal
        balance.reserved == 0 as BigDecimal
    }

    def "Tokens can be granted several times"() {
        when:
        def txid = grantTokens(actorAddress, currencyID, 170)
        generateBlock()

        then:
        getTransactionMP(txid).txid == txid.toString()
        getTransactionMP(txid).valid
        getTransactionMP(txid).type_int == 55
        getTransactionMP(txid).propertyid == currencyID.longValue()
        getTransactionMP(txid).divisible == false
        getTransactionMP(txid).amount as Integer == 170

        and:
        getproperty_MP(currencyID).totaltokens as Integer == 270
        getbalance_MP(actorAddress, currencyID).balance == 270 as BigDecimal
    }

    def "It's impossible to grant tokens for an non-existing property"() {
        when:
        def txid = grantTokens(otherAddress, new CurrencyID(CurrencyID.MAX_REAL_ECOSYSTEM_VALUE), 1)
        generateBlock()

        then:
        getTransactionMP(txid).valid == false
    }

    def "Granting tokens for a property with fixed supply is invalid"() {
        when:
        def txid = grantTokens(actorAddress, nonManagedID, 1)
        generateBlock()

        then:
        getTransactionMP(txid).valid == false

        and:
        getproperty_MP(nonManagedID).totaltokens == old(getproperty_MP(nonManagedID)).totaltokens
        getbalance_MP(actorAddress, nonManagedID) == old(getbalance_MP(actorAddress, nonManagedID))
    }

    def "Tokens can only be granted by the issuer on record"() {
        when:
        def txid = grantTokens(otherAddress, currencyID, 500)
        generateBlock()

        then:
        getTransactionMP(txid).valid == false

        and:
        getproperty_MP(currencyID).totaltokens as Integer == 270
        getbalance_MP(actorAddress, currencyID).balance == 270 as BigDecimal
        getbalance_MP(otherAddress, currencyID).balance == zeroAmount
    }

    def "Up to a total of 9223372036854775807 tokens can be granted"() {
        when:
        def txid = grantTokens(actorAddress, currencyID, new Long("9223372036854775537")) // MAX - already granted tokens
        generateBlock()

        then:
        getTransactionMP(txid).valid
        getTransactionMP(txid).amount as Long == new Long("9223372036854775537")

        and:
        getproperty_MP(currencyID).totaltokens as Long == new Long("9223372036854775807")
        getbalance_MP(actorAddress, currencyID).balance == new BigDecimal("9223372036854775807")
    }

    @Ignore
    def "Granting more than 9223372036854775807 tokens in total is invalid"() {
        when:
        def txid = grantTokens(actorAddress, currencyID, 1)
        generateBlock()

        then:
        getTransactionMP(txid).valid == false

        and:
        getproperty_MP(currencyID).totaltokens == old(getproperty_MP(currencyID)).totaltokens
        getbalance_MP(actorAddress, currencyID) == old(getbalance_MP(actorAddress, currencyID))
    }

    def "Granted tokens can be transfered as usual"() {
        when:
        def txid = send_MP(actorAddress, otherAddress, currencyID, 1)
        generateBlock()

        then:
        getTransactionMP(txid).valid

        and:
        getproperty_MP(currencyID).totaltokens as Long == new Long("9223372036854775807")
        getbalance_MP(actorAddress, currencyID).balance == new BigDecimal("9223372036854775806")
        getbalance_MP(otherAddress, currencyID).balance == new BigDecimal("1")
    }

    @Ignore
    def "Sending of granted tokens does not remove the limit of total tokens"() {
        when:
        def txid = grantTokens(actorAddress, currencyID, 1) // MAX < total + 1 (!)
        generateBlock()

        then:
        getTransactionMP(txid).valid == false

        and:
        getproperty_MP(currencyID).totaltokens == old(getproperty_MP(currencyID)).totaltokens
        getbalance_MP(actorAddress, currencyID) == old(getbalance_MP(actorAddress, currencyID))
        getbalance_MP(otherAddress, currencyID) == old(getbalance_MP(otherAddress, currencyID))
    }

    def "Tokens of managed properties can be revoked with transaction type 56"() {
        when:
        def txid = revokeTokens(actorAddress, currencyID, new Long("9223372036854775805"))
        generateBlock()

        then:
        getTransactionMP(txid).txid == txid.toString()
        getTransactionMP(txid).valid
        getTransactionMP(txid).type_int == 56
        getTransactionMP(txid).propertyid == currencyID.longValue()
        getTransactionMP(txid).divisible == false
        getTransactionMP(txid).amount as Long == new Long("9223372036854775805")
    }

    def "Revoking tokens decreases the total number of tokens"() {
        when:
        def propertyInfo = getproperty_MP(currencyID)

        then:
        propertyInfo.totaltokens as Integer == 2
    }

    def "Revoking tokens decreases the issuer's balance"() {
        when:
        def balance = getbalance_MP(actorAddress, currencyID)

        then:
        balance.balance == 1 as BigDecimal
    }

    def "It's impossible to revoke tokens for an non-existing property"() {
        when:
        def txid = revokeTokens(otherAddress, new CurrencyID(CurrencyID.MAX_REAL_ECOSYSTEM_VALUE), 1)
        generateBlock()

        then:
        getTransactionMP(txid).valid == false
    }

    @Ignore
    def "Tokens can only be revoked by the issuer on record"() {
        when:
        def txid = revokeTokens(otherAddress, currencyID, 1)
        generateBlock()

        then:
        getTransactionMP(txid).valid == false

        and:
        getproperty_MP(currencyID).totaltokens == old(getproperty_MP(currencyID)).totaltokens
        getbalance_MP(actorAddress, currencyID) == old(getbalance_MP(actorAddress, currencyID))
        getbalance_MP(otherAddress, currencyID) == old(getbalance_MP(otherAddress, currencyID))
    }

    def "Revoking tokens for a property with fixed supply is invalid"() {
        when:
        def txid = revokeTokens(actorAddress, nonManagedID, 1)
        generateBlock()

        then:
        getTransactionMP(txid).valid == false

        and:
        getproperty_MP(nonManagedID).totaltokens == old(getproperty_MP(nonManagedID)).totaltokens
        getbalance_MP(actorAddress, nonManagedID) == old(getbalance_MP(actorAddress, nonManagedID))
    }

    def "Revoking more tokens than available is not possible"() {
        when:
        def txid = revokeTokens(actorAddress, currencyID, 100) // issuer has less than 100 tokens
        generateBlock()

        then:
        getTransactionMP(txid).valid == false

        and:
        getproperty_MP(currencyID).totaltokens == old(getproperty_MP(currencyID)).totaltokens
        getbalance_MP(actorAddress, currencyID) == old(getbalance_MP(actorAddress, currencyID))
        getbalance_MP(otherAddress, currencyID) == old(getbalance_MP(otherAddress, currencyID))
    }

}
