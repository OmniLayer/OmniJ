package foundation.omni.tx

import spock.lang.Specification

import foundation.omni.tx.Transactions.TransactionType

/**
 * Tests for {@link TransactionType}
 */
class TransactionTypeSpec extends  Specification {

    void "switch test"(int code, boolean expectedResult) {
        given: "An Optional-wrapped Transaction Type enum"
        Optional<TransactionType> optionalType = TransactionType.find(code)

        when: "We use a switch expression to calculate a boolean value "
        boolean isSend = switch(optionalType.orElse(null))  {
            case TransactionType.SIMPLE_SEND,
                 TransactionType.SEND_TO_OWNERS,
                 TransactionType.SEND_ALL -> true
            case null -> false
            default -> false
        }

        then: "We get the correct result"
        isSend == expectedResult

        where:
        code    | expectedResult
        // Transaction codes that are "sends"
        0       | true
        3       | true
        4       | true
        // Defined Transactions that are not "sends"
        20      | false
        25      | false
        // Undefined transaction codes
        300     | false
    }
}
