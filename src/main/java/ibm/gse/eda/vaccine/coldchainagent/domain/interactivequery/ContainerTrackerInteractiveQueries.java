package ibm.gse.eda.vaccine.coldchainagent.domain.interactivequery;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyQueryMetadata;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import ibm.gse.eda.vaccine.coldchainagent.domain.ReeferAggregate;
import ibm.gse.eda.vaccine.coldchainagent.domain.TelemetryAssessor;

@ApplicationScoped
public class ContainerTrackerInteractiveQueries {

    private static final Logger LOG = Logger.getLogger(ContainerTrackerInteractiveQueries.class);

    @Inject
    @ConfigProperty(name = "POD_IP")
    String host;

    @Inject
    KafkaStreams streams;

    public List<PipelineMetadata> getMetaData() {
        return streams.allMetadataForStore(TelemetryAssessor.REEFER_AGGREGATE_TABLE)
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
        KeyQueryMetadata metadata = null;
        LOG.warnv("Search metadata for key {0}", id);
        try {
            metadata = streams.queryMetadataForKey(
                TelemetryAssessor.REEFER_AGGREGATE_TABLE,
                id,
                Serdes.String().serializer());
        } catch (Exception e) {
            e.printStackTrace();
            return GetContainerTrackerDataResult.notFound();
        }
        if (metadata == null || metadata == KeyQueryMetadata.NOT_AVAILABLE) {
            LOG.warnv("Found no metadata for key {0}", id);
            return GetContainerTrackerDataResult.notFound();
        } else if (metadata.getActiveHost().host().equals(host)) {
            LOG.infov("Found data for key {0} locally", id);
            ReeferAggregate result = getContainerTrackerStore().get(id);
            LOG.infov("Container Tracker is : ", id);
            if (result != null) {
                LOG.infov("returning found");
                return GetContainerTrackerDataResult.found(result);
            } else {
                LOG.infov("returning not found");
                return GetContainerTrackerDataResult.notFound();
            }
        } else {
            LOG.infov("Found data for key {0} on remote host {1}:{2}", 
                id, 
                metadata.getActiveHost().host(), 
                metadata.getActiveHost().port());
            return GetContainerTrackerDataResult.foundRemotely(metadata.getActiveHost().host(), metadata.getActiveHost().port());
        }
    }

    public ReadOnlyKeyValueStore<String, ReeferAggregate> getContainerTrackerStore() {
        while (true) {
            try {
                StoreQueryParameters<ReadOnlyKeyValueStore<String,ReeferAggregate>> parameters = StoreQueryParameters.fromNameAndType(TelemetryAssessor.REEFER_AGGREGATE_TABLE,QueryableStoreTypes.keyValueStore());
                return streams.store(parameters);
            } catch (InvalidStateStoreException e) {
                // ignore, store not ready yet
            }
        }
    }
}
