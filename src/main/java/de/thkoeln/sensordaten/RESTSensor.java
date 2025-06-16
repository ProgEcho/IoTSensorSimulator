package de.thkoeln.sensordaten;

import java.util.List;

public class RESTSensor extends Sensor {
private String restPath;
    public RESTSensor(String sensorName, String restPath, SubRESTSensor subRESTSensor) {
        super(sensorName, subRESTSensor);
        this.restPath = restPath;
    }



    public void setSensorName(String restSensorName) {
        super.setSensorName(restSensorName);
    }

    public String getSensorName() {
        return super.getSensorName();
    }

    public String getRestPath() {
        return restPath;
    }

    public void setRestPath(String restPath) {
        this.restPath = restPath;
    }

    public void addSubSensor(SubRESTSensor subRESTSensor) {
        super.addSubSensor(subRESTSensor);
    }

    public void delSubSensor(SubRESTSensor subRESTSensor) {
        super.delSubSensor(subRESTSensor);
    }

    public List<SubSensor> getSubSensors() {
        return super.getSubSensors();
    }
}
