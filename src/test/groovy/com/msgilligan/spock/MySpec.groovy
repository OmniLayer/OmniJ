package com.msgilligan.spock

import spock.lang.Specification


/**
 * User: sean
 * Date: 7/31/14
 * Time: 12:39 PM
 */
class MySpec extends Specification implements NetDelegate {
    def "a test"() {
        when:
        def result = makeRequest()

        then:
        result == "correct"
    }
}