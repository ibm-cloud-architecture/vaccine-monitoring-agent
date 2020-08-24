
package ibm.gse.eda.vaccine.coldchainagent.test;



import java.util.ArrayList;
import java.util.Properties;

import javax.inject.Inject;
import javax.validation.constraints.AssertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import ibm.gse.eda.vaccine.coldchainagent.domain.ContainerTracker;
import ibm.gse.eda.vaccine.coldchainagent.domain.Telemetry;
import ibm.gse.eda.vaccine.coldchainagent.domain.TelemetryAssessor;
import ibm.gse.eda.vaccine.coldchainagent.infrastructure.TelemetryEvent;
import io.quarkus.kafka.client.serialization.JsonbSerde;
import io.quarkus.test.junit.QuarkusTest;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;


@QuarkusTest
public class TelemetryColdChainTest {

    private static TopologyTestDriver testDriver;

    private TestInputTopic<String, TelemetryEvent> inputTopic;

    private Serde<String> stringSerde = Serdes.String();
    private double temperatureThreshold = 100.0;
    private String streamTopic = "stream-refer-topic";
    private int maxCount = 5;
    private String tableName = TelemetryAssessor.CONTAINER_TABLE;

    
    private JsonbSerde<TelemetryEvent> telemetrySerde = new JsonbSerde<>(TelemetryEvent.class);
    private JsonbSerde<ContainerTracker> containerSerde = new JsonbSerde<>(ContainerTracker.class);
    
    private  Properties getStreamsConfig() {
        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "telemetry-tester");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummmy:1234");
        return props;
    }


    /**
     * process item sale events, and aggregate per key
     */
    @BeforeEach
    public void setup() {
        // as no CDI is used set the topic names
        TelemetryAssessor telemetryAssessor = new TelemetryAssessor(temperatureThreshold, streamTopic, maxCount, tableName);
        Topology topology = telemetryAssessor.buildTopology();
        testDriver = new TopologyTestDriver(topology, getStreamsConfig());
        inputTopic = testDriver.createInputTopic(streamTopic, 
                                stringSerde.serializer(),
                                telemetrySerde.serializer());
        
    }

    @AfterEach
    public void tearDown() {
        try {
            testDriver.close();
        } catch (final Exception e) {
             System.out.println("Ignoring exception, test failing due this exception:" + e.getLocalizedMessage());
        } 
    }

    private TelemetryEvent generateTelemetryEvent(double temperature) {
        TelemetryEvent tel = new TelemetryEvent();
        tel.containerID= "contId-1234";
        tel.payload = new Telemetry();
        tel.payload.temperature = temperature;
        return tel;
    }

    @Test
    @Order(1)
    public void violatedTemepratureCountTest(){
        ReadOnlyKeyValueStore<String,ContainerTracker> storage = testDriver.getKeyValueStore(TelemetryAssessor.CONTAINER_TABLE);

        TelemetryEvent telemetryEvent = generateTelemetryEvent(this.temperatureThreshold+ 1.0);
        // send message to topic
        inputTopic.pipeInput(telemetryEvent);
        // get data from ktable
        ContainerTracker containerTracker = storage.get("contId-1234");
         // validation
        Assertions.assertEquals(1, containerTracker.getViolatedTemperatureCount());
        Assertions.assertEquals(1, containerTracker.getTemperatureList().size());

        // sending another event with temperature greater than max temperature
        telemetryEvent = generateTelemetryEvent(this.temperatureThreshold+ 50);
        // send message to topic
        inputTopic.pipeInput(telemetryEvent);
        // get data from ktable
        containerTracker = storage.get("contId-1234");
        // validation
        Assertions.assertEquals(2, containerTracker.getViolatedTemperatureCount());
        Assertions.assertEquals(2, containerTracker.getTemperatureList().size());

        // generate event with temperature lesser than max temperature
        telemetryEvent = generateTelemetryEvent(temperatureThreshold-1.0);
        // send message to topic
        inputTopic.pipeInput(telemetryEvent);
        // get data from ktable
        containerTracker = storage.get("contId-1234");
        // validation
        Assertions.assertEquals(0, containerTracker.getViolatedTemperatureCount());
        Assertions.assertEquals(0, containerTracker.getTemperatureList().size());

        // sending another event with temperature greater than max temperature
        telemetryEvent = generateTelemetryEvent(temperatureThreshold+1.0);
        // send message to topic
        inputTopic.pipeInput(telemetryEvent);
        // get data from ktable
        containerTracker = storage.get("contId-1234");
        // validation
        Assertions.assertEquals(1, containerTracker.getViolatedTemperatureCount());
        Assertions.assertEquals(1, containerTracker.getTemperatureList().size());
    }


    @Test
    @Order(2)
    public void violatedContainerTest() {
        ReadOnlyKeyValueStore<String,ContainerTracker> storage = testDriver.getKeyValueStore(TelemetryAssessor.CONTAINER_TABLE);
        // send max maxCount event with temperature greater than maxtemperatureThreshold
        ArrayList<Double> tempList = new ArrayList<>();
        ContainerTracker containerTracker;
        for (int i=1; i < this.maxCount + 1; i++){
            double temp = this.temperatureThreshold + i;
            tempList.add(temp);
            TelemetryEvent telemetryEvent = generateTelemetryEvent(temp);
            // send message to topic
            inputTopic.pipeInput(telemetryEvent);
            // get data from ktable
            containerTracker = storage.get("contId-1234");
            // validation
            Assertions.assertEquals(i, containerTracker.getViolatedTemperatureCount());
            Assertions.assertEquals(i, containerTracker.getTemperatureList().size());
            Assertions.assertArrayEquals(tempList.toArray(), containerTracker.getTemperatureList().toArray());
        }
        // violation should occur with last temperature
        containerTracker = storage.get("contId-1234");
        Assertions.assertEquals(true, containerTracker.isViolatedWithLastTemp());
        Assertions.assertEquals(true, containerTracker.isPreviousViolation());
        
        // should change isviolatedwithlastTemp to false
        TelemetryEvent telemetryEvent = generateTelemetryEvent(temperatureThreshold + 1);
        // send message to topic
        inputTopic.pipeInput(telemetryEvent);
        containerTracker = storage.get("contId-1234");
        Assertions.assertEquals(false, containerTracker.isViolatedWithLastTemp());
        Assertions.assertEquals(true, containerTracker.isPreviousViolation());
        
        // should not change violation information by sending temperature less than threshold
        telemetryEvent = generateTelemetryEvent(temperatureThreshold - 10);
        // send message to topic
        inputTopic.pipeInput(telemetryEvent);
        containerTracker = storage.get("contId-1234");
        Assertions.assertEquals(false, containerTracker.isViolatedWithLastTemp());
        Assertions.assertEquals(true, containerTracker.isPreviousViolation());

    }

}