
package ibm.gse.eda.vaccine.coldchainagent.test;



import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import ibm.gse.eda.vaccine.coldchainagent.domain.TelemetryAssessor;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class TelemetryColdChainTest {

    @Inject
    public TelemetryAssessor assessor;
    
    /**
     * When 5 temperature measurements go over the threshold
     * then raise an issue
     */
    @Test
    public void shouldAlarmOn5ConcecutiveTemperatureRaise(){
        Assertions.assertEquals(5.0, assessor.getMaxCount());
        Assertions.assertEquals(2.5, assessor.getTemperatureThreshold());
    }
}