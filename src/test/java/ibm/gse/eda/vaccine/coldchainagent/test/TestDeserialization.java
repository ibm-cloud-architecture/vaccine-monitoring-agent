package ibm.gse.eda.vaccine.coldchainagent.test;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import ibm.gse.eda.vaccine.coldchainagent.domain.ReeferAggregate;
import ibm.gse.eda.vaccine.coldchainagent.infrastructure.ReeferAggregateSerde;

public class TestDeserialization {
    
    @Test
    public void shouldHaveList(){
        ReeferAggregate a = new ReeferAggregate();
        a.setReeferID("test-id");
        a.getTemperatureList().add(new Double(10));
        a.getTemperatureList().add(new Double(11));
        Assertions.assertEquals(2,a.getTemperatureList().size());

        ReeferAggregateSerde serde = new ReeferAggregateSerde();
        byte[] s = serde.serializer().serialize("", a);
        ReeferAggregate b = serde.deserializer().deserialize("",s);

        Assertions.assertEquals(2,b.getTemperatureList().size());
        serde.close();
    }
}