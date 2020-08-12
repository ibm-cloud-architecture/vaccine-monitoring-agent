
package ibm.gse.eda.vaccine.coldchainagent.domain;

import ibm.gse.eda.vaccine.coldchainagent.infrastructure.TelemetryEvent;

public class ContainerTracker {
    public int maxTemperature;
    public int maxViloationAllowed;
    public int violatedTemperatureCount;

    public ContainerTracker() {
        this.maxViloationAllowed = 10;
        this.violatedTemperatureCount=  0;
        this.maxTemperature = 100;
    }

    public boolean violateTemperatureThresholdOverTime() {
        if (violatedTemperatureCount >= maxViloationAllowed) {
            return true;
        } else {
            return false;
        }
    }

    public int getViolatedTemperatureCount() {
        return violatedTemperatureCount;
    }

    public void setViolatedTemperatureCount(final int violatedTemperatureCount) {
        this.violatedTemperatureCount = violatedTemperatureCount;
    }

    public int getMaxViloationAllowed() {
        return maxViloationAllowed;
    }

    public void setMaxViloationAllowed(final int maxViloationAllowed) {
        this.maxViloationAllowed = maxViloationAllowed;
    }

    public ContainerTracker update(final TelemetryEvent telemetryEvent) {
        if (telemetryEvent.payload.temperature >  this.maxTemperature){
            this.violatedTemperatureCount++;
        }else{
            this.violatedTemperatureCount = 0;
        }
        return this;
    }   
}