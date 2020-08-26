package ibm.gse.eda.vaccine.coldchainagent.domain;

import java.sql.Timestamp;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.enterprise.inject.Produces;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.jboss.logging.Logger;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KGroupedStream;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.KeyValueStore;

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
import org.eclipse.microprofile.reactive.messaging.Outgoing;

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

    @Inject
    @ConfigProperty(name = "temperature.threshold")
    public double temperatureThreshold;

    @Inject
    @ConfigProperty(name = "quarkus.kafka-streams.topics", defaultValue = "testTopic")
    public String streamTopic;

    @Inject
    @ConfigProperty(name = "temperature.max.occurence.count", defaultValue = "3")
    public int maxCount;

    @Inject
    @ConfigProperty(name = "prediction.enabled", defaultValue = "false")
    public boolean predictions_enabled;

    @Inject
    @ConfigProperty(name = "mp.messaging.incoming.reefer-telemetry.topic")
    public String reeferTelemetry;

    @Inject @Channel("telmetryreefer") Emitter<KeyValue<String, ReeferEvent>> telEmitter;

    public String tableName = "containerTable";

    // @Inject
    // @RestClient
    // ScoringService scoringService;

    public int count;
    private boolean anomalyFound = false;

    public TelemetryAssessor() {
        
    }


    public boolean violateTemperatureThresholdOverTime(TelemetryEvent telemetryEvent) {
        return true;
    }

    @Produces
    public Topology buildTopology() {
        StreamsBuilder builder = new StreamsBuilder();
        
        JsonbSerde<TelemetryEvent> telemetryEventSerde = new JsonbSerde<>(TelemetryEvent.class);
        JsonbSerde<ContainerTracker> containerSerde = new JsonbSerde<>(ContainerTracker.class);
        // from original message create stream with key containerID and value as it is
        // 1- steam from kafka topic streamTopic and deserialize with key as string and value ad TelemetryEvent
        KStream<String, TelemetryEvent> telemetryStream = builder.stream(streamTopic, Consumed.with(Serdes.String(), telemetryEventSerde))
                .map((k, v) -> { 
                    System.out.println(k + " -> " + v);
                    if (v.payload != null){
                        System.out.println("Temperature: " + v.payload.temperature);
                        return new KeyValue<String, TelemetryEvent>(v.containerID, v);
                    }else {
                        return new KeyValue<String, TelemetryEvent>(v.containerID, null);
                    }
                });
        // for each message call anomaly detector
        // telemetryStream.peek(( k,telemetryEvent ) -> {
        //     anomalyDetector(k, telemetryEvent);
        // });

        // group stream by key and serialized with key as string and value ad TelemetryEvent
        KGroupedStream<String, TelemetryEvent> telemetryGroup = telemetryStream.groupByKey(Grouped.with(Serdes.String(), telemetryEventSerde));
        // create table with store as containerTable
        KTable<String, ContainerTracker> containerTable = telemetryGroup.aggregate(
            () -> new ContainerTracker(maxCount,temperatureThreshold), 
            (k, v, aggValue) -> aggValue.update(k , v),
            Materialized.<String, ContainerTracker, KeyValueStore<Bytes, byte[]>>as(CONTAINER_TABLE)
            .withKeySerde(Serdes.String())
            .withValueSerde(containerSerde)     
        );
        // now this might be duplicate @jerome
        containerTable.toStream().filter((k, v) -> v.isPreviousViolation()).foreach((k, v) -> {
            if (v.isViolatedWithLastTemp()){
                System.out.println("violated " + v.toString());
                System.out.println("Send Notification **************->>> or message to another topic. violated container ");
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                telEmitter.send(new KeyValue<String,ReeferEvent>(k, new ReeferEvent(v.getReeferID(),timestamp.toString(), new Telemetry())));
            }
        });
        return builder.build();
    }
   
/*
    private void anomalyDetector(String key, TelemetryEvent telemetryEvent){
        if (telemetryEvent != null){
            if (predictions_enabled) {
                ScoringResult scoringResult= callAnomalyDetection(telemetryEvent.payload);
                int prediction = (int)scoringResult.getPredictions()[0].values[0][0];
                LOG.info("This is the prediction: " + prediction);
                LOG.info("with a probability: " + "[" + scoringResult.getPredictions()[0].values[0][1] + "," + scoringResult.getPredictions()[0].values[0][1] + "]");
                // Is there anomaly?
                anomalyFound = ( prediction == 0 );
            }
            else {
                // Mockup the prediction
                int number = new Random().nextInt(10);
                if (number > 6) anomalyFound = true;
            }
    
            if (anomalyFound)
            {
                LOG.info("A reefer anomaly has been predicted. Therefore, sending a ReeferAnomaly Event to the appropriate topic");
                ReeferEvent cae = new ReeferEvent(
                            telemetryEvent.containerID, 
                            telemetryEvent.timestamp,
                            telemetryEvent.payload);
                LOG.info("Reefer Anomaly Event object sent: " + cae.toString());
    
                // This message will be sent on, create a new message which acknowledges the incoming message when it is acked
                telEmitter.send(new KeyValue<String,ReeferEvent>(key, cae));
            }
        }
    }
    */

    public double getTemperatureThreshold() {
        return temperatureThreshold;
    }

    public double getMaxCount() {
        return maxCount;
    }

    // used for testing 
    public TelemetryAssessor(double temperatureThreshold, String streamTopic, int maxCount, String tableName) {
        this.temperatureThreshold = temperatureThreshold;
        this.streamTopic = streamTopic;
        this.maxCount = maxCount;
        this.tableName = tableName;
    }

    // public ScoringResult callAnomalyDetection(Telemetry telemetry) {
    //     // todo compute last temperature diff
    //     ScoringTelemetry st = ScoringTelemetry.build(telemetry,0);
    //     ScoringTelemetryWrapper wrapper = new ScoringTelemetryWrapper(st);
    //     return scoringService.assessTelemetry(wrapper);
    // }
}
