package org.mastercoin.consensus

import groovy.json.JsonSlurper

/**
 * User: sean
 * Date: 7/8/14
 * Time: 1:56 PM
 */
class OmniListProperties {
    public static void main(String[] args) {
        def slurper = new JsonSlurper()
        def props = slurper.parse("https://www.omniwallet.org/v1/mastercoin_verify/properties")
        println props.size()
    }
}
