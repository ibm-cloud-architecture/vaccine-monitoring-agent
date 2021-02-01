# Reefer Monitoring Agent

The main documentation for this project (the what, why, how) is part of the Vaccine project and can be [read here](https://ibm-cloud-architecture.github.io/vaccine-solution-main/solution/cold-monitoring/).

This project uses the following technologies to support consuming refrigerator or freezer IoT units and assess temperature / cold chain violation or / and sensor anomaly detection.

* Event Streams on Cloud Pack for Integration
* Quarkus 1.11.0
* Reactive Messaging
* Kafka Streams, and Ktable with interactive queries

The component interacts with other components as highlighted in the figure below:

 ![1](https://ibm-cloud-architecture.github.io/vaccine-solution-main/static/592098bb7c68d4abdb6525813e4be606/3cbba/cold-monitoring-1.png)

## Pre-requisites

* JDK 11 or later is installed.
* Apache Maven 3.6.2 or later is installed
* Access to an OpenShift Cluster 4.4
* Event Streams installed or Strimzi operator deployed with a vanilla kKafka 2.6 cluster
* A kKafka user exists with administration role. (See [Event Streams documentation](https://ibm.github.io/event-streams/security/managing-access/#assigning-access-to-applications) for that)

## Running locally

**THIS WAS CHANGED THE LAST THREE DAYS 01/30/21. So not working yet**

As it is quite simple to connect to Event Streams running on OpenShift cluster, we are using the settings to run the quarkus app in dev mode, but remotely connected to Event Streams.

* Connect CLI to Event Streams:

```shell
oc login
cloudctl es init

Select an instance:
1. minimal-prod ( Namespace:eventstreams )
2. sandbox-rp ( Namespace:eventstreams )
Enter a number> 1
```

Get the `Event Streams bootstrap address`.

* Define the the following environment variables in `.env`

```shell
export POD_IP=localhost
export CP4D_USER=<user cloud pak for data>
export CP4D_APIKEY=<api key for cloud pak for data>
export CP4D_AUTH_URL=<url of cloud pak for data>/icp4d-api/v1/authorize
export ANOMALY_DETECTION_URL=<>

export KAFKA_USER=<kafka scram user>
export KAFKA_PASSWORD=<kafka user password>
export KAFKA_BOOTSTRAP_SERVERS=eda-dev-kafka-bootstrap-eventstreams.gse-eda-2021-1-0143c5dd31acd8e030a1d6e0ab1380e3-0000.us-east.containers.appdomain.cloud:443
export KAFKA_SSL_TRUSTSTORE_LOCATION=${PWD}/certs/es-cert.p12
export KAFKA_SSL_TRUSTSTORE_PASSWORD=<>
export TELEMETRY_TOPIC=coldchain-telemetries
export REEFER_TOPIC=coldchain-reefers
export PREDICTION_ENABLED=False
export EDA_LOGGING_LEVEL=INFO
export KAFKA_SASL_MECHANISM=
```


* Then get the TLS certificate with the command:

```shell
cloudctl es certificates --format p12
# get the truststore password and the .p12 file
# mv the certificate
mv es-cert.p12 certs
```

The cluster public certificate is required for all external connections and is available to download from the Cluster connection panel under the Certificates heading. Upon downloading the PKCS12 certificate, the certificate password will also be displayed.

## Deploy to OpenShift

* Build and push the image to private or public registry.

```shell
# if not logged yes to your openshift cluster where the docker private registry resides do:
oc login --token=... --server=https://c...
mvn clean package  -Dquarkus.kubernetes.deploy=true -DskipTests
```


## Running locally with docker compose

The project has a simple `docker-compose.yaml` which you can use to run a single-node Kafka cluster on your local machine. To start Kafka locally, run `docker-compose up`. This will start Kafka, Zookeeper, and also create a Docker network on your machine, which you can find the name of by running `docker network list`.

To run the application using Appsody, use this command, substituting in the name of your Docker network:

`$ appsody run --network network_name --docker-options "--env KAFKA_BROKERS=kafka:9092"`

To shut down Kafka and Zookeeper afterwards, run `docker-compose down`.

now you can goto http://localhost:8080/ktable/{containerId}
or http://localhost:8080/ktable to view all ktable
and see what is in ktable


### Running locally with multiple instance

`$ docker-compose -f docker-compose-code.yaml up --scale vaccinemonotoringagent=5`

now you can access your endpoint using

`http://localhost:4000/reefer-tracker/`
`http://localhost:4000/reefer-tracker/data/{reeferID}`
`http://localhost:4000/reefer-tracker/meta-data`

you should get request from one of the docker instance running
