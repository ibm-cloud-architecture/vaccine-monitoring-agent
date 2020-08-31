package ibm.gse.eda.vaccine.coldchainagent.domain;

import java.util.ArrayList;

import ibm.gse.eda.vaccine.coldchainagent.infrastructure.TelemetryEvent;

public class ContainerTracker {
    private double maxTemperature;
    private int maxViolationAllowed;
    private int violatedTemperatureCount;
    private boolean previousViolation;
    private ArrayList<Double> temperatureList;
    private boolean violatedWithLastTemp;
    private String reeferID;
    
    public ContainerTracker(int maxViloationAllowed, double maxTemperature) {
        this.maxViolationAllowed = maxViloationAllowed;
        this.violatedTemperatureCount=  0;
        this.maxTemperature = maxTemperature;
        this.previousViolation = false;
        this.temperatureList = new ArrayList<>();
        this.violatedWithLastTemp = false;
    }
    public ContainerTracker(double maxTemperature, int maxViloationAllowed, int violatedTemperatureCount) {
        this.maxTemperature = maxTemperature;
        this.maxViolationAllowed = maxViloationAllowed;
        this.violatedTemperatureCount = violatedTemperatureCount;
        this.previousViolation = false;
        this.temperatureList = new ArrayList<>();
        this.violatedWithLastTemp = false;
    }
    public ContainerTracker() {
        this.maxTemperature = 90;
        this.maxViolationAllowed = 5;
        this.violatedTemperatureCount = 0;
        this.previousViolation = false;
        this.temperatureList = new ArrayList<>();
        this.violatedWithLastTemp = false;
    }

    

    public boolean violateTemperatureThresholdOverTime() {
        if (violatedTemperatureCount >= maxViolationAllowed) {
            return true;
        } else {
            return false;
        }
    }

    public ContainerTracker update(final String key, final TelemetryEvent telemetryEvent) {
        this.setReeferID(key);
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

    public int getMaxViolationAllowed() {
        return maxViolationAllowed;
    }

    public void setMaxViolationAllowed(int maxViolationAllowed) {
        this.maxViolationAllowed = maxViolationAllowed;
    }

    public String getReeferID() {
        return reeferID;
    }

    public void setReeferID(String reeferID) {
        this.reeferID = reeferID;
    }

    @Override
    public String toString() {
        return "ContainerTracker [ maxTemperature=" + maxTemperature + ", maxViolationAllowed=" + maxViolationAllowed
                + ", previousViolation=" + previousViolation + ", reeferID=" + reeferID + ", temperatureList="
                + temperatureList + ", violatedTemperatureCount=" + violatedTemperatureCount + ", violatedWithLastTemp="
                + violatedWithLastTemp + " ]";
    }

    
}