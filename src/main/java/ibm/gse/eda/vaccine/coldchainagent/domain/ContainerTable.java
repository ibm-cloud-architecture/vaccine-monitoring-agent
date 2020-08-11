
package ibm.gse.eda.vaccine.coldchainagent.domain;

import ibm.gse.eda.vaccine.coldchainagent.infrastructure.TelemetryEvent;

public class ContainerTable {
    public int maxTemperature;
    public int maxViloationAllowed;
    public int violatedTemperatureCount;

    public ContainerTable(int violatedTemperatureCount) {
        this.maxViloationAllowed = 10;    
        this.violatedTemperatureCount = violatedTemperatureCount;
        this.maxTemperature = 100;
        
    }

    public int getViolatedTemperatureCount() {
        return violatedTemperatureCount;
    }

    public void setViolatedTemperatureCount(int violatedTemperatureCount) {
        this.violatedTemperatureCount = violatedTemperatureCount;
    }

    public int getMaxViloationAllowed() {
        return maxViloationAllowed;
    }

    public void setMaxViloationAllowed(int maxViloationAllowed) {
        this.maxViloationAllowed = maxViloationAllowed;
    }

    public ContainerTable update(TelemetryEvent telemetryEvent){
        if (telemetryEvent.payload.temperature >  this.maxTemperature){
            this.violatedTemperatureCount++;
        }else{
            this.violatedTemperatureCount = 0;
        }
        return this;
    }

    
    
}