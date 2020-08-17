package ibm.gse.eda.vaccine.coldchainagent.api;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

import ibm.gse.eda.vaccine.coldchainagent.domain.ContainerTracker;
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

    @Inject
    KafkaStreams streams;
    
    // @POST
    // @Consumes(MediaType.APPLICATION_JSON)
    // public ScoringResult scoreATelemetry(Telemetry t) {
    //     return assessor.callAnomalyDetection(t);
    // }
    @GET
    @Path("/{id}")
    public ContainerTracker getContainer(@PathParam("id") String id) {
        ReadOnlyKeyValueStore<String, ContainerTracker> store=  streams.store(TelemetryAssessor.CONTAINER_TABLE, QueryableStoreTypes.keyValueStore());  
        return store.get(id);
    }
}
