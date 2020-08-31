# Reefer Monitoring Event Driven Service

The main documentation is part of the Vaccine project and can be [read here](https://pages.github.ibm.com/vaccine-cold-chain/vaccine-cold-chain-main/solution/cold-monitoring/).


## Running locally

The template provides a sample `docker-compose.yaml` which you can use to run a single-node Kafka cluster on your local machine. To start Kafka locally, run `docker-compose up`. This will start Kafka, Zookeeper, and also create a Docker network on your machine, which you can find the name of by running `docker network list`.

To run the application using Appsody, use this command, substituting in the name of your Docker network:

`$ appsody run --network network_name --docker-options "--env KAFKA_BOOTSTRAP_SERVERS=kafka:9092"`

To shut down Kafka and Zookeeper afterwards, run `docker-compose down`.

now you can goto http://localhost:8080/ktable/{containerId}
or http://localhost:8080/ktable to view all ktable
and see what is in ktable

## Running on Kubernetes

To run on Kubernetes, you will first need to deploy Kafka. The easiest way to do that is to use the [Strimzi operator quickstart](https://strimzi.io/quickstarts/). This will deploy a basic Kafka cluster into a `kafka` namespace on your Kubernetes cluster.

You will need to inject the address of your Kafka into your Quarkus application via the `KAFKA_BOOTSTRAP_SERVERS` environment variable. To do this, you can add an `env:` section to your `app-deploy.yaml` that is generated when you run `appsody build`.

```yaml
spec:
  env:
  - name: KAFKA_BOOTSTRAP_SERVERS
    value: my-cluster-kafka-bootstrap.default.svc:9092
```

To get the `value` you need, you can run `kubectl describe kafka -n kafka` and examine the listener address in the status section.

Once you have updated your `app-deploy.yaml` to inject the environment variable, you can run `appsody deploy` to run your Quarkus application on Kubernetes.


# Running locally with multiple instance 
`$ docker-compose -f docker-compose-code.yaml up --scale vaccinemonotoringagent=5`

now you can access your endpoint using

`http://localhost:4000/reefer-tracker/`
`http://localhost:4000/reefer-tracker/data/{reeferID}`
`http://localhost:4000/reefer-tracker/meta-data`

you should get request from one of the docker instance running