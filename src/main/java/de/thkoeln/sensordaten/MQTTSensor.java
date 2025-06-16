package de.thkoeln.sensordaten;


import java.util.List;

public class MQTTSensor extends Sensor {
    private String broker;
    private String baseTopic;

    public MQTTSensor(String sensorName, String broker, String baseTopic, SubMQTTSensor subMQTTSensor) {
        super(sensorName,  subMQTTSensor);
        this.broker = broker;
        this.baseTopic = baseTopic;
    }

    public String getBroker() {
        return broker;
    }

    public String getBaseTopic() {
        return baseTopic;
    }

    public void setBroker(String broker) {
        this.broker = broker;
    }

    public void setBaseTopic(String baseTopic) {
        this.baseTopic = baseTopic;
    }

    public String getSensorName() {
        return super.getSensorName();
    }


    public void setSensorName(String mqttSensorName) {
        super.setSensorName(mqttSensorName);
    }


    public void addSubSensor(SubMQTTSensor subMQTTSensor) {
        super.addSubSensor(subMQTTSensor);
    }


    public void delSubSensor(SubMQTTSensor subMQTTSensor) {
        super.delSubSensor(subMQTTSensor);
    }

    public List<SubSensor> getSubSensors() {
        return super.getSubSensors();
    }

}
