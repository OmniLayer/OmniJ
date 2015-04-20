package foundation.omni.test.consensus.remote_core.tmsc

import foundation.omni.test.consensus.remote_core.CompareCoreCoreBaseSpec
import spock.lang.Title

import static foundation.omni.CurrencyID.TMSC

/**
 * Local Omni Core vs Remote Omni Core consensus comparison for <code>TMSC</code>
 */
@Title("Compare Master Core vs. Remote Master Core (e.g. stable/last revision)")
class CompareCoreCoreSpec extends CompareCoreCoreBaseSpec {

    void setupSpec() {
        setupCoreCoreComparisonForCurrency(TMSC)
    }
}
