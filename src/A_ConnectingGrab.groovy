//@Grab('org.hsqldb:hsqldb:2.3.2')
@GrabConfig(systemClassLoader=true)
import groovy.sql.Sql

// ...
def url = 'jdbc:hsqldb:mem:marathon'
def user = 'sa'
def password = ''
def driver = 'org.hsqldb.jdbcDriver'
def sql = Sql.newInstance(url, user, password, driver)

// use 'sql' instance

sql.close()
