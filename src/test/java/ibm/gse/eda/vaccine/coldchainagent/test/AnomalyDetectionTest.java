package ibm.gse.eda.vaccine.coldchainagent.test;

import javax.ws.rs.core.MediaType;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import ibm.gse.eda.vaccine.coldchainagent.domain.Telemetry;
import ibm.gse.eda.vaccine.coldchainagent.infrastructure.scoring.ScoringResult;
import io.quarkus.test.junit.QuarkusTest;
import static io.restassured.RestAssured.given;

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
        ScoringResult result = given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(telemetry)
        .post("/some/resource")
        .then()
        .statusCode(200)
        .extract()
        .response().body().as(ScoringResult.class);
        Assertions.assertNotNull(result);
        System.out.println(result.getPredictions()[0].getScoringPredictionValues().getPrediction().toString());
    }
}