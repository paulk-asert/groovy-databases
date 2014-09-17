// samples using BoneCP connection pool and DataSource constructor

//@Grab('com.jolbox:bonecp:0.8.0.RELEASE')
//@Grab('org.hsqldb:hsqldb:2.3.2')
import groovy.sql.Sql
import com.jolbox.bonecp.BoneCPDataSource
import com.jolbox.bonecp.BoneCPConfig

def url = 'jdbc:hsqldb:mem:marathon'
def config = new BoneCPConfig(jdbcUrl: url, username: 'sa', password: '')
def dataSource = new BoneCPDataSource(config)
def sql = new Sql(dataSource)

// use 'sql' instance ...
def result = sql.firstRow('SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS')
assert result[0] == 1

sql.close()
/* */
