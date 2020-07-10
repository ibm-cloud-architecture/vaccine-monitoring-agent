package ibm.gse.eda.vaccine.coldchainagent.infrastructure;

import java.sql.Timestamp;

import ibm.gse.eda.vaccine.coldchainagent.domain.Telemetry;

public class ContainerAnomalyEvent {

    String containerID;
    ReeferAnomaly payload;
    long timestamp;
    String type;

    public ContainerAnomalyEvent(){}

    public ContainerAnomalyEvent(String ContainerID, String timestamp, Telemetry payload){
        this.containerID = ContainerID;
        this.timestamp = Timestamp.valueOf(timestamp).getTime();
        this.type = "ReeferAnomaly";
        this.payload = new ReeferAnomaly(payload);
    }

    public String getContainerID(){
        return containerID;
    }
    public void setContainerID(String containerID){
        this.containerID=containerID;
    }
    public ReeferAnomaly getPayload(){
        return payload;
    }
    public void setPayload(ReeferAnomaly payload){
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
