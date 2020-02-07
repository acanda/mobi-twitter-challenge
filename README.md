# Mobi Twitter Challenge

## Build

Build everything with `mvn clean package`. You have to do this at least once
before starting the docker containers.


## Run

Start everything with `docker-compose up -d --build`. You need access to the
Mobi Docker registry.

You can follow the logs of individual containers with 
`docker logs -f dtm-kafka-connect-service`.

The following UIs are available:
- Kafka Topics UI: http://localhost:8000
- Kafka Schema Registry UI: http://localhost:8001
- Kafka Connect UI: http://localhost:8003
- GraphDB: http://localhost:7200

Stop everything with `docker-compose down`.


## Setup

### GraphDB

Open `http://localhost:7200/repository` and create a repository with repository
id `twitter-status` (keep the default values for all other properties).

### Kafka Connect

Open `http://localhost:8003/` and create a new TwitterSourceConnector:
```properties
name=TwitterSourceConnector
connector.class=com.github.jcustenborder.kafka.connect.twitter.TwitterSourceConnector
tasks.max=1
process.deletes=false
filter.keywords=the
kafka.status.topic=twitter-status
twitter.oauth.accessToken=...
twitter.oauth.accessTokenSecret=...
twitter.oauth.consumerSecret=...
twitter.oauth.consumerKey=...
```

Then create a new SparqlSinkConnector:
```properties
name=SparqlSinkConnector
connector.class=ch.mobi.emme.twitter.sparql.sink.SparqlSinkConnector
tasks.max=1
repository.host=http://graphdb:7200
repository.name=twitter-status
topics=twitter-status
```

This will start a stream of Twitter status records that are stored to GraphDB.
