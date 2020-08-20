package ibm.gse.eda.vaccine.coldchainagent.domain;

import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.enterprise.inject.Produces;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KGroupedStream;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.Stores;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.processor.ProcessorContext;

import io.quarkus.kafka.client.serialization.JsonbSerde;

import ibm.gse.eda.vaccine.coldchainagent.infrastructure.ReeferEvent;
import ibm.gse.eda.vaccine.coldchainagent.infrastructure.TelemetryDeserializer;
import ibm.gse.eda.vaccine.coldchainagent.infrastructure.TelemetryEvent;
import ibm.gse.eda.vaccine.coldchainagent.infrastructure.scoring.ScoringResult;
import ibm.gse.eda.vaccine.coldchainagent.infrastructure.scoring.ScoringService;
import ibm.gse.eda.vaccine.coldchainagent.infrastructure.scoring.ScoringTelemetry;
import ibm.gse.eda.vaccine.coldchainagent.infrastructure.scoring.ScoringTelemetryWrapper;
import io.reactivex.Flowable;
import io.smallrye.reactive.messaging.kafka.KafkaRecord;
import org.eclipse.microprofile.reactive.messaging.Emitter;

/**
 * A bean consuming telemetry events from the "reefer-telemetry" Kafka topic and
 * applying following logic: - count if the temperature is above a specific
 * threshold for n events then the cold chain is violated. - call external
 * anomaly detection scoring service
 */
@ApplicationScoped
public class TelemetryAssessor {
    public final static String CONTAINER_TABLE = "containerTableInfo";
    protected static Logger LOG = Logger.getLogger(TelemetryAssessor.class);
    @ConfigProperty(name = "temperature.threshold")
    public double temperatureThreshold;

    @ConfigProperty(name = "temperature.max.occurence.count", defaultValue = "3")
    public int maxCount;

    @ConfigProperty(name = "prediction.enabled", defaultValue = "false")
    public boolean predictions_enabled;

    @ConfigProperty(name = "mp.messaging.incoming.reefer-telemetry.topic")
    public String reeferTelemetry;

    @Inject @Channel("telmetry-final") Emitter<KeyValue<String, Telemetry>> telEmitter;

    // @Inject
    // @RestClient
    // ScoringService scoringService;
    public String tableName = "containerTable";

    public int count;
    private boolean anomalyFound = false;

    public TelemetryAssessor() {
    }

    @Outgoing("reefer-telemetry-out")
    public Flowable<TelemetryEvent> generate() {
        System.out.println("Here:");
        return Flowable.interval(25, TimeUnit.SECONDS)
                .map(tick -> {
                    double random = new Random().nextDouble();
                    double result = 70 + (random * (130 - 70));
                    TelemetryEvent tel = new TelemetryEvent();
                    tel.containerID= "contId-1234";
                    tel.payload = new Telemetry();
                    tel.payload.temperature = result;
                    System.out.println(tel);
                    return tel;
                });
    }

    /**
     * 
     * @param message
     * @return
     */
    // @Incoming("reefer-telemetry")
    // @Outgoing("reefers")
    // @Acknowledgment(Acknowledgment.Strategy.MANUAL)
    // public PublisherBuilder<Message<ReeferEvent>> processTelemetryEvent(Message<TelemetryEvent> message) {
    //     // Get the message as String
    //     TelemetryEvent telemetryEvent = message.getPayload();
    //     LOG.info("Received message: " + telemetryEvent);
    //     // if (violateTemperatureThresholdOverTime(telemetryEvent)) {

    //     // }
    //     // query ktable

    //     if (predictions_enabled) {
    //         ScoringResult scoringResult = callAnomalyDetection(telemetryEvent.payload);
    //         int prediction = (int) scoringResult.getPredictions()[0].values[0][0];
    //         LOG.info("This is the prediction: " + prediction);
    //         LOG.info("with a probability: " + "[" + scoringResult.getPredictions()[0].values[0][1] + ","
    //                 + scoringResult.getPredictions()[0].values[0][1] + "]");
    //         // Is there anomaly?
    //         anomalyFound = (prediction == 0);
    //     } else {
    //         // Mockup the prediction
    //         int number = new Random().nextInt(10);
    //         if (number > 6)
    //             anomalyFound = true;
    //     }

    //     if (!anomalyFound) {
    //         message.ack(); // All processing of this message is done, ack it now
    //         return ReactiveStreams.empty();
    //     } else {
    //         LOG.info(
    //                 "A reefer anomaly has been predicted. Therefore, sending a ReeferAnomaly Event to the appropriate topic");
    //         ReeferEvent cae = new ReeferEvent(telemetryEvent.containerID, telemetryEvent.timestamp,
    //                 telemetryEvent.payload);
    //         LOG.info("Reefer Anomaly Event object sent: " + cae.toString());

    //         // This message will be sent on, create a new message which acknowledges the
    //         // incoming message when it is acked
    //         return ReactiveStreams.of(Message.of(cae));
    //     }
    // }

    public boolean violateTemperatureThresholdOverTime(TelemetryEvent telemetryEvent) {
        return true;
    }

    @Produces
    public Topology buildTopology() {
        StreamsBuilder builder = new StreamsBuilder();
        // 1- steam from kafka
        JsonbSerde<TelemetryEvent> telemetrySerde = new JsonbSerde<>(TelemetryEvent.class);
        JsonbSerde<ContainerTracker> containerSerde = new JsonbSerde<>(ContainerTracker.class);
        System.out.println("inside producer topology");
        KStream<String, TelemetryEvent> telemetryStream = builder.stream("refarcTopic", Consumed.with(Serdes.String(), telemetrySerde))
                .map((k, v) -> { 
                    System.out.println(k + " -> " + v);
                    if (v.payload != null){
                        System.out.println("Temperature: " + v.payload.temperature);
                        return new KeyValue<String, TelemetryEvent>(v.containerID, v);
                    }else {
                        return new KeyValue<String, TelemetryEvent>(v.containerID, null);
                    }
                });
        KGroupedStream<String, TelemetryEvent> telemetryGroup = telemetryStream.groupByKey(Grouped.with(Serdes.String(), telemetrySerde));
        KTable<String, ContainerTracker> containerTable = telemetryGroup.aggregate(
            () -> new ContainerTracker(maxCount,temperatureThreshold), 
            (k, v, aggValue) -> aggValue.update(v),
            Materialized.<String, ContainerTracker, KeyValueStore<Bytes, byte[]>>as(CONTAINER_TABLE)
            .withKeySerde(Serdes.String())
            .withValueSerde(containerSerde)     
        );
        containerTable.toStream().filter((k, v) -> v.isPreviousViolation()).foreach((k, v) -> {
            if (v.isViolatedWithLastTemp()){
                System.out.println("violated " + v.toString());
                System.out.println("Send Notification **************->>> or message to another topic. freshely violated container ");
                telEmitter.send(new KeyValue<String,Telemetry>(k, new Telemetry()));
            }
        });
        return builder.build();
    }
    // public ScoringResult callAnomalyDetection(Telemetry telemetry) {
    //     // todo compute last temperature diff
    //     ScoringTelemetry st = ScoringTelemetry.build(telemetry, 0);
    //     ScoringTelemetryWrapper wrapper = new ScoringTelemetryWrapper(st);
    //     return scoringService.assessTelemetry(wrapper);
    // }

    public double getTemperatureThreshold() {
        return temperatureThreshold;
    }

    public double getMaxCount() {
        return maxCount;
    }
}
