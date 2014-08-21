package org.mastercoin.consensus

import groovy.json.JsonSlurper

/**
 * Command-line tool to fetch a list of all Master Protocol properties from Master Chest
 *
 * Prints list size (for now)
 */
class ChestListProperties {
    public static void main(String[] args) {
        def slurper = new JsonSlurper()
        def props = slurper.parse("https://masterchest.info/mastercoin_verify/properties.aspx/")
        println props.size()
    }
}
