
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

import ibm.gse.eda.vaccine.coldchainagent.domain.ReeferAggregate;
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
    private double temperatureThreshold = 0.0;
    private String telemetryTopic = "telemetries";
    private int maxCount = 5;
   
    
    private JsonbSerde<TelemetryEvent> telemetrySerde = new JsonbSerde<>(TelemetryEvent.class);
    private JsonbSerde<ReeferAggregate> containerSerde = new JsonbSerde<>(ReeferAggregate.class);
    
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
        TelemetryAssessor telemetryAssessor = new TelemetryAssessor(temperatureThreshold, telemetryTopic, maxCount);
        Topology topology = telemetryAssessor.buildTopology();
        testDriver = new TopologyTestDriver(topology, getStreamsConfig());
        inputTopic = testDriver.createInputTopic(telemetryTopic, 
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

    private TelemetryEvent generateTelemetryEventForKey(String key,double temperature) {
        TelemetryEvent tel = new TelemetryEvent();
        tel.containerID= key;
        tel.payload = new Telemetry();
        tel.payload.temperature = temperature;
        return tel;
    }

    @Test
    @Order(1)
    public void shouldHaveTemperaturesGrowButGoDownAgain(){
        ReadOnlyKeyValueStore<String,ReeferAggregate> storage = testDriver.getKeyValueStore(TelemetryAssessor.REEFER_AGGREGATE_TABLE);
        String reeferID = "contId-1234";
        TelemetryEvent telemetryEvent = generateTelemetryEventForKey(reeferID,this.temperatureThreshold+ 1.0);
        // send message to topic
        inputTopic.pipeInput(telemetryEvent);
        // get data from ktable
        ReeferAggregate reeferAggregate = storage.get(reeferID);
         // validation
        Assertions.assertEquals(1, reeferAggregate.getViolatedTemperatureCount());
        Assertions.assertEquals(1, reeferAggregate.getTemperatureList().size());

        // sending another event with temperature greater than max temperature
        telemetryEvent = generateTelemetryEventForKey(reeferID,this.temperatureThreshold+ 5);
        // send message to topic
        inputTopic.pipeInput(telemetryEvent);
        // get data from ktable
        reeferAggregate = storage.get(reeferID);
        // validation
        Assertions.assertEquals(2, reeferAggregate.getViolatedTemperatureCount());
        Assertions.assertEquals(2, reeferAggregate.getTemperatureList().size());

        // generate event with temperature lesser than max temperature
        telemetryEvent = generateTelemetryEventForKey(reeferID,temperatureThreshold-1.0);
        // send message to topic
        inputTopic.pipeInput(telemetryEvent);
        // get data from ktable
        reeferAggregate = storage.get(reeferID);
        // validation
        Assertions.assertEquals(0, reeferAggregate.getViolatedTemperatureCount());
        Assertions.assertEquals(0, reeferAggregate.getTemperatureList().size());

        // sending another event with temperature greater than max temperature
        telemetryEvent = generateTelemetryEventForKey(reeferID,temperatureThreshold+1.0);
        // send message to topic
        inputTopic.pipeInput(telemetryEvent);
        // get data from ktable
        reeferAggregate = storage.get(reeferID);
        // validation
        Assertions.assertEquals(1, reeferAggregate.getViolatedTemperatureCount());
        Assertions.assertEquals(1, reeferAggregate.getTemperatureList().size());
    }


    @Test
    @Order(2)
    public void shouldHaveTooManyViolations() {
        ReadOnlyKeyValueStore<String,ReeferAggregate> storage = testDriver.getKeyValueStore(TelemetryAssessor.REEFER_AGGREGATE_TABLE);
        String reeferID = "contId-1234";
        // send max maxCount event with temperature greater than maxtemperatureThreshold
        ReeferAggregate reeferAggregate;
        for (int i=1; i < this.maxCount + 1; i++){
            double temp = this.temperatureThreshold + i;
            TelemetryEvent telemetryEvent = generateTelemetryEventForKey(reeferID,temp);
            // send message to topic
            inputTopic.pipeInput(telemetryEvent);
            // get data from ktable
            reeferAggregate = storage.get(reeferID);
            // validation
            Assertions.assertEquals(i, reeferAggregate.getViolatedTemperatureCount());
        }
        // violation should occur with last temperature
        reeferAggregate = storage.get(reeferID);
        Assertions.assertEquals(true, reeferAggregate.hasTooManyViolations());
        
        // should change isviolatedwithlastTemp to false
        TelemetryEvent telemetryEvent = generateTelemetryEventForKey(reeferID,temperatureThreshold + 1);
        // send message to topic
        inputTopic.pipeInput(telemetryEvent);
        reeferAggregate = storage.get(reeferID);
        Assertions.assertEquals(true, reeferAggregate.hasTooManyViolations());
        
        // should not change violation information by sending temperature less than threshold
        telemetryEvent = generateTelemetryEventForKey(reeferID,temperatureThreshold - 10);
        // send message to topic
        inputTopic.pipeInput(telemetryEvent);
        reeferAggregate = storage.get(reeferID);
        Assertions.assertEquals(true, reeferAggregate.hasTooManyViolations());

    }

}