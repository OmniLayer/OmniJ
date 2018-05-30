package foundation.omni.test.consensus.remote_core.msc

import foundation.omni.test.consensus.remote_core.CompareCoreCoreBaseSpec
import spock.lang.Ignore
import spock.lang.Title

import static foundation.omni.CurrencyID.OMNI

/**
 * Local Omni Core vs Remote Omni Core consensus comparison for <code>OMNI</code>
 */
@Ignore("Remote core server is down")
@Title("Compare Omni Core vs. Remote Omni Core (e.g. stable/last revision)")
class CompareCoreCoreSpec extends CompareCoreCoreBaseSpec {

    void setupSpec() {
        setupCoreCoreComparisonForCurrency(OMNI)
    }
}
