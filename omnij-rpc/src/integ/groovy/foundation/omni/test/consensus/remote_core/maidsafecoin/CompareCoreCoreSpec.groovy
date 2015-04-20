package foundation.omni.test.consensus.remote_core.maidsafecoin

import foundation.omni.test.consensus.remote_core.CompareCoreCoreBaseSpec
import spock.lang.Title

import static foundation.omni.CurrencyID.MaidSafeCoin

/**
 * Local Omni Core vs Remote Omni Core consensus comparison for <code>MaidSafeCoin</code>
 */
@Title("Compare Master Core vs. Remote Master Core (e.g. stable/last revision)")
class CompareCoreCoreSpec extends CompareCoreCoreBaseSpec {

    void setupSpec() {
        setupCoreCoreComparisonForCurrency(MaidSafeCoin)
    }
}
