package ibm.gse.eda.vaccine.coldchainagent.domain.interactivequery;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.apache.kafka.streams.state.StreamsMetadata;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import ibm.gse.eda.vaccine.coldchainagent.domain.ContainerTracker;
import ibm.gse.eda.vaccine.coldchainagent.domain.TelemetryAssessor;

@ApplicationScoped
public class ContainerTrackerInteractiveQueries {

    private static final Logger LOG = Logger.getLogger(ContainerTrackerInteractiveQueries.class);

    // @ConfigProperty(name = "hostname")
    String host = System.getenv("HOSTNAME");

    @Inject
    KafkaStreams streams;

    public List<PipelineMetadata> getMetaData() {
        return streams.allMetadataForStore(TelemetryAssessor.CONTAINER_TABLE)
                .stream()
                .map(m -> new PipelineMetadata(
                        m.hostInfo().host() + ":" + m.hostInfo().port(),
                        m.topicPartitions()
                                .stream()
                                .map(TopicPartition::toString)
                                .collect(Collectors.toSet())))
                .collect(Collectors.toList());
    }

    public GetContainerTrackerDataResult getContainerTrackerData(String id) {
        StreamsMetadata metadata = streams.metadataForKey(
            TelemetryAssessor.CONTAINER_TABLE,
                id,
                Serdes.String().serializer());

        if (metadata == null || metadata == StreamsMetadata.NOT_AVAILABLE) {
            LOG.warnv("Found no metadata for key {0}", id);
            return GetContainerTrackerDataResult.notFound();
        } else if (metadata.host().equals(host)) {
            LOG.infov("Found data for key {0} locally", id);
            ContainerTracker result = getContainerTrackerStore().get(id);

            if (result != null) {
                return GetContainerTrackerDataResult.found(result);
            } else {
                return GetContainerTrackerDataResult.notFound();
            }
        } else {
            LOG.infov("Found data for key {0} on remote host {1}:{2}", id, metadata.host(), metadata.port());
            return GetContainerTrackerDataResult.foundRemotely(metadata.host(), metadata.port());
        }
    }

    private ReadOnlyKeyValueStore<String, ContainerTracker> getContainerTrackerStore() {
        while (true) {
            try {
                return streams.store(TelemetryAssessor.CONTAINER_TABLE, QueryableStoreTypes.keyValueStore());
            } catch (InvalidStateStoreException e) {
                // ignore, store not ready yet
            }
        }
    }
}
