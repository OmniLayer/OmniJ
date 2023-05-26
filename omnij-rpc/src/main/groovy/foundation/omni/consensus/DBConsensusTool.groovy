package foundation.omni.consensus

import foundation.omni.json.pojo.ConsensusSnapshot
import foundation.omni.json.pojo.SmartPropertyListInfo
import groovy.sql.Sql
import foundation.omni.CurrencyID
import org.postgresql.ds.PGSimpleDataSource

import javax.sql.DataSource

/**
 * Command-line tool and class for fetching consensus data from OmniEngine DB
 */
abstract class DBConsensusTool implements ConsensusTool {
    private final PGSimpleDataSource dataSource
    private Sql sql = null

    DBConsensusTool(DataSource dataSource) {
        this.dataSource = dataSource
        this.sql = new Sql(dataSource)
    }

    public static void main(String[] args) {
        def source = new  PGSimpleDataSource()
        source.serverName = "hostname.rds.amazonaws.com"
        source.user = "username"
        source.password = "password"
        source.databaseName = "omniwallet"
        //DBConsensusTool tool = new DBConsensusTool(source)
        //tool.run(args.toList())
    }

    Integer currentBlockHeight() {
        def row = sql.rows("select max(blocknumber) from blocks")[0]
        def height = row.max
        return height;
    }

    List<SmartPropertyListInfo> listProperties() {
        throw new RuntimeException("Unimplemented method")
        return null
    }

    @Override
    ConsensusSnapshot getConsensusSnapshot(CurrencyID currencyID) {
        // TODO: Code needs to be written
        return null
    }
}
