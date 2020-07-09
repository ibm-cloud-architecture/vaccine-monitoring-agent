package ibm.gse.eda.vaccine.coldchainagent.infrastructure.scoring;

import ibm.gse.eda.vaccine.coldchainagent.domain.Telemetry;

/**
 * This is the input to WML deployed scoring service
 * @author jeromeboyer
 *
 */
public class ScoringTelemetry {

    public static final String[] FEATURE_NAMES = { 
		"temperature",
		"target_temperature", 
		"ambiant_temperature", 
		"kilowatts", 
		"time_door_open",
		"content_type",
		"defrost_cycle",
		"oxygen_level", 
		"nitrogen_level", 
		"humidity_level",
		"target_humidity_level",
		"carbon_dioxide_level", 
		"fan_1", "fan_2", "fan_3",
		"maintenance_required",
	    "temp_diff"};
	
		String[] fields = FEATURE_NAMES;
		Object[][] values = new Object[1][16];
	
		public ScoringTelemetry() {}
		
		public static ScoringTelemetry build(Telemetry inEvent, double temp_diff) {
			ScoringTelemetry st = new ScoringTelemetry();
			st.values[0][0] = inEvent.temperature;
			st.values[0][1] = inEvent.target_temperature;
			st.values[0][2] = inEvent.ambiant_temperature;
			st.values[0][3] = inEvent.kilowatts;
			st.values[0][4] = inEvent.time_door_open;
			st.values[0][5] = inEvent.content_type;
			st.values[0][6] = inEvent.defrost_cycle;
			st.values[0][7] = inEvent.oxygen_level;
			st.values[0][8] = inEvent.nitrogen_level;
			st.values[0][9] = inEvent.humidity_level;
			st.values[0][10] = inEvent.carbon_dioxide_level;
			st.values[0][11] = inEvent.fan_1;
			st.values[0][12] = inEvent.fan_2;
			st.values[0][13] = inEvent.fan_3;
			st.values[0][14] = 0;
			st.values[0][15] = temp_diff;
			return st;
		}
	
		public Object[][] getValues() {
			return values;
		}
	
		public void setValues(Object[][] values) {
			this.values = values;
		}
	
		public String[] getFields() {
			return fields;
		}
}