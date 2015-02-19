package foundation.omni.consensus

import groovy.sql.Sql
import foundation.omni.CurrencyID
import org.postgresql.ds.PGSimpleDataSource

import javax.sql.DataSource

/**
 * Command-line tool and class for fetching consensus data from OmniEngine DB
 */
class DBConsensusTool extends ConsensusTool {
    private Sql sql = null;

    DBConsensusTool(DataSource dataSource) {
        sql = new Sql(dataSource)
    }

    public static void main(String[] args) {
        def source = new  PGSimpleDataSource()
        source.serverName = "hostname.rds.amazonaws.com"
        source.user = "username"
        source.password = "password"
        source.databaseName = "omniwallet"
        DBConsensusTool tool = new DBConsensusTool(source)
        tool.run(args.toList())
    }

    @Override
    Integer currentBlockHeight() {
        def row = sql.rows("select max(blocknumber) from blocks")[0]
        def height = row.max
        return height;
    }

    @Override
    ConsensusSnapshot getConsensusSnapshot(CurrencyID currencyID) {
        // TODO: Code needs to be written
        return null
    }
}
