package de.thkoeln.sensordaten;
public  class SensorResponse {
    private String timestamp;
    private Object value;

    public SensorResponse() {}

    public SensorResponse(String timestamp, Object value) {
        this.timestamp = timestamp;
        this.value = value;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
