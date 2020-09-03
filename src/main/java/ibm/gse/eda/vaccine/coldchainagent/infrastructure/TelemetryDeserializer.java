package ibm.gse.eda.vaccine.coldchainagent.infrastructure;

import io.quarkus.kafka.client.serialization.JsonbDeserializer;

public class TelemetryDeserializer extends JsonbDeserializer<TelemetryEvent> {
    public TelemetryDeserializer(){
        // pass the class to the parent.
        super(TelemetryEvent.class);
    }
    
}