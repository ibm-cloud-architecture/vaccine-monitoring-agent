package ibm.gse.eda.vaccine.coldchainagent.api;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Qualifier;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

import ibm.gse.eda.vaccine.coldchainagent.api.dto.Returntype;
import ibm.gse.eda.vaccine.coldchainagent.domain.ContainerTracker;
import ibm.gse.eda.vaccine.coldchainagent.domain.TelemetryAssessor;
import ibm.gse.eda.vaccine.coldchainagent.domain.interactivequery.ContainerTrackerInteractiveQueries;
import ibm.gse.eda.vaccine.coldchainagent.domain.interactivequery.GetContainerTrackerDataResult;
import ibm.gse.eda.vaccine.coldchainagent.domain.interactivequery.PipelineMetadata;

/**
 * A simple resource retrieving the last n telemetries read from the topic, and
 * the reefer with temperature raise
 */
@Path("/reefer-tracker")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class ContainerResource {
    // @GET
    // @Path("/{reeferID}")
    // public ContainerTracker getContainer(@PathParam("reeferID") final String
    // reeferID) {
    // final StoreQueryParameters<ReadOnlyKeyValueStore<String,ContainerTracker>>
    // parameters =
    // StoreQueryParameters.fromNameAndType(TelemetryAssessor.CONTAINER_TABLE,QueryableStoreTypes.keyValueStore());
    // return streams.store(parameters).get(reeferID);
    // }

    // @GET
    // public ArrayList<Returntype> getktable() {
    // final StoreQueryParameters<ReadOnlyKeyValueStore<String,ContainerTracker>>
    // parameters =
    // StoreQueryParameters.fromNameAndType(TelemetryAssessor.CONTAINER_TABLE,QueryableStoreTypes.keyValueStore());
    // final KeyValueIterator<String, ContainerTracker> val =
    // streams.store(parameters).all();
    // final ArrayList<Returntype> returnList= new ArrayList<Returntype>();
    // while (val.hasNext()){
    // final KeyValue<String, ContainerTracker> keypair = val.next();
    // returnList.add(new Returntype(keypair.key, keypair.value));
    // }
    // return returnList;
    // }

    @Inject
    ContainerTrackerInteractiveQueries interactiveQueries;

    @GET
    @Path("/data/{reeferID}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getContainerData(@PathParam("reeferID") final String reeferID) {
        GetContainerTrackerDataResult result = interactiveQueries.getContainerTrackerData(reeferID);

        if (result.getResult().isPresent()) {
            return Response.ok(result.getResult().get()).build();
        } else if (result.getHost().isPresent()) {
            URI otherUri = getOtherUri(result.getHost().get(), result.getPort().getAsInt(), reeferID);
            System.out.println(otherUri.toString());
            // not working
            // InputStream is = null;
            // DataInputStream dis;
            // String s;
            // try {
            //     is = otherUri.toURL().openStream();
            
            //     dis = new DataInputStream(new BufferedInputStream(is));
            //     while ((s = dis.readLine()) != null)
            //     {
            //         System.out.println(s);
            //     }
            // } catch (MalformedURLException e) {
            //     // TODO Auto-generated catch block
            //     e.printStackTrace();
            // } catch (IOException e) {
            //     // TODO Auto-generated catch block
            //     e.printStackTrace();
            // }
            return Response.seeOther(otherUri).build();
        } else {
            return Response.status(Status.NOT_FOUND.getStatusCode(), "No data found for container Id " + reeferID).build();
        }
    }

    @GET
    @Path("/meta-data")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PipelineMetadata> getMetaData() {
        return interactiveQueries.getMetaData();
    }

    private URI getOtherUri(String host, int port, String reeferID) {
        try {
            return new URI("http://" + host + ":" + port + "/reefer-tracker/data/" + reeferID);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }


}


