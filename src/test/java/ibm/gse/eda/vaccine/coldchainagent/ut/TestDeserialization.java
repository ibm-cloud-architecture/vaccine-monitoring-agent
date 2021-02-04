package ibm.gse.eda.vaccine.coldchainagent.ut;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import ibm.gse.eda.vaccine.coldchainagent.domain.ReeferAggregate;
import ibm.gse.eda.vaccine.coldchainagent.infrastructure.ReeferAggregateSerde;

public class TestDeserialization {
    
    @Test
    public void shouldHaveList(){
        ReeferAggregate a = new ReeferAggregate();
        a.getTemperatureList().add(Double.valueOf(10));
        a.getTemperatureList().add(Double.valueOf(11));
        Assertions.assertEquals(2,a.getTemperatureList().size());

        ReeferAggregateSerde serde = new ReeferAggregateSerde();
        byte[] s = serde.serializer().serialize("topicname", a);
        ReeferAggregate b = serde.deserializer().deserialize("topicname",s);

        Assertions.assertEquals(2,b.getTemperatureList().size());
        Assertions.assertEquals(4,b.maxViolationAllowed);
        Assertions.assertEquals(25,b.maxRecordToKeep);
        Assertions.assertEquals(0,b.maxTemperature);
        System.out.println(b.toString());
        serde.close();
    }
}