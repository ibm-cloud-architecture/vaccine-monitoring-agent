package ibm.gse.eda.vaccine.coldchainagent.test;

import java.util.Collections;
import java.util.Map;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class KafkaResource implements QuarkusTestResourceLifecycleManager
{

    @Container
    private final GenericContainer<?> kafka = new GenericContainer("strimzi/kafka:latest-kafka-2.6.0").withExposedPorts(9092);

    @Override
    public Map<String, String> start() {
        kafka.start();
        return Collections.singletonMap("kafka.bootstrap.servers", kafka.getContainerIpAddress()+":9092");
    }

    @Override
    public void stop() {
        kafka.close();
    }
}