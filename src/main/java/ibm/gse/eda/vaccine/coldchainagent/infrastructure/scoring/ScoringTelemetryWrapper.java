package ibm.gse.eda.vaccine.coldchainagent.infrastructure.scoring;

/**
 * This is the input to WML deployed scoring service
 * @author jeromeboyer
 *
 */
public class ScoringTelemetryWrapper {

    ScoringTelemetry[] input_data = new ScoringTelemetry[1];

    public ScoringTelemetryWrapper() {}
    
    public ScoringTelemetryWrapper(ScoringTelemetry sc) {
    	input_data[0] = sc;
    }

	public ScoringTelemetry[] getInputData() {
		return input_data;
	}

	public void setInputData(ScoringTelemetry[] input) {
		this.input_data = input;
	}
}