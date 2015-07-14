package foundation.omni.test.consensus.remote_core.tmsc

import foundation.omni.test.consensus.remote_core.CompareCoreCoreBaseSpec
import spock.lang.Ignore
import spock.lang.Title

import static foundation.omni.CurrencyID.TMSC

/**
 * Local Omni Core vs Remote Omni Core consensus comparison for <code>TMSC</code>
 */
@Title("Compare Omni Core vs. Remote Omni Core (e.g. stable/last revision)")
@Ignore("Omni test ecosystem on integration branch now has features not supported on production servers")
class CompareCoreCoreSpec extends CompareCoreCoreBaseSpec {

    void setupSpec() {
        setupCoreCoreComparisonForCurrency(TMSC)
    }
}
