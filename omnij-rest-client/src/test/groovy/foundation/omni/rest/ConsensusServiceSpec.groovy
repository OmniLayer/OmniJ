package foundation.omni.rest

import foundation.omni.netapi.omniwallet.OmniwalletAbstractClient
import foundation.omni.rest.omniwallet.OmniwalletClient
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

/**
 * TODO: Make this a base class so we can have Omniwallet and OmniCore implementations
 */
@Ignore("Integration test")
class ConsensusServiceSpec extends Specification {


    @Shared OmniwalletAbstractClient client

    def "Main net properties have not decreased"() {
        when: "we get data"
        def properties = client.listSmartProperties().get()

        then: "we get a list of size >= last time constant was set"
        properties.size() >= 1314

        and: "number of various categories of properties hasn't decreased"
        properties.stream().filter({p -> p.fixedIssuance }).count() >= 981
        properties.stream().filter({p -> p.managedIssuance }).count() >= 73
        properties.stream().filter({p -> p.freezingEnabled }).count() >= 2
    }

    def setup() {
        URI baseURL = OmniwalletAbstractClient.omniwalletBase
        boolean debug = true
        client = new OmniwalletClient(baseURL, debug)
    }

}
