@Grab('com.h2database:h2:1.4.181')
import groovy.sql.Sql
import groovy.transform.Field
import org.h2.jdbcx.JdbcDataSource
// ensure that this source file is on the classpath before running

@Field Sql sql = new Sql(new JdbcDataSource(
    URL: 'jdbc:h2:mem:GIA;DB_CLOSE_DELAY=-1', user: 'sa', password: ''))

def DLL_WITH_VIEW = '''
    DROP   TABLE Athlete IF EXISTS;
    CREATE TABLE Athlete (
      athleteId   INTEGER GENERATED BY DEFAULT AS IDENTITY,
      firstname   VARCHAR(64),
      lastname    VARCHAR(64),
      dateOfBirth DATE
    );
    DROP   INDEX idx IF EXISTS;
    CREATE INDEX idx ON Athlete (athleteId);
    DROP   TABLE Run IF EXISTS;
    CREATE TABLE Run (
      runId       INTEGER GENERATED BY DEFAULT AS IDENTITY,
      distance    INTEGER,    -- in meters
      time        INTEGER,    -- in seconds
      venue       VARCHAR(64),
      when        TIMESTAMP,
      fkAthlete   INTEGER,
      CONSTRAINT fk FOREIGN KEY (fkAthlete)
        REFERENCES Athlete (athleteId) ON DELETE CASCADE
    );
    DROP   VIEW AthleteRun IF EXISTS;
    CREATE VIEW AthleteRun AS
      SELECT * FROM Athlete LEFT OUTER JOIN Run
        ON fkAthlete=athleteId;
  '''
sql.execute DLL_WITH_VIEW

def insertRun(h, m, s, venue, date, lastname) {
  def time = h * 60 * 60 + m * 60 + s
  sql.execute """
    INSERT INTO Run (distance, time, venue, when, fkAthlete)
      SELECT 42195, $time, $venue, $date,
        athleteId FROM Athlete WHERE lastname=$lastname;
  """
}

String athleteInsert = '''
INSERT INTO Athlete (firstname, lastname, dateOfBirth)
  VALUES (?,?,?);
'''
sql.execute athleteInsert, ['Paul', 'Tergat', '1969-06-17']
sql.execute athleteInsert, ['Khalid', 'Khannouchi', '1971-12-22']
sql.execute athleteInsert, ['Ronaldo', 'da Costa', '1970-06-07']

insertRun(2, 4, 55, 'Berlin', '2003-09-28', 'Tergat')
insertRun(2, 5, 38, 'London', '2002-04-14', 'Khannouchi')
insertRun(2, 5, 42, 'Chicago', '1999-10-24', 'Khannouchi')
insertRun(2, 6, 05, 'Berlin', '1998-09-20', 'da Costa')
println sql.updateCount

def athletes = sql.dataSet('Athlete')
athletes.each { println it.firstname }
athletes.add(
    firstname: 'Paula',
    lastname: 'Radcliffe',
    dateOfBirth: '1973-12-17'
)
athletes.each { println it.firstname }

def runs = sql.dataSet('AthleteRun').findAll { it.firstname == 'Khalid' }
runs.each { println "$it.lastname $it.venue" }

def query = athletes.findAll { it.firstname >= 'P' }
query = query.findAll { it.dateOfBirth > '1970-01-01' }
query = query.sort { it.dateOfBirth }
query = query.reverse()
println query.sql
println query.parameters
println query.rows()*.firstname
