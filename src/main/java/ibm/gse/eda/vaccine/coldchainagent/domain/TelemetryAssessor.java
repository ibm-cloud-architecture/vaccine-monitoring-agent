package ibm.gse.eda.vaccine.coldchainagent.domain;

import java.util.Random;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import ibm.gse.eda.vaccine.coldchainagent.infrastructure.ContainerAnomalyEvent;
import ibm.gse.eda.vaccine.coldchainagent.infrastructure.ReeferEvent;
import ibm.gse.eda.vaccine.coldchainagent.infrastructure.TelemetryEvent;
import ibm.gse.eda.vaccine.coldchainagent.infrastructure.scoring.ScoringResult;
import ibm.gse.eda.vaccine.coldchainagent.infrastructure.scoring.ScoringService;
import ibm.gse.eda.vaccine.coldchainagent.infrastructure.scoring.ScoringTelemetry;
import ibm.gse.eda.vaccine.coldchainagent.infrastructure.scoring.ScoringTelemetryWrapper;

/**
 * A bean consuming telemetry events from the "reefer-telemetry" Kafka topic and
 * applying following logic: - count if the temperature is above a specific
 * threshold for n events then the cold chain is violated. - call external
 * anomaly detection scoring service
 */
@ApplicationScoped
public class TelemetryAssessor {
    protected static Logger LOG = Logger.getLogger(TelemetryAssessor.class);
    @ConfigProperty(name = "temperature.threshold")
    public double temperatureThreshold;

    @ConfigProperty(name = "temperature.max.occurence.count", defaultValue = "3")
    public double maxCount;

    @ConfigProperty(name = "prediction.enabled", defaultValue = "false")
    public boolean predictions_enabled;

    @Inject
    @RestClient
    ScoringService scoringService;

    public int count;
    private boolean anomalyFound = false;

    public TelemetryAssessor() {
    }

    /**
     * 
     * @param message
     * @return
     */
    @Incoming("reefer-telemetry")
    @Outgoing("reefers")
    @Acknowledgment(Acknowledgment.Strategy.MANUAL)
    public PublisherBuilder<Message<ReeferEvent>> processTelemetryEvent(Message<TelemetryEvent> message) {
        // Get the message as String
        TelemetryEvent telemetryEvent = message.getPayload();
        LOG.info("Received message: " + telemetryEvent);
        if (violateTemperatureThresholdOverTime(telemetryEvent)) {

        }
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

        if (!anomalyFound){
            message.ack(); // All processing of this message is done, ack it now
            return ReactiveStreams.empty();
        }
        else{
            LOG.info("A reefer anomaly has been predicted. Therefore, sending a ReeferAnomaly Event to the appropriate topic");
            ReeferEvent cae = new ReeferEvent(
                        telemetryEvent.containerID, 
                        telemetryEvent.timestamp,
                        telemetryEvent.payload);
            LOG.info("Reefer Anomaly Event object sent: " + cae.toString());

            // This message will be sent on, create a new message which acknowledges the incoming message when it is acked
            return ReactiveStreams.of(Message.of(cae));
        }
    }

    public boolean violateTemperatureThresholdOverTime(TelemetryEvent telemetryEvent) {
        return false;
    }

    public ScoringResult callAnomalyDetection(Telemetry telemetry) {
        // todo compute last temperature diff
        ScoringTelemetry st = ScoringTelemetry.build(telemetry,0);
        ScoringTelemetryWrapper wrapper = new ScoringTelemetryWrapper(st);
        return scoringService.assessTelemetry(wrapper);
    }

    public double getTemperatureThreshold() {
		return temperatureThreshold;
	}

    public double getMaxCount() {
        return maxCount;
    }
}
