package ibm.gse.eda.vaccine.coldchainagent.domain;

import java.util.ArrayList;

import ibm.gse.eda.vaccine.coldchainagent.infrastructure.TelemetryEvent;

public class ContainerTracker {
    private double maxTemperature;
    private int maxViloationAllowed;
    private int violatedTemperatureCount;
    private boolean previousViolation;
    private ArrayList<Double> temperatureList;
    public ContainerTracker(int maxViloationAllowed, double maxTemperature) {
        this.maxViloationAllowed = maxViloationAllowed;
        this.violatedTemperatureCount=  0;
        this.maxTemperature = maxTemperature;
        this.previousViolation = false;
        this.temperatureList = new ArrayList<>();
    }
    public ContainerTracker(double maxTemperature, int maxViloationAllowed, int violatedTemperatureCount) {
        this.maxTemperature = maxTemperature;
        this.maxViloationAllowed = maxViloationAllowed;
        this.violatedTemperatureCount = violatedTemperatureCount;
        this.previousViolation = false;
        this.temperatureList = new ArrayList<>();

    }
    public ContainerTracker() {
        this.maxTemperature = 90;
        this.maxViloationAllowed = 5;
        this.violatedTemperatureCount = 0;
        this.previousViolation = false;
        this.temperatureList = new ArrayList<>();

    }

    public boolean violateTemperatureThresholdOverTime() {
        if (violatedTemperatureCount >= maxViloationAllowed) {
            return true;
        } else {
            return false;
        }
    }

    public ContainerTracker update(final TelemetryEvent telemetryEvent) {
        if (!previousViolation){
            if (telemetryEvent.payload.temperature >=  this.maxTemperature){
                this.temperatureList.add(telemetryEvent.payload.temperature);
                this.violatedTemperatureCount++;
                previousViolation = violateTemperatureThresholdOverTime();
            }else{
                this.temperatureList = new ArrayList<>();
                this.violatedTemperatureCount = 0;
            }
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


}