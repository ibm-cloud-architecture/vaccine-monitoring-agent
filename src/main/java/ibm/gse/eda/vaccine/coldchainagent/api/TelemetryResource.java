package ibm.gse.eda.vaccine.coldchainagent.api;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import ibm.gse.eda.vaccine.coldchainagent.domain.Telemetry;
import ibm.gse.eda.vaccine.coldchainagent.domain.TelemetryAssessor;
import ibm.gse.eda.vaccine.coldchainagent.infrastructure.scoring.ScoringResult;

/**
 * A simple resource retrieving the last n telemetries read from the topic,
 * and the reefer with temperature raise
 */
@Path("/telemetry")
@Produces(MediaType.APPLICATION_JSON)
public class TelemetryResource {

    @Inject
    TelemetryAssessor assessor;
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public ScoringResult scoreATelemetry(Telemetry t) {
        return assessor.callAnomalyDetection(t);
    }

}
