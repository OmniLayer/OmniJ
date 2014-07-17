package org.mastercoin.consensus

import groovy.json.JsonSlurper

/**
 * User: sean
 * Date: 7/8/14
 * Time: 1:46 PM
 */
class ChestListProperties {
    public static void main(String[] args) {
        def slurper = new JsonSlurper()
        def props = slurper.parse("https://masterchest.info/mastercoin_verify/properties.aspx/")
        println props.size()
    }
}
