#!/usr/bin/env groovy
@GrabResolver(name='bitcoinjaddons', root='https://dl.bintray.com/msgilligan/maven')
@Grab('com.msgilligan:bitcoinj-rpcclient:0.0.7')
@Grab('com.msgilligan:bitcoinj-groovy:0.0.7')
@GrabResolver(name='OmniJ', root='https://dl.bintray.com/omni/maven')
@Grab('foundation.omni:omnij-dsl:0.3.0')
@Grab('foundation.omni:omnij-rpc:0.3.0')
import foundation.omni.CurrencyID
import foundation.omni.Ecosystem
import foundation.omni.PropertyType
import foundation.omni.rpc.OmniExtendedClient
import foundation.omni.rpc.OmniCLIClient
import com.msgilligan.bitcoinj.rpc.RPCURI
import foundation.omni.test.OmniTestSupport

// Mine some blocks and setup a source of funds for testing
def miner = new RegTestMiner()
def managerAddress = miner.createFundedAddress(1.btc, 100.divisible)
println "Created funded address: ${managerAddress}"

// Create an OmniClient, to talk to a local Omni Core running in RegTest Mode
def client = new OmniExtendedClient(RPCURI.defaultRegTestURI, "bitcoinrpc", "pass")

// Create a managed property
def creationTxId = client.createManagedProperty(managerAddress, Ecosystem.MSC, PropertyType.INDIVISIBLE, "Test Category",
                                             "Test Subcategory", "ManagedTokens", "http://www.omnilayer.org",
                                             "This is a test for managed properties")
client.generateBlock()
def creationTx = client.omniGetTransaction(creationTxId)
def currencyID = new CurrencyID(creationTx.propertyid as Long)
println "created currencyID ${currencyID}"

// Can Issue Tokens
def grantTxId = client.grantTokens(managerAddress, currencyID, 100)
client.generateBlock()

// Can Send a newly issued token
def otherAddress = client.getNewAddress()
def sendTxId = client.omniSend(managerAddress, otherAddress, currencyID, 1)
client.generateBlock()

// Some checks
def totalSPT = client.omniGetProperty(currencyID).totaltokens as Integer
def managerSPT = client.omniGetBalance(managerAddress, currencyID).balance
def otherSPT = client.omniGetBalance(otherAddress, currencyID).balance
println "Manager has ${managerSPT} SPT, Other has ${otherSPT}, total = ${totalSPT}"
assert totalSPT == 100
assert managerSPT == 99
assert otherSPT == 1



// Leverage OmniTestSupport to create a "miner" that can fund an address with BTC and MSC
class RegTestMiner implements OmniTestSupport {
    { client = new OmniCLIClient(RPCURI.defaultRegTestURI,  "bitcoinrpc", "pass")  }
}
