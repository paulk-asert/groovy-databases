// sample using Apache Commons DBCP connection pool and DataSource constructor

@Grab('org.hsqldb:hsqldb:2.3.2')
@Grab('commons-dbcp:commons-dbcp:1.4')
import groovy.sql.Sql
import org.apache.commons.dbcp.BasicDataSource

def url = 'jdbc:hsqldb:mem:marathon'
def driver = 'org.hsqldb.jdbcDriver'
def dataSource = new BasicDataSource(
        driverClassName: driver, url: url,
        username: 'sa', password: '')
def sql = new Sql(dataSource)

// use 'sql' instance ...

sql.close()
