package ibm.gse.eda.vaccine.coldchainagent.domain;

import java.util.ArrayList;

import ibm.gse.eda.vaccine.coldchainagent.infrastructure.TelemetryEvent;

public class ContainerTracker {
    private double maxTemperature;
    private int maxViloationAllowed;
    private int violatedTemperatureCount;
    private boolean previousViolation;
    private ArrayList<Double> temperatureList;
    private boolean violatedWithLastTemp;
    
    public ContainerTracker(int maxViloationAllowed, double maxTemperature) {
        this.maxViloationAllowed = maxViloationAllowed;
        this.violatedTemperatureCount=  0;
        this.maxTemperature = maxTemperature;
        this.previousViolation = false;
        this.temperatureList = new ArrayList<>();
        this.violatedWithLastTemp = false;
    }
    public ContainerTracker(double maxTemperature, int maxViloationAllowed, int violatedTemperatureCount) {
        this.maxTemperature = maxTemperature;
        this.maxViloationAllowed = maxViloationAllowed;
        this.violatedTemperatureCount = violatedTemperatureCount;
        this.previousViolation = false;
        this.temperatureList = new ArrayList<>();
        this.violatedWithLastTemp = false;
    }
    public ContainerTracker() {
        this.maxTemperature = 90;
        this.maxViloationAllowed = 5;
        this.violatedTemperatureCount = 0;
        this.previousViolation = false;
        this.temperatureList = new ArrayList<>();
        this.violatedWithLastTemp = false;
    }

    public boolean violateTemperatureThresholdOverTime() {
        if (violatedTemperatureCount >= maxViloationAllowed) {
            return true;
        } else {
            return false;
        }
    }

    public ContainerTracker update(final TelemetryEvent telemetryEvent) {
        // if violation hasn't occured yet
        if (!previousViolation){
            // check temperature
            if (telemetryEvent.payload.temperature >=  this.maxTemperature){
                // temperature is greater than allowed record temperature
                this.temperatureList.add(telemetryEvent.payload.temperature);
                // increase violation count
                this.violatedTemperatureCount++;
                // check if this temperature made container temperature go over allowed
                previousViolation = violateTemperatureThresholdOverTime();
                // if this temperature changed violation of container
                if (previousViolation){
                    this.violatedWithLastTemp = true;
                }
            }else{
                this.temperatureList = new ArrayList<>();
                this.violatedTemperatureCount = 0;
                this.violatedWithLastTemp = false;
            }
        } else {
            // this event didn't cause the container to change state to violated. so changing violated with last temp to false
            this.violatedWithLastTemp = false;
        }
        return this;
    }

    public double getMaxTemperature() {
        return maxTemperature;
    }

    public void setMaxTemperature(double maxTemperature) {
        this.maxTemperature = maxTemperature;
    }

    public int getMaxViloationAllowed() {
        return maxViloationAllowed;
    }

    public void setMaxViloationAllowed(int maxViloationAllowed) {
        this.maxViloationAllowed = maxViloationAllowed;
    }

    public int getViolatedTemperatureCount() {
        return violatedTemperatureCount;
    }

    public void setViolatedTemperatureCount(int violatedTemperatureCount) {
        this.violatedTemperatureCount = violatedTemperatureCount;
    }

    public boolean isPreviousViolation() {
        return previousViolation;
    }

    public void setPreviousViolation(boolean previousViolation) {
        this.previousViolation = previousViolation;
    }

    public ArrayList<Double> getTemperatureList() {
        return temperatureList;
    }

    public void setTemperatureList(ArrayList<Double> temperatureList) {
        this.temperatureList = temperatureList;
    }


    public boolean isViolatedWithLastTemp() {
        return this.violatedWithLastTemp;
    }

    public void setViolatedWithLastTemp(boolean violatedWithLastTemp) {
        this.violatedWithLastTemp = violatedWithLastTemp;
    }

    @Override
    public String toString() {
        return "ContainerTracker [maxTemperature=" + maxTemperature + ", maxViloationAllowed=" + maxViloationAllowed
                + ", previousViolation=" + previousViolation + ", temperatureList=" + temperatureList
                + ", violatedTemperatureCount=" + violatedTemperatureCount + ", violatedWithLastTemp="
                + violatedWithLastTemp + "]";
    }

    
}