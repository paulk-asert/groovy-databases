//@Grab('org.hsqldb:hsqldb:2.3.2')
import groovy.sql.Sql
import org.hsqldb.jdbc.JDBCDataSource

def dataSource = new JDBCDataSource(
        database: 'jdbc:hsqldb:mem:marathon', user: 'sa', password: '')
def sql = new Sql(dataSource)

// use 'sql' instance

sql.close()
