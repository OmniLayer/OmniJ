package foundation.omni.test.rpc.basic

import foundation.omni.BaseRegTestSpec
import foundation.omni.json.pojo.OmniTransactionInfo

/**
 * Tests for OmniClient.omniListTransactions()
 */
class ListTransactionsSpec extends BaseRegTestSpec {
    def "We can list transactions"() {
        when: "we call omni_listtransactions"
        List<OmniTransactionInfo> transactions = omniListTransactions();

        then: "it should return a result with no error"
        transactions != null
        transactions.size() <= 10       // Default request size is 10
    }

    def "We can list all transactions"() {
        when: "we call omni_listtransactions"
        List<OmniTransactionInfo> transactions = omniListTransactions("*", Integer.MAX_VALUE, null, null, null);

        then: "it should return a result with no error"
        transactions != null
        transactions.size() >= 0
    }
}
