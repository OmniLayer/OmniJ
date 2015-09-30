package foundation.omni.test.consensus.remote_core.maidsafecoin

import foundation.omni.test.consensus.remote_core.CompareCoreCoreBaseSpec
import spock.lang.Ignore
import spock.lang.Title

import static foundation.omni.CurrencyID.MaidSafeCoin

/**
 * Local Omni Core vs Remote Omni Core consensus comparison for <code>MaidSafeCoin</code>
 */
@Title("Compare Omni Core vs. Remote Omni Core (e.g. stable/last revision)")
@Ignore("Until we get a new test host")
class CompareCoreCoreSpec extends CompareCoreCoreBaseSpec {

    void setupSpec() {
        setupCoreCoreComparisonForCurrency(MaidSafeCoin)
    }
}
