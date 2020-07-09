package ibm.gse.eda.vaccine.coldchainagent.infrastructure.scoring;

public class ScoringResult {
	
	public ScoringPrediction[] predictions;
	
	
	public ScoringResult() {}
	

	public ScoringPrediction[] getPredictions() {
		return predictions;
	}

	public void setPredictions(ScoringPrediction[] predictions) {
		this.predictions = predictions;
	}

	public ScoringPredictionValues getScoringPredictionValues(){
		return predictions[0].getScoringPredictionValues();
	}
}
