package de.thkoeln.sensordaten;

import java.util.ArrayList;
import java.util.List;

public abstract class Sensor {

    private String sensorName;
    private List <SubSensor> subSensors = new ArrayList<>();

    public Sensor(String sensorName, SubSensor subSensor) {
        this.sensorName = sensorName;
        this.subSensors.add(subSensor);
    }

    public String getSensorName() {
        return this.sensorName;
    }

    public void setSensorName(String sensorName) {
        this.sensorName = sensorName;
    }

    public void addSubSensor (SubSensor subSensor) {
        this.subSensors.add(subSensor);
    }

    public void delSubSensor (SubSensor subSensor) {
        this.subSensors.remove(subSensor);
    }

    public List<SubSensor> getSubSensors() {
        return this.subSensors;
    }
}
