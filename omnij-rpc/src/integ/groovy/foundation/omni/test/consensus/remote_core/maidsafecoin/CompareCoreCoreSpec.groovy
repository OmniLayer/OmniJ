package foundation.omni.test.consensus.remote_core.maidsafecoin

import foundation.omni.test.consensus.remote_core.CompareCoreCoreBaseSpec
import spock.lang.Ignore
import spock.lang.Title

import static foundation.omni.CurrencyID.MAID

/**
 * Local Omni Core vs Remote Omni Core consensus comparison for <code>MaidSafeCoin</code>
 */
@Ignore("Remote core server is down")
@Title("Compare Omni Core vs. Remote Omni Core (e.g. stable/last revision)")
class CompareCoreCoreSpec extends CompareCoreCoreBaseSpec {

    void setupSpec() {
        setupCoreCoreComparisonForCurrency(MAID)
    }
}
