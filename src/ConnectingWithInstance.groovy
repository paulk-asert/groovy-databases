import groovy.sql.Sql

def url = 'jdbc:hsqldb:mem:marathon'
def user = 'sa'
def password = ''
def driver = 'org.hsqldb.jdbcDriver'

// ...
Sql.withInstance(url, user, password, driver) {
    // use 'sql' instance ...
}
