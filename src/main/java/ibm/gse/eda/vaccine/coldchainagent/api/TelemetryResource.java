package ibm.gse.eda.vaccine.coldchainagent.api;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.annotations.SseElementType;
import org.reactivestreams.Publisher;

import org.eclipse.microprofile.reactive.messaging.Channel;

/**
 * A simple resource retrieving the last n telemetries read from the topic,
 * and the reefer with temperature raise
 */
@Path("/telemetry")
@Produces(MediaType.APPLICATION_JSON)
public class TelemetryResource {


}
