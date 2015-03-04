package foundation.omni.consensus

import groovy.json.JsonSlurper

/**
 * Command-line tool to fetch a list of all Master Protocol properties
 *
 * Prints list size (for now)
 */
class OmniwalletListProperties {
    public static void main(String[] args) {
        def slurper = new JsonSlurper()
        def props = slurper.parse("https://www.omniwallet.org/v1/mastercoin_verify/properties")
        println props.size()
    }
}
