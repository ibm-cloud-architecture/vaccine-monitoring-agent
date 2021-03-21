package ibm.gse.eda.vaccine.coldchainagent.domain;

import java.time.LocalDateTime;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KGroupedStream;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

import ibm.gse.eda.vaccine.coldchainagent.infrastructure.ReeferAggregateSerde;
import ibm.gse.eda.vaccine.coldchainagent.infrastructure.ReeferAlert;
import ibm.gse.eda.vaccine.coldchainagent.infrastructure.TelemetryEvent;
import ibm.gse.eda.vaccine.coldchainagent.infrastructure.scoring.ScoringResult;
import ibm.gse.eda.vaccine.coldchainagent.infrastructure.scoring.ScoringTelemetry;
import ibm.gse.eda.vaccine.coldchainagent.infrastructure.scoring.ScoringTelemetryWrapper;
import ibm.gse.eda.vaccine.coldchainagent.infrastructure.scoring.WMLScoringClient;
import io.quarkus.kafka.client.serialization.ObjectMapperSerde;

/**
 * A bean consuming telemetry events from the "telemetries" Kafka topic and
 * applying following logic: - count if the temperature is above a specific
 * threshold for n events then the cold chain is violated. Generate an alert events
 * to a different topic
 * This code may also call an external anomaly detection scoring service to assess for
 * refrigerator anomaly detected from the different sensor telemetries
 */
@ApplicationScoped
public class TelemetryAssessor {
    public final static String REEFER_AGGREGATE_TABLE = "reeferAggregateTable";
    protected static Logger LOG = Logger.getLogger(TelemetryAssessor.class);

    @Inject
    @ConfigProperty(name = "temperature.threshold")
    public double temperatureThreshold;

    @Inject
    @ConfigProperty(name = "quarkus.kafka-streams.topics", defaultValue = "telemetries")
    public String telemetryTopicName;

    @Inject
    @ConfigProperty(name = "temperature.max.occurence.count", defaultValue = "12")
    public int maxCount;

    @Inject
    @ConfigProperty(name = "prediction.enabled", defaultValue = "false")
    public boolean anomalyDetectionEnabled;

    public @Inject @Channel("reeferAlerts") Emitter<ReeferAlert> reeferEventEmitter;

    // Anomaly detection service
    WMLScoringClient anomalyDetectionScoringService = new WMLScoringClient();

    public int count;
    private boolean anomalyFound = false;

    public TelemetryAssessor() {
    }


    public boolean violateTemperatureThresholdOverTime(ReeferAggregate telemetryEvent) {
        return true;
    }

    /**
     * From the telemetries received, compute the aggregates and keep those
     * aggregates in a ktable.
     * If there are consecutive temperature violations for more than n measured T
     * then emit an event on the reefers.
     * @return kafka stream topology
     */
    @Produces
    public Topology buildTopology() {
        StreamsBuilder builder = new StreamsBuilder();

        ObjectMapperSerde<TelemetryEvent> telemetryEventSerde = new ObjectMapperSerde<>(
                TelemetryEvent.class);
        ReeferAggregateSerde reeferAggregateSerde = new ReeferAggregateSerde();
        // telemetries message has containerID as key and value as telemetries
        KStream<String, TelemetryEvent> telemetryStream = builder.stream(telemetryTopicName,
                    Consumed.with(Serdes.String(),
                    telemetryEventSerde))
                .peek((k, v) -> {
                  LOG.info(k + " -> " + v);
                })
                .map((k, v) -> {
                    if (v.payload != null){
                        return new KeyValue<String, TelemetryEvent>(v.containerID, v);
                    } else {
                        return new KeyValue<String, TelemetryEvent>(v.containerID, null);
                    }
                });
        // for each message potentially call anomaly detector
        telemetryStream.peek(( k,telemetryEvent ) -> {
             if (anomalyDetectionEnabled) {
                anomalyDetector(k, telemetryEvent);
             }
             
         });

        // group stream by key and serialized with key as string and value as TelemetryEvent
        KGroupedStream<String, TelemetryEvent> telemetryGroup = telemetryStream.groupByKey(Grouped.with(Serdes.String(), telemetryEventSerde));
        // create table with store as containerTable
        KTable<String, ReeferAggregate> reeferAggregateTable = telemetryGroup.aggregate(
            () -> new ReeferAggregate(maxCount,temperatureThreshold),
            (k, newTelemetry, currentAggregate) -> currentAggregate.updateTemperature(newTelemetry.payload.temperature),
            Materialized.<String, ReeferAggregate, KeyValueStore<Bytes, byte[]>>as(REEFER_AGGREGATE_TABLE)
            .withKeySerde(Serdes.String())
            .withValueSerde(reeferAggregateSerde)
        );
        // send reefer info that has cold chain violated
        reeferAggregateTable.toStream()
        .peek((k, v) -> {
          LOG.debug(k + " -> " + v);
        })
        .filter((k, v) -> v.hasTooManyViolations()).foreach((k, v) -> {
                LOG.info("Violation " + v.toString());
                LOG.info("Send Notification **************->>> or message to reeferAlert topic. ");
                reeferEventEmitter.send(new ReeferAlert(k,LocalDateTime.now(),v));
        });
        return builder.build();
    }


    private void anomalyDetector(String key, TelemetryEvent telemetryEvent){
        anomalyFound = false;
        if (telemetryEvent != null){
            if (anomalyDetectionEnabled) {
                ScoringResult scoringResult= callAnomalyDetection(telemetryEvent.payload);
                if (scoringResult != null) {
                    int prediction = (int)scoringResult.getPredictions()[0].values[0][0];
                    LOG.info("This is the prediction: " + prediction);
                    LOG.info("with a probability: " + "[" + scoringResult.getPredictions()[0].values[0][1] + "," + scoringResult.getPredictions()[0].values[0][1] + "]");
                    // Is there anomaly?
                    anomalyFound = ( prediction == 0 );
                }
               
            }

            if (anomalyFound)
            {
                LOG.info("A reefer anomaly has been predicted. Therefore, sending a ReeferAnomaly Event to the reefer topic " + telemetryEvent.toString());
                ReeferAlert cae = new ReeferAlert(
                            telemetryEvent.containerID,
                            telemetryEvent.timestamp,
                            telemetryEvent.payload);
                LOG.info("Reefer Anomaly Event object sent: " + cae.toString());

                // This message will be sent on, create a new message which acknowledges the incoming message when it is acked
                reeferEventEmitter.send(cae);
            }
        }
    }
    


    // used for testing
    public TelemetryAssessor(double temperatureThreshold, String topicName, int maxCount) {
        this.temperatureThreshold = temperatureThreshold;
        this.telemetryTopicName = topicName;
        this.maxCount = maxCount;
    }

    public ScoringResult callAnomalyDetection(Telemetry telemetry) {
         // todo compute last temperature diff
         ScoringTelemetry st = ScoringTelemetry.build(telemetry,0);
         ScoringTelemetryWrapper wrapper = new ScoringTelemetryWrapper(st);
         return anomalyDetectionScoringService.assessTelemetry(wrapper);
     }
}
