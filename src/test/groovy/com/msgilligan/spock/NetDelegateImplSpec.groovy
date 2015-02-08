package com.msgilligan.spock

import spock.lang.Specification


/**
 * Spec that implements NetDelegate trait and thereby acquires the methods
 * of NetClient.
 */
class NetDelegateImplSpec extends Specification implements NetDelegate {
    def "can directly call method from NetClient"() {
        when:
        def result = makeRequest()

        then:
        result == "correct"
    }
}