package ibm.gse.eda.vaccine.coldchainagent.infrastructure;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import ibm.gse.eda.vaccine.coldchainagent.domain.ReeferAggregate;
import io.quarkus.kafka.client.serialization.JsonbDeserializer;
import io.quarkus.kafka.client.serialization.JsonbSerializer;

public class ReeferAggregateSerde implements Serde<ReeferAggregate> {
    private final ReeferAggregateSerializer serializer;
    private final ReeferAggregateDeserializer deserializer;

    public ReeferAggregateSerde() {
        this.serializer = new ReeferAggregateSerializer();
        this.deserializer = new ReeferAggregateDeserializer();
    }

    @Override
    public Serializer<ReeferAggregate> serializer() {
        return serializer;
    }

    @Override
    public Deserializer<ReeferAggregate> deserializer() {
        return deserializer;
    }

    @Override
    public void close() {
        serializer.close();
        deserializer.close();
    }
    
    private class ReeferAggregateSerializer extends JsonbSerializer<ReeferAggregate>  {
        private final Jsonb jsonb = JsonbBuilder.create();
       
        @Override
        public byte[] serialize(String topic, ReeferAggregate data) {
            try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                jsonb.toJson(data, output);
                return output.toByteArray();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private class ReeferAggregateDeserializer extends JsonbDeserializer<ReeferAggregate> {
        private final Jsonb jsonb;

        public ReeferAggregateDeserializer() {
            super(ReeferAggregate.class);
            jsonb = JsonbBuilder.create();
        }

        @Override
        public ReeferAggregate deserialize(String topic, byte[] data) {
            if (data == null) {
                return null;
            }

            // THIS IS HORRIBLE... need to think of a better way to do that
            try (InputStream is = new ByteArrayInputStream(data)) {
                ReeferAggregate result = new ReeferAggregate();
                JsonObject asJson = jsonb.fromJson(is, JsonObject.class);
                result.setReeferID(asJson.getString("reeferID"));
                result.maxTemperature = asJson.getInt("maxTemperature");
                try {
                    result.tooManyViolations = asJson.getBoolean("tooManyViolations");
                    result.alreadyReportedColdChainViolation = asJson.getBoolean("alreadyReportedColdChainViolation");
                } catch (Exception e) {
                    // not present... really?
                }
                
                result.maxViolationAllowed = asJson.getInt("maxViolationAllowed");
                result.setViolatedTemperatureCount(asJson.getInt("violatedTemperatureCount"));
                JsonArray tValues = asJson.getJsonArray("temperatureList");
                tValues.forEach( (t) -> 
                        result.getTemperatureList().add(Double.parseDouble(t.toString()))
                );
                return result;
            
               } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

  
}