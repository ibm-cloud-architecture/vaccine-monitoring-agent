package ibm.gse.eda.vaccine.coldchainagent.infrastructure;

import java.time.LocalDate;

import ibm.gse.eda.vaccine.coldchainagent.domain.ReeferAggregate;
import ibm.gse.eda.vaccine.coldchainagent.domain.Telemetry;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class ReeferEvent {

    public String containerID;
    public Object record;
    public LocalDate timestamp;
    public String type;

    public ReeferEvent(String ContainerID, LocalDate localDate, ReeferAggregate v){
        this.containerID = ContainerID;
        this.timestamp = localDate;
        this.type = "Cold Chain Violated";
        this.record = v;
    }

    public ReeferEvent(String ContainerID, LocalDate localDate, Telemetry payload){
        this.containerID = ContainerID;
        this.timestamp = localDate;
        this.type = "Container Anomaly Detected";
        this.record = payload;
    }

    public ReeferEvent(String ContainerID, String localDate, Telemetry payload){
        this.containerID = ContainerID;
        this.timestamp = LocalDate.parse(localDate);
        this.type = "Container Anomaly Detected";
        this.record = payload;
    }
   
    public String toString(){
        return "{" + 
            "containerID: " + this.containerID + ", " +
            "timestamp: " + this.timestamp + ", " +
            "type: " + this.type + ", " +
            "}";
    }
}
