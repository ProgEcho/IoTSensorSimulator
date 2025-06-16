package de.thkoeln.sensordaten;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.*;


import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SubMQTTSensor extends SubSensor implements Runnable{
    private String subTopic;
    private String datenTyp;
    private double wertIntervalMax;
    private double wertIntervalMin;
    private int zeitIntervall;
    private boolean zeitStempel;
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private String fullTopic;
    private String brokerUrl;

    public SubMQTTSensor(String subTopic, String datenTyp, double wertIntervalMin, double wertIntervalMax, int zeitIntervall, boolean zeitStempel) {
        this.subTopic = subTopic;
        this.datenTyp = datenTyp;
        this.wertIntervalMin = wertIntervalMin;
        this.wertIntervalMax = wertIntervalMax;
        this.zeitIntervall = zeitIntervall;
        this.zeitStempel = zeitStempel;
    }

    public String getSubTopic() {

        return subTopic;
    }

    public String getDatenTyp() {
        return datenTyp;
    }

    public double getWertIntervalMax() {
        return wertIntervalMax;
    }

    public double getWertIntervalMin() {
        return wertIntervalMin;
    }

    public int getZeitIntervall() {
        return zeitIntervall;
    }

    public boolean isZeitStempel() {
        return zeitStempel;
    }
    public void setZeitStempel(boolean zeitStempel) {
        this.zeitStempel = zeitStempel;
    }

    public void setZeitIntervall(int zeitIntervall) {
        this.zeitIntervall = zeitIntervall;
    }

    public void setWertIntervalMin(double wertIntervalMin) {
        this.wertIntervalMin = wertIntervalMin;
    }

    public void setWertIntervalMax(double wertIntervalMax) {
        this.wertIntervalMax = wertIntervalMax;
    }

    public void setDatenTyp(String datenTyp) {
        this.datenTyp = datenTyp;
    }

    public void setSubTopic(String subTopic) {
        this.subTopic = subTopic;
    }

    public void setFullTopic(String fullTopic) { this.fullTopic = fullTopic; }
    public void setBrokerUrl(String brokerUrl) { this.brokerUrl = brokerUrl; }

    public Object generateDaten() {
        switch (this.getDatenTyp().toLowerCase()) {

            case "integer":
                return  new Random().nextInt ((int) (this.getWertIntervalMax() - this.getWertIntervalMin() - 1) ) + (int) this.getWertIntervalMin();
            case "double":
                return  new Random().nextDouble() * (this.getWertIntervalMax() - this.getWertIntervalMin()) + this.getWertIntervalMin();
            case "float":
                return  (float) (new Random().nextDouble() * (this.getWertIntervalMax() - this.getWertIntervalMin()) + this.getWertIntervalMin());
            case "string":
                byte[] array = new byte[8];

                new Random().nextBytes(array);
                return new String(array, StandardCharsets.UTF_8);
            case "boolean":
                return  new Random().nextBoolean();
        }
        throw new IllegalArgumentException("Unsupported datenTyp: " + getDatenTyp());
    }

    @Override
    public void run() {
        MqttClient client = null;

        try {
            client = new MqttClient(this.brokerUrl, MqttClient.generateClientId());

            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            client.connect(options);

            while (!Thread.currentThread().isInterrupted()) {
                String timestamp = null;
                if (isZeitStempel()) {
                    timestamp = LocalDateTime.now().format(formatter);
                }
                Map<String, Object> messageMap = new HashMap<>();
                messageMap.put("timestamp", timestamp);
                messageMap.put("value", generateDaten());
                String messageAsJSON = new ObjectMapper().writeValueAsString(messageMap);

                MqttMessage message = new MqttMessage(messageAsJSON.getBytes(StandardCharsets.UTF_8));
                message.setQos(1);

                client.publish(this.fullTopic, message);

                Thread.sleep(this.getZeitIntervall());

            }

        } catch (MqttException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore the interrupt flag
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } finally {
            if (client != null) {
                try {
                    if (client.isConnected()) {
                        client.disconnect();
                        System.out.println("Disconnected from broker!");
                    }
                } catch (MqttException e) {
                    System.err.println("Error during MQTT client cleanup: " + e.getMessage());
                    e.printStackTrace();
                }
                try {
                    client.close();
                } catch (MqttException e) {
                    System.err.println("Error closing MQTT client: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
}
