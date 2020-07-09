package ibm.gse.eda.vaccine.coldchainagent.infrastructure.scoring;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey="anomalydetection.scoring")
public interface ScoringService {

    @ConfigProperty(name="anomalydetection.scoring.wmlToken")
    public static String wml_token = "";
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ClientHeaderParam(name = "Authorization", value="{ibm.gse.eda.vaccine.coldchainagent.infrastructure.scoring.ScoringService.generateHeader}")
    public ScoringResult assessTelemetry(ScoringTelemetryWrapper telemetry); 

    public static String generateHeader(){
        return "Bearer " + wml_token;
    }
}