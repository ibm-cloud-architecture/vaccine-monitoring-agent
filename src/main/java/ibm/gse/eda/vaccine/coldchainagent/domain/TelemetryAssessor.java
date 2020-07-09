package ibm.gse.eda.vaccine.coldchainagent.domain;

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

import ibm.gse.eda.vaccine.coldchainagent.infrastructure.ReeferEvent;
import ibm.gse.eda.vaccine.coldchainagent.infrastructure.TelemetryEvent;
import ibm.gse.eda.vaccine.coldchainagent.infrastructure.scoring.ScoringResult;
import ibm.gse.eda.vaccine.coldchainagent.infrastructure.scoring.ScoringService;
import ibm.gse.eda.vaccine.coldchainagent.infrastructure.scoring.ScoringTelemetry;
import ibm.gse.eda.vaccine.coldchainagent.infrastructure.scoring.ScoringTelemetryWrapper;

/**
 * A bean consuming telemetry events from the "reefer-telemetry" Kafka topic and 
 * applying following logic:
 * - count if the temperature is above a specific threshold for n events then
 * the cold chain is violated. 
 * - call external anomaly detection scoring service
 */
@ApplicationScoped
public class TelemetryAssessor {

    @ConfigProperty(name="temperature.threshold")
    public double temperatureThreshold;

    @ConfigProperty(name="temperature.max.occurence.count",defaultValue="3")
    public double maxCount;

    @ConfigProperty(name = "prediction.enabled", defaultValue="false")
    public boolean predictions_enabled;

    @Inject
    @RestClient
    ScoringService scoringService;
    
    public int count;

    public TelemetryAssessor(){}

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
        System.out.println("Received message: " + telemetryEvent);
        if (violateTemperatureThresholdOverTime(telemetryEvent)) {

        }
        if (predictions_enabled) {
            ScoringResult scoringResult= callAnomalyDetection(telemetryEvent.payload);
        }
        return ReactiveStreams.empty();
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
