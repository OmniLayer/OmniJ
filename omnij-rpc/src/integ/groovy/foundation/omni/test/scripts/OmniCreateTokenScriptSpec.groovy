package foundation.omni.test.scripts

import foundation.omni.scripts.omniCreateToken
import spock.lang.Ignore
import spock.lang.Specification


/**
 * Just run the script and make sure its assert doesn't fail
 */
@Ignore("This isn't used or supported currently")
class OmniCreateTokenScriptSpec extends Specification {
    def "run the omniCreateToken.groovy script"() {
        expect:
        omniCreateToken.main()
    }

}