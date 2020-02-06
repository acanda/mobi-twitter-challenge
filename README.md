# Mobi Twitter Challenge

## Run

Start everything with `docker-compose up -d --build`. You need access to the
Mobi Docker registry.

You can follow the logs of individual containers with 
`docker logs -f dtm-kafka-connect-service`.

The following UIs are available:
Kafka Topics UI: http://localhost:8000
Kafka Schema Registry UI: http://localhost:8001
Kafka Connect UI: http://localhost:8003
GraphDB: http://localhost:7200

Stop everything with `docker-compose down`.
