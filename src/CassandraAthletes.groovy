@Grab('com.datastax.cassandra:cassandra-driver-core:2.1.1')
@Grab('com.datastax.cassandra:cassandra-driver-mapping:2.1.1')
@Grab('org.slf4j:slf4j-simple:1.7.5')
import com.datastax.driver.core.*

def node = '192.168.59.104'
def cluster = Cluster.builder().addContactPoint(node).build()
def metadata = cluster.metadata
println "Connected to cluster: $metadata.clusterName"
for (Host host : metadata.allHosts) {
    println """\
    Datacenter: $host.datacenter
    Host: $host.address
    Rack: $host.rack
    """.stripIndent()
}
def session = cluster.newSession()
session.execute '''
CREATE KEYSPACE IF NOT EXISTS marathon
WITH replication = {'class':'SimpleStrategy', 'replication_factor':3};
'''
session.execute '''
CREATE TABLE IF NOT EXISTS marathon.athlete (
    id uuid PRIMARY KEY,
    firstname text,
    lastname text,
    dateOfBirth timestamp
);
'''

session.execute '''
    TRUNCATE marathon.athlete
'''

session.execute '''
    INSERT INTO marathon.athlete (id, firstname, lastname, dateOfBirth)
    VALUES (uuid(), 'Paul', 'Tergat', '1969-06-17')
'''

// also supports bound variables
PreparedStatement ps = session.prepare '''
        INSERT INTO marathon.athlete (id, firstname, lastname, dateOfBirth)
        VALUES (uuid(), ?, ?, ?)
'''
def date = Date.parse('yyyy-MM-dd', '1971-12-22')
session.execute(new BoundStatement(ps).bind('Khalid', 'Khannouchi', date))

session.execute('SELECT * FROM marathon.athlete').each {
    println "${it.getString('firstname')} ${it.getString('lastname')} ${it.getDate('dateOfBirth').format('dd-MMM-yyyy')}"
}
session.close()
cluster.close()
