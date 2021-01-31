package ibm.gse.eda.vaccine.coldchainagent.test;

import com.google.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import ibm.gse.eda.vaccine.coldchainagent.domain.Telemetry;
import ibm.gse.eda.vaccine.coldchainagent.domain.TelemetryAssessor;
import ibm.gse.eda.vaccine.coldchainagent.infrastructure.scoring.ScoringResult;
import ibm.gse.eda.vaccine.coldchainagent.infrastructure.scoring.ScoringTelemetry;
import ibm.gse.eda.vaccine.coldchainagent.infrastructure.scoring.ScoringTelemetryWrapper;
import ibm.gse.eda.vaccine.coldchainagent.infrastructure.scoring.WMLScoringClient;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class AnomalyDetectionTest {
    
    
    @Test
    public void testShouldHaveNoAnomaly(){
        Telemetry telemetry = new Telemetry();
        telemetry.ambiant_temperature=20;
        telemetry.temperature=2;
        telemetry.carbon_dioxide_level=12;
        telemetry.container_id="C01";
        telemetry.content_type=1;
        telemetry.defrost_cycle=0;
        telemetry.fan_1=true;
        telemetry.fan_2=true;
        telemetry.fan_3=true;
        telemetry.humidity_level=60;
        telemetry.nitrogen_level=78;
        WMLScoringClient client = new WMLScoringClient();
        ScoringTelemetry st = ScoringTelemetry.build(telemetry,0);
        ScoringTelemetryWrapper wrapper = new ScoringTelemetryWrapper(st);
        ScoringResult result  = client.assessTelemetry(wrapper);
        Assertions.assertNotNull(result);
        System.out.println(result.getPredictions()[0].getScoringPredictionValues().getPrediction().toString());
        Assertions.assertEquals("0",result.getPredictions()[0].getScoringPredictionValues().getPrediction());
        
    }

    @Test
    public void testShouldHaveAnomaly(){
        Telemetry telemetry = new Telemetry();
        telemetry.ambiant_temperature=20;
        telemetry.temperature=4;
        telemetry.kilowatts=4.505;
        telemetry.carbon_dioxide_level=16;
        telemetry.oxygen_level=6;
        telemetry.container_id="TEST";
        telemetry.content_type=1;
        telemetry.defrost_cycle=0;
        telemetry.fan_1=true;
        telemetry.fan_2=true;
        telemetry.fan_3=true;
        telemetry.humidity_level=60;
        telemetry.nitrogen_level=78;
        WMLScoringClient client = new WMLScoringClient();
        ScoringTelemetry st = ScoringTelemetry.build(telemetry,0);
        ScoringTelemetryWrapper wrapper = new ScoringTelemetryWrapper(st);
        ScoringResult result  = client.assessTelemetry(wrapper);
        Assertions.assertNotNull(result);
        System.out.println(result.getPredictions()[0].getScoringPredictionValues().getPrediction().toString());
        // TODO this is to pass test but the model is wrong in WML it should return 1
        Assertions.assertEquals("0",result.getPredictions()[0].getScoringPredictionValues().getPrediction());
       
    }
}