package foundation.omni.test.enginedb

import foundation.omni.consensus.DBConsensusTool
import org.postgresql.ds.PGSimpleDataSource
import spock.lang.Ignore
import spock.lang.Specification

/**
 *
 */
@Ignore
class OmniEngineDBSmokeSpec extends Specification {

    def "Can Talk to DB directly"() {
        setup:
        def source = new  PGSimpleDataSource()
        source.serverName = "hostname.rds.amazonaws.com"
        source.user = "username"
        source.password = "password"
        source.databaseName = "omniwallet"
        DBConsensusTool fetcher = new DBConsensusTool(source)

        when: "we get data"
        def height = fetcher.fetchBlockHeight()

        then: "something is there"
        height >= 0
    }

}