package ibm.gse.eda.vaccine.coldchainagent.domain;

public class ContainerAnomaly {
    public double temperature;
    public double target_temperature; 
    public double ambiant_temperature; 
    public double kilowatts; 
    public double time_door_open;
    public int content_type; 
    public int defrost_cycle;
    public double oxygen_level; 
    public double nitrogen_level; 
    public double humidity_level;
    public double carbon_dioxide_level; 
    public boolean fan_1; 
    public boolean fan_2; 
    public boolean fan_3;
    public double latitude;
    public double longitude;

    public ContainerAnomaly(){
        
    }

    public ContainerAnomaly(Telemetry t){
        this.temperature = t.temperature;
        this.target_temperature = t.target_temperature;
        this.ambiant_temperature = t.ambiant_temperature;
        this.kilowatts = t.kilowatts;
        this.time_door_open = t.time_door_open;
        this.content_type = t.content_type;
        this.defrost_cycle = t.defrost_cycle;
        this.oxygen_level = t.oxygen_level;
        this.nitrogen_level = t.nitrogen_level;
        this.humidity_level = t.humidity_level;
        this.carbon_dioxide_level = t.carbon_dioxide_level;
        this.fan_1 = t.fan_1;
        this.fan_2 = t.fan_2;
        this.fan_3 = t.fan_3;
        this.latitude = t.latitude;
        this.longitude = t.longitude;
    }

  

    public String toString(){
        return "{" +
        "temperature: " + this.temperature + ", " +
        "target_temperature: " + this.target_temperature + ", " +
        "ambiant_temperature: " + this.ambiant_temperature + ", " +
        "kilowatts: " + this.kilowatts + ", " +
        "time_door_open: " + this.time_door_open + ", " +
        "content_type: " + this.content_type + ", " +
        "defrost_cycle: " + this.defrost_cycle + ", " +
        "oxygen_level: " + this.oxygen_level + ", " +
        "nitrogen_level: " + this.nitrogen_level + ", " +
        "humidity_level: " + this.humidity_level + ", " +
        "carbon_dioxide_level: " + this.carbon_dioxide_level + ", " +
        "fan_1: " + this.fan_1 + ", " +
        "fan_2: " + this.fan_2 + ", " +
        "fan_3: " + this.fan_3 + ", " +
        "latitude: " + this.latitude + ", " +
        "longitude: " + this.longitude +
        "}" ;
    }
}
