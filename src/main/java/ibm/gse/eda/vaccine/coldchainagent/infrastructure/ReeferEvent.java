package ibm.gse.eda.vaccine.coldchainagent.infrastructure;

import java.sql.Timestamp;

import ibm.gse.eda.vaccine.coldchainagent.domain.ContainerAnomaly;
import ibm.gse.eda.vaccine.coldchainagent.domain.Telemetry;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class ReeferEvent {

    String containerID;
    ContainerAnomaly payload;
    long timestamp;
    String type;

    public ReeferEvent(){}

    public ReeferEvent(String ContainerID, String timestamp, Telemetry payload){
        this.containerID = ContainerID;
        this.timestamp = Timestamp.valueOf(timestamp).getTime();
        this.type = "ContainerAnomaly";
        this.payload = new ContainerAnomaly(payload);
    }

    public String getContainerID(){
        return containerID;
    }
    public void setContainerID(String containerID){
        this.containerID=containerID;
    }
    public ContainerAnomaly getPayload(){
        return payload;
    }
    public void setPayload(ContainerAnomaly payload){
        this.payload=payload;
    }
    public long getTimestamp(){
        return timestamp;
    }
    public void setTimestamp(long timestamp){
        this.timestamp=timestamp;
    }
    public String getType(){
        return type;
    }
    public void setType(String type){
        this.type=type;
    }
    public String toString(){
        return "{" + 
            "containerID: " + this.containerID + ", " +
            "timestamp: " + this.timestamp + ", " +
            "type: " + this.type + ", " +
            "payload: " + this.payload.toString() +
            "}";
    }
}
