# Reefer Monitoring Agent

The main documentation for this project (the what, why, how) is part of the Vaccine project and can be [read here](https://ibm-cloud-architecture.github.io/vaccine-solution-main/solution/cold-monitoring/).

This project uses the following technologies to support consuming refrigerator or freezer IoT units and assess temperature / cold chain violation or / and sensor anomaly detection.

* Event Streams on Cloud Pack for Integration
* Quarkus 1.7.1+
* Reactive Messaging
* Kafka Streams, and Ktable with interactive queries

The component interacts with other components as highlighted in the figure below:

 ![1](https://ibm-cloud-architecture.github.io/vaccine-solution-main/static/592098bb7c68d4abdb6525813e4be606/3cbba/cold-monitoring-1.png)

## Pre-requisites

* JDK 11 or later is installed.
* Apache Maven 3.6.2 or later is installed.
* Appsody CLI (tested on 0.6.4)
* Access to an Openshift Cluster 4.4
* Event Streams installed or Strimzi operator deployed with a vanilla kafka 2.5 cluster
* A kafka user exists with administration role. (See [Event Streams documentation](https://ibm.github.io/event-streams/security/managing-access/#assigning-access-to-applications) for that)

## Running locally

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

* Define the KAFKA_BROKERS env variable in `scripts/appsody.env` from the bootstrap address retrieved before.

* Select one of the kafka users defined

```shell
oc get kafkausers -n eventstreams
```

* Define the KAFKA_USER variable (in scripts/appsody.env) with one of the user and KAFKA_PASSWORD with the user's password extracted from his secret:

```
oc get secret <username> --namespace eventstreams -o jsonpath='{.data.password}' | base64 --decode
```

* Then get the TLS certificate with the command:

```shell
cloudctl es certificates --format p12
# get the truststore password and the .p12 file
# mv the certificate
mv es-cert.p12 certs
```

The cluster public certificate is required for all external connections and is available to download from the Cluster connection panel under the Certificates heading. Upon downloading the PKCS12 certificate, the certificate password will also be displayed.

Modify KAFKA_CERT_PWD in the `scripts/appsody.env` file.

* Start appsody run with the environment variables, so the quarkus kafka app is remotely connected to Event Streams.

```
appsody run --docker-options "--env-file ./scripts/appsody.env -v $(pwd)/certs:/deployment/certs"
```

## Deploy to OpenShift

* Build and push the image to private or public registry.

```shell
# if not logged yes to your openshift cluster where the docker private registry resides do:
oc login --token=... --server=https://c...
# Get the route to reach the docker private registry
oc get route --all-namespaces | grep registry
# Define the path as environment variable
export IMAGE_REGISTRY=default-route-openshift-image-registry.gse-eda-demo-202005-fa9ee67c9ab6a7791435450358e564cc-0000.us-south.containers.appdomain.cloud
# log to the docker registry using the security token from the openshift console
docker login $IMAGE_REGISTRY
# Then build and push the image
appsody build -t vaccine-solution/vaccine-monitoring-agent:0.0.1 [--push-url $IMAGE_REGISTRY] [--push]
 appsody build -t ibmcase/vaccine-monitoring-agent:0.0.1 --push
```

* Define config map

```
oc apply -f src/main/kubernetes/configmap.yaml
```

* Select one of the kafka users defined or create a new one with the produce, consume messages and create topic and schemas authorizations, on all topics or topic with a specific prefix, on all consumer groups or again with a specific prefix, all transaction IDs.

```shell
oc get kafkausers -n eventstreams
```

Get username and then to get the password do the following:

```shell
oc get secret <username>  -o jsonpath='{.data.password}' | base64 --decode
```

Modify the KAFKA_USER and KAFKA_PASSWORD variables in the `scripts/appsody.env` file.

* Copy user's secret

```
oc get secret jesus -n eventstreams --export -o yaml | oc apply -n vaccine-solution -f -
```

The cluster Truststore certificate is required for all external connections and is available to download from the Cluster connection panel under the Certificates heading. Upon downloading the PKCS12 certificate, the certificate password will also be displayed.

Modify KAFKA_CERT_PWD in the `scripts/appsody.env` file.

* Remove the "%prod." in the application.properties for the kafka settings. These were set to run with Kafka running with docker compose, but when remote connected to Event Streams we need those settings.

* Start the app with appsody using the environment variables and SSL certificate

```shell
appsody run --docker-options "--env-file ./scripts/appsody.env -v $(pwd)/certs:/deployment/certs"
```

## Running on OpenShift with Event Streams co-located in same cluster

To run on OpenShift, you will need to inject the address of your Kafka settings into your Quarkus application via the environment variables and mount points. The `app-deploy.yaml` file contains declarations to inject the required information at runtime.

There are multiple required configuration elements for connectivity to IBM Event Streams (Kafka) prior to deployment:
  - A KafkaUser with TLS-based authentication credentials
  - A `ConfigMap` named `agent-cm` containing the following key-value pairs:
    -  `kafka-brokers`
    -  `reefer-topic`
    -  `telemetry-topic` - _(this topic should match the output topic of the [vaccine-reefer-simulator](https://github.com/ibm-cloud-architecture/vaccine-reefer-simulator))_
  - A `Secret` copied from the `eventstreams` namespace containing the KafkaUser's TLS credentials
  - A `Secret` copied from the `eventstreams` namespace containing the Event Streams' clusters certificates

### Create a TLS User

* Create a TLS user for the internal bootstrap endpoint of the Event Streams instance via the **"Connect to this cluster"** dialog in the Event Streams console.

* Validate the user is created and available via the OpenShift CLI
```shell
oc get kafkausers -n eventstreams
# we will use kafka-tls-user as it is a TLS user.
kafka-scram-user                         scram-sha-512    simple
kafka-tls-user                           tls              simple
```

### Get Cluster bootstrap URL

* Get the cluster's bootstrap URL via the **"Connect to this cluster"** dialog in the Event Streams console. It is in the format of `{cluster-name}-bootstrap.eventstreams.svc:9093` with `{cluster-name}` being replaced with the actual name of your Event Streams cluster.

### Create ConfigMap

A sample configuration command for `agent-cm` ConfigMap is included below:

```
oc create configmap agent-cm \
--from-literal=kafka-brokers={cluster-name}-kafka-bootstrap.eventstreams.svc:9093 \
--from-literal=reefer-topic=vaccine-reefers \
--from-literal=telemetries-topic=vaccine-reefer-telemetries
```

Replace the values for `kafka-brokers`, `reefer-topic`, and `telemetry-topic` to the values that match your environment.

### Copy Event Streams credentials

* Copy the user's TLS credentials from the Event Streams project to the target project:

```shell
oc get secret {kafka-user} -n eventstreams --export -o yaml | oc apply -n {target-namespace} -f -
```

* Copy the Kafka cluster's public certificate from the Event Streams project to the target project _(This certificate includes a ca.pa12 entry and a truststore password which will be used in the deployment manifest.)_:

```shell
oc get secret {cluster-name}-cluster-ca-cert -n eventstreams --export -o yaml | oc apply -n {target-namespace} -f -
```

### Update app-deploy.yaml

Sections of the `app-deploy.yaml` need to be updated to correspond to your locally copied Secrets from the `eventstreams` namespace.

* Modify the **name** of the Secrets referenced for the environment variables declarations in `app-deploy.yaml`:

```yaml
- name: KAFKA_CERT_PWD
  valueFrom:
    secretKeyRef:
      key: ca.password
      name: {cluster-name}-cluster-ca-cert
...
- name: USER_CERT_PWD
  valueFrom:
    secretKeyRef:
      key: user.password
      name: {kafka-user}
```

* Modify the **secretName** of the Secrets referenced in the `volumes` declaration of the `app-deploy.yaml`.

```yaml
volumes:
- name: es-cert
  secret:
    secretName: {cluster-name}-cluster-ca-cert
- name: user-cert
  secret:
    secretName: {kafka-user}
```

### Deploy the Appsody application

```shell
appsody deploy -t ibmcase/reefer-monitoring-agent:latest --no-build --namespace {target-namespace}
```

If you want to remove the deployment:

```shell
oc delete app-deploy.yaml
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
