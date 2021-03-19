package ibm.gse.eda.vaccine.coldchainagent.infrastructure;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.jboss.logging.Logger;

import ibm.gse.eda.vaccine.coldchainagent.domain.ReeferAggregate;
import ibm.gse.eda.vaccine.coldchainagent.domain.Telemetry;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class ReeferAlert {
    private static Logger LOG = Logger.getLogger(ReeferAlert.class);
    public String containerID;
    public Object record;
    public LocalDateTime timestamp;
    public String type;

    public ReeferAlert(String ContainerID, LocalDateTime localDate, ReeferAggregate v){
        this.containerID = ContainerID;
        this.timestamp = localDate;
        this.type = "Cold Chain Violated";
        this.record = v;
    }

    public ReeferAlert(String ContainerID, LocalDateTime localDate, Telemetry payload){
        this.containerID = ContainerID;
        this.timestamp = localDate;
        this.type = "Container Anomaly Detected";
        this.record = payload;
    }

    public ReeferAlert(String ContainerID, String localDate, Telemetry payload){
        this.containerID = ContainerID;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd H:mm:ss"); 
            this.timestamp = LocalDateTime.parse(localDate,formatter);
        } catch (Exception e) {
            LOG.error(localDate + " not a good date, use now");
            this.timestamp = LocalDateTime.now();
        }
       
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
