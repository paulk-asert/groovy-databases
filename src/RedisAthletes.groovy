@Grab('redis.clients:jedis:2.6.0')
import redis.clients.jedis.Jedis
import groovy.json.JsonSlurper

//def redisHost = 'localhost'
def redisHost = '192.168.59.104'
def jedis = new Jedis(redisHost)
jedis.del('athlete')
def athleteId = jedis.incr('athlete')
def athleteKey = "athlete:$athleteId"
jedis.hmset(athleteKey, [first: 'Paul', last: 'Tergat', dob: '1969-06-17'])

jedis.del('record')
def recordKey = "record:${jedis.incr('record')}"
jedis.hmset(recordKey, [time: '' + 2 * 60 * 60 + 4 * 60 + 55,
                        venue: 'Berlin', when: '2003-09-28'])

jedis.del("set:$athleteId")
jedis.sadd("set:$athleteId", recordKey)

def insertAthlete(jedis, first, last, dob) {
    def athleteId = jedis.incr('athlete')
    jedis.hmset("athlete:$athleteId", [first: first, last: last, dob: dob])
    jedis.del("set:$athleteId")
    athleteId
}

def insertRecord(jedis, h, m, s, venue, date, athleteId) {
    def recordKey = "record:${jedis.incr('record')}"
    jedis.hmset(recordKey,
            [time: '' + h * 60 * 60 + m * 60 + s, venue: venue,
             when: date, athlete: '' + athleteId])
    jedis.sadd("set:$athleteId", recordKey)
}

def id = insertAthlete(jedis, 'Khalid', 'Khannouchi', '1971-12-22')
insertRecord(jedis, 2, 5, 38, 'London', '2002-04-14', id)
insertRecord(jedis, 2, 5, 42, 'Chicago', '1999-10-24', id)
id = insertAthlete(jedis, 'Ronaldo', 'da Costa', '1970-06-07')
insertRecord(jedis, 2, 6, 05, 'Berlin', '1998-09-20', id)

def radcliffe = """{
    "first": "Paula",
    "last": "Radcliffe",
    "dob": "1973-12-17",
    "records": [
        {"hour": 2, "min": 15, "sec": 25,
        "venue": "London", "when": "2003-04-13"}
    ]
}"""
def parsed = new JsonSlurper().parseText(radcliffe)

id = insertAthlete(jedis, parsed.first, parsed.last, parsed.dob)
parsed.records.each {
    insertRecord(jedis, it.hour, it.min, it.sec, it.venue, it.when, id)
}

assert jedis.keys('athlete:*').size() == 4
assert jedis.keys('record:*').size() == 5

jedis.keys('record:*').each {
    jedis.sadd('venue:' + jedis.hmget(it, 'venue')[0], it)
}

def londonRecords = jedis.smembers('venue:London')

assert londonRecords.collect{
    jedis.hmget("athlete:${jedis.hmget(it, 'athlete')[0]}", 'first')[0]
} == ['Khalid', 'Paula']
