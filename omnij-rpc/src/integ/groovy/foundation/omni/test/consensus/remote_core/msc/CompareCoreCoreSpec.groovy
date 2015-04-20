package foundation.omni.test.consensus.remote_core.msc

import foundation.omni.test.consensus.remote_core.CompareCoreCoreBaseSpec
import spock.lang.Title

import static foundation.omni.CurrencyID.MSC

/**
 * Local Omni Core vs Remote Omni Core consensus comparison for <code>MSC</code>
 */
@Title("Compare Master Core vs. Remote Master Core (e.g. stable/last revision)")
class CompareCoreCoreSpec extends CompareCoreCoreBaseSpec {

    void setupSpec() {
        setupCoreCoreComparisonForCurrency(MSC)
    }
}
