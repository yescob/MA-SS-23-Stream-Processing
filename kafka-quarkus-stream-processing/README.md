Quarkus Kafka Streams
========================

## Anatomy

This quickstart is made up of the following parts:

* Apache Kafka and ZooKeeper
* _producer_, a Quarkus application that publishes some test data on two Kafka topics: `weather-stations` and `temperature-values`
* _aggregator_, a Quarkus application processing the two topics, using the Kafka Streams API

The _aggregator_ application is the interesting piece; it

* runs a KStreams pipeline, that joins the two topics (on the weather station id),
groups the values by weather station and emits the minimum/maximum temperature value per station to the `temperatures-aggregated` topic
* exposes an HTTP endpoint for getting the current minimum/maximum values
for a given station using Kafka Streams interactive queries.

## Building

Requires Java 11.

To build the _producer_ and _aggregator_ applications, run

```bash
cd aggregator
./gradle build
cd ..
cd producer
./gradle build
```

## Running

A Docker Compose file is provided for running all the components.
Start all containers by running:

```bash
docker-compose up -d --build
```

Now run an instance of the _debezium/tooling_ image which comes with several useful tools such as _kafkacat_ and _httpie_:

```bash
docker run --tty --rm -i --network ks debezium/tooling:1.1
```

In the tooling container, run _kafkacat_ to examine the results of the streaming pipeline:

```bash
kafkacat -b kafka:9092 -C -o beginning -q -t temperatures-aggregated
```

## Running locally

For development purposes it can be handy to run the _producer_ and _aggregator_ applications
directly on your local machine instead of via Docker.
For that purpose, a separate Docker Compose file is provided which just starts Apache Kafka and ZooKeeper, _docker-compose-local.yaml_
configured to be accessible from your host system.
Open this file an editor and change the value of the `KAFKA_ADVERTISED_LISTENERS` variable so it contains your host machine's name or ip address. Additionaly, in the /src/main/resources/application.properties file of each project the
`kafka.bootstrap.servers=localhost:9092` needs to be set.

```bash
docker-compose -f docker-compose-local.yaml up

cd aggregator
./gradlew quarkusDev

cd producer
./gradlew quarkusDev
```
Optionaly you can skip the first step and use the DevService provided by Quarkus to automatically start and stop a
Kafka container in dev mode. (Needs running DockerHub)

Any changes done to the _aggregator_ application will be picked up instantly,
and a reload of the stream processing application will be triggered upon the next Kafka message to be processed.

# Updating Quarkus application in Docker Compose

```bash
./gradlew build
docker-compose stop <servicename>
docker-compose up --build -d
```

# Diagram Documetation

For documentation reasons we have created Diagrams with structurizer. 
To view these diagrams, the docker compose file in `kafka-quarkus-stream-processing/diagrams/docker-compose.yaml` needs to be executed.

When this file is running, the application can be accessed on `localhost:8091`.
