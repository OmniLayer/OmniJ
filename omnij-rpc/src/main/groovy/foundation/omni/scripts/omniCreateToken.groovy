package foundation.omni.scripts

import foundation.omni.CurrencyID
import foundation.omni.Ecosystem
import foundation.omni.PropertyType
import foundation.omni.test.RegTestContext

// Initialize a client, a blockchain env, and a funding source
def (client, env, funder) = RegTestContext.setup("bitcoinrpc", "pass")

// Load the manager address with some initial BTC and OMNI funds
def managerAddress = funder.createFundedAddress(1.btc, 100.divisible)
println "Created funded address: ${managerAddress}"

// Create a managed property
def creationTxId = client.createManagedProperty(managerAddress, Ecosystem.MSC, PropertyType.INDIVISIBLE, "Test Category",
        "Test Subcategory", "ManagedTokens", "http://www.omnilayer.org",
        "This is a test for managed properties")
env.waitForBlocks(1)
def creationTx = client.omniGetTransaction(creationTxId)
def currencyID = new CurrencyID(creationTx.propertyid as Long)
println "created currencyID ${currencyID}"

// Can Issue Tokens
def grantTxId = client.grantTokens(managerAddress, currencyID, 100.indivisible)
env.waitForBlocks(1)

// Can Send a newly issued token
def otherAddress = client.getNewAddress()
def sendTxId = client.omniSend(managerAddress, otherAddress, currencyID, 1.indivisible)
env.waitForBlocks(1)

// Some checks
def totalSPT = client.omniGetProperty(currencyID).totaltokens as BigDecimal
def managerSPT = client.omniGetBalance(managerAddress, currencyID).balance
def otherSPT = client.omniGetBalance(otherAddress, currencyID).balance
println "Manager has ${managerSPT} SPT, Other has ${otherSPT}, total = ${totalSPT}"
assert totalSPT == 100.0
assert managerSPT == 99.0
assert otherSPT == 1.0
