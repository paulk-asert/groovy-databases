//////////////

@Grab('org.neo4j:neo4j-kernel:2.1.4')
@Grab('com.tinkerpop.gremlin:gremlin-groovy:2.5.0')
//@Grab('com.tinkerpop.blueprints:blueprints-neo4j-graph:2.5.0')
@Grab('com.tinkerpop.blueprints:blueprints-neo4j-graph:2.5.0;transitive=false')
@Grab('com.tinkerpop.blueprints:blueprints-core:2.5.0')
//@Grab('codehaus-stax:stax:1.1.1')
//@GrabResolver('https://repository.jboss.org/nexus/content/repositories/thirdparty-releases')
@GrabExclude('org.codehaus.groovy:groovy')
import com.tinkerpop.blueprints.Graph
import com.tinkerpop.blueprints.impls.neo4j.Neo4jGraph
//import com.tinkerpop.blueprints.util.io.graphml.GraphMLWriter
import com.tinkerpop.gremlin.groovy.Gremlin
import groovy.transform.Field
import org.neo4j.graphdb.traversal.Evaluators
import org.neo4j.kernel.EmbeddedGraphDatabase
import org.neo4j.graphdb.*
import org.neo4j.kernel.Traversal
import org.neo4j.kernel.Uniqueness

@Field graphDb = new EmbeddedGraphDatabase("athletes")

enum MyRelationshipTypes implements RelationshipType { ran, supercedes }

// some optional metaclass syntactic sugar
EmbeddedGraphDatabase.metaClass {
  createNode { Map properties ->
    def n = delegate.createNode()
    properties.each { k, v -> n[k] = v }
    n
  }
}
Node.metaClass {
  propertyMissing { String name, val -> delegate.setProperty(name, val) }
  propertyMissing { String name -> delegate.getProperty(name) }
  methodMissing { String name, args ->
    delegate.createRelationshipTo(args[0], MyRelationshipTypes."$name")
  }
}
Relationship.metaClass {
  propertyMissing { String name, val -> delegate.setProperty(name, val) }
  propertyMissing { String name -> delegate.getProperty(name) }
}

def insertAthlete(first, last, dob) {
  graphDb.createNode(first: first, last: last, dob: dob)
}

def insertRun(h, m, s, venue, when, athlete) {
  def run = graphDb.createNode(distance: 42195,
      time: h * 60 * 60 + m * 60 + s, venue: venue, when: when)
  athlete.ran(run)
  run
}

Gremlin.load()

def tx = graphDb.beginTx()
def athlete1, athlete2, athlete3, athlete4
def marathon1, marathon2a, marathon2b, marathon3, marathon4a, marathon4b
try {
  athlete1 = graphDb.createNode()
  athlete1.first = 'Paul'
  athlete1.last = 'Tergat'
  athlete1.dob = '1969-06-17'
  marathon1 = graphDb.createNode()
  marathon1.distance = 42195
  marathon1.time = 2 * 60 * 60 + 4 * 60 + 55
  marathon1.venue = 'Berlin'
  marathon1.when = '2003-09-28'
  athlete1.ran(marathon1)

  athlete2 = insertAthlete('Khalid', 'Khannouchi', '1971-12-22')
  marathon2a = insertRun(2, 5, 38, 'London', '2002-04-14', athlete2)
  marathon2b = insertRun(2, 5, 42, 'Chicago', '1999-10-24', athlete2)

  athlete3 = insertAthlete('Ronaldo', 'da Costa', '1970-06-07')
  marathon3 = insertRun(2, 6, 5, 'Berlin', '1998-09-20', athlete3)

  athlete4 = insertAthlete('Paula', 'Radcliffe', '1973-12-17')
  marathon4a = insertRun(2, 17, 18, 'Chicago', '2002-10-13', athlete4)
  marathon4b = insertRun(2, 15, 25, 'London', '2003-04-13', athlete4)

  def venue = marathon1.venue
  def when = marathon1.when
  println "$athlete1.first $athlete1.last won the $venue marathon on $when"

  def allAthletes = [athlete1, athlete2, athlete3, athlete4]
  def wonInLondon = allAthletes.findAll { athlete ->
    athlete.getRelationships(MyRelationshipTypes.ran).any { run ->
      run.getOtherNode(athlete).venue == 'London'
    }
  }
  assert wonInLondon*.last == ['Khannouchi', 'Radcliffe']

  marathon2b.supercedes(marathon3)
  marathon2a.supercedes(marathon2b)
  marathon1.supercedes(marathon2a)
  marathon4b.supercedes(marathon4a)

  println "World records following $marathon3.venue $marathon3.when:"
  def t = new Traversal()
  for (Path p in t.description().breadthFirst().
      relationships(MyRelationshipTypes.supercedes).
      evaluator(Evaluators.fromDepth(1)).
      uniqueness(Uniqueness.NONE).
      traverse(marathon3)) {
    def newRecord = p.endNode()
    println "$newRecord.venue $newRecord.when"
  }

  Graph g = new Neo4jGraph(graphDb)
  def pretty = { it.collect { "$it.venue $it.when" }.join(', ') }
  def results = []
  g.V('venue', 'London').fill(results)
  println 'London world records: ' + pretty(results)

  results = []
  g.V('venue', 'London').in('supercedes').fill(results)
  println 'World records after London: ' + pretty(results)

  results = []
  def emitAll = { true }
  def forever = { true }
  def berlin98 = { it.venue == 'Berlin' &&
      it.when.startsWith('1998') }
  g.V.filter(berlin98).in('supercedes').
      loop(1, forever, emitAll).fill(results)
  println 'World records after Berlin 1998: ' + pretty(results)
//    def writer = new GraphMLWriter(g)
//    def out = new FileOutputStream("c:/temp/athletes.graphml")
//    writer.outputGraph(out)
//    writer.setNormalize(true)
//    out.close()

  tx.success()
} finally {
  tx.finish()
  graphDb.shutdown()
}

/////////////////
