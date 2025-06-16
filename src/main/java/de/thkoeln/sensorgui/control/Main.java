package de.thkoeln.sensorgui.control;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.thkoeln.sensordaten.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Main {
    @FXML
    private ImageView mqttLogo;

    @FXML
    private ImageView restLogo;

    public static List<MQTTSensor> MQTTSensorList = new ArrayList<>();
    public static List<RESTSensor> RESTSensorList = new ArrayList<>();
    private final Map<SubMQTTSensor, Thread> simulationThreads = new HashMap<>();

    @FXML
    private void initialize() {
        mqttLogo.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/mqtt_logo.png"))));
        restLogo.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/rest_logo.png"))));
    }

    @FXML
    private void handleNeu() {
        openSensorWindow("/erstellen.fxml", " Sensor erstellen");
    }

    @FXML
    private void handlebearbeiten() {
        openSensorWindow("/SensorListView.fxml", " Sensor bearbeiten");
    }

    @FXML
    private void handleSimulieren() {
        if (MQTTSensorList.isEmpty() && RESTSensorList.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Keine Sensoren vorhanden");
            alert.setContentText("Bitte erstellen Sie mindestens einen MQTT oder REST Sensor bevor Sie die Simulation starten.");
            alert.showAndWait();
            return;
        }

        if (!RESTSensorList.isEmpty()) {
            try {
                DynamicSensorServer.startServer(RESTSensorList);
            } catch (Exception e) {
            }
        }

        for (MQTTSensor mqttSensor : MQTTSensorList) {
            for (SubSensor subSensor : mqttSensor.getSubSensors()) {
                SubMQTTSensor subMqttSensor = (SubMQTTSensor) subSensor;

                subMqttSensor.setFullTopic(mqttSensor.getBaseTopic() + "/" + mqttSensor.getSensorName() + "/" + subMqttSensor.getSubTopic());
                subMqttSensor.setBrokerUrl(mqttSensor.getBroker());

                if (!simulationThreads.containsKey(subSensor)) {
                    Thread simulationThread = new Thread(subMqttSensor);
                    simulationThread.setDaemon(true);
                    simulationThread.start();
                    simulationThreads.put(subMqttSensor, simulationThread);
                }
            }
        }
        stopSimulationButton.setDisable(false);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Simulation gestartet");
        alert.setHeaderText(null);
        alert.setContentText("Die Simulation wurde erfolgreich gestartet.\n" +
                "MQTT Sensoren: " + MQTTSensorList.size() + "\n" +
                "REST Sensoren: " + RESTSensorList.size());
        alert.showAndWait();
    }

    @FXML
    private void handleStopSimulation() {
        try {
            DynamicSensorServer.stopServer();
            stopSimulationButton.setDisable(true);
            for (Thread thread : simulationThreads.values()) {
                thread.interrupt();
            }
            simulationThreads.clear();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Server Stopped");
            alert.setHeaderText(null);
            alert.setContentText("Simulation has been stopped successfully.");
            alert.showAndWait();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to stop simulation");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private Button stopSimulationButton;

    private void openSensorWindow(String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(loader.load()));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void saveSensorsToFile() {
        if (MQTTSensorList.isEmpty() && RESTSensorList.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Keine Sensoren vorhanden");
            alert.setContentText("Bitte erstellen Sie mindestens einen Sensor bevor Sie speichern.");
            alert.showAndWait();
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Speicherort auswählen");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON-Dateien", "*.json"));

        File file = fileChooser.showSaveDialog(new Stage());
        if (file != null) {
            saveAsJson(file);
            showAlertForSavedSensors(file);
        }
    }

    private void saveAsJson(File file) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            var sensors = new HashMap<String, Object>();
            sensors.put("MQTTSensors", MQTTSensorList);
            sensors.put("RESTSensors", RESTSensorList);

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, sensors);

            System.out.println("Sensoren erfolgreich gespeichert: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Fehler beim Speichern der Datei.");
        }
    }

    private void showAlertForSavedSensors(File file) {
        int mqttSensorCount = MQTTSensorList.size();
        int restSensorCount = RESTSensorList.size();
        int totalSensorCount = mqttSensorCount + restSensorCount;

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sensoren gespeichert");
        alert.setHeaderText("Speichervorgang abgeschlossen");
        alert.setContentText("Die Sensoren wurden erfolgreich gespeichert:\n" +
                "- Datei: " + file.getName() + "\n" +
                "- Gesamte Sensoren: " + totalSensorCount + "\n" +
                "- MQTT-Sensoren: " + mqttSensorCount + "\n" +
                "- REST-Sensoren: " + restSensorCount);
        alert.showAndWait();
    }

    public void loadSensorsFromFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("JSON-Datei auswählen");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON-Dateien", "*.json"));

        File file = fileChooser.showOpenDialog(new Stage());
        if (file != null) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();

                Map<String, List<Map<String, Object>>> sensorsMap =
                        objectMapper.readValue(file, new TypeReference<Map<String, List<Map<String, Object>>>>() {
                        });

                if (sensorsMap.containsKey("MQTTSensors")) {
                    List<Map<String, Object>> mqttSensorData = sensorsMap.get("MQTTSensors");
                    for (Map<String, Object> sensorMap : mqttSensorData) {
                        try {
                            List<Map<String, Object>> subSensors =
                                    (List<Map<String, Object>>) sensorMap.get("subSensors");

                            if (subSensors != null && !subSensors.isEmpty()) {
                                Map<String, Object> firstSubSensorMap = subSensors.get(0);
                                SubMQTTSensor firstSubSensor = new SubMQTTSensor(
                                        (String) firstSubSensorMap.get("subTopic"),
                                        (String) firstSubSensorMap.get("datenTyp"),
                                        ((Number) firstSubSensorMap.get("wertIntervalMin")).doubleValue(),
                                        ((Number) firstSubSensorMap.get("wertIntervalMax")).doubleValue(),
                                        ((Number) firstSubSensorMap.get("zeitIntervall")).intValue(),
                                        (Boolean) firstSubSensorMap.get("zeitStempel")
                                );

                                MQTTSensor sensor = new MQTTSensor(
                                        (String) sensorMap.get("sensorName"),
                                        (String) sensorMap.get("broker"),
                                        (String) sensorMap.get("baseTopic"),
                                        firstSubSensor
                                );

                                for (int i = 1; i < subSensors.size(); i++) {
                                    Map<String, Object> additionalSubSensorMap = subSensors.get(i);
                                    SubMQTTSensor additionalSubSensor = new SubMQTTSensor(
                                            (String) additionalSubSensorMap.get("subTopic"),
                                            (String) additionalSubSensorMap.get("datenTyp"),
                                            ((Number) additionalSubSensorMap.get("wertIntervalMin")).doubleValue(),
                                            ((Number) additionalSubSensorMap.get("wertIntervalMax")).doubleValue(),
                                            ((Number) additionalSubSensorMap.get("zeitIntervall")).intValue(),
                                            (Boolean) additionalSubSensorMap.get("zeitStempel")
                                    );
                                    sensor.addSubSensor(additionalSubSensor);
                                }

                                MQTTSensorList.add(sensor);
                            }
                        } catch (Exception e) {
                            System.err.println("Fehler beim Laden eines MQTT-Sensors: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }

                if (sensorsMap.containsKey("RESTSensors")) {
                    List<Map<String, Object>> restSensorData = sensorsMap.get("RESTSensors");
                    for (Map<String, Object> sensorMap : restSensorData) {
                        try {
                            List<Map<String, Object>> subSensors =
                                    (List<Map<String, Object>>) sensorMap.get("subSensors");

                            if (subSensors != null && !subSensors.isEmpty()) {
                                Map<String, Object> firstSubSensorMap = subSensors.get(0);
                                SubRESTSensor firstSubSensor = new SubRESTSensor(
                                        (String) firstSubSensorMap.get("unterPfad"),
                                        (String) firstSubSensorMap.get("datenTyp"),
                                        ((Number) firstSubSensorMap.get("wertIntervalMin")).doubleValue(),
                                        ((Number) firstSubSensorMap.get("wertIntervalMax")).doubleValue(),
                                        (Boolean) firstSubSensorMap.get("zeitStempel")
                                );

                                RESTSensor sensor = new RESTSensor(
                                        (String) sensorMap.get("sensorName"),
                                        (String) sensorMap.get("restPath"),
                                        firstSubSensor
                                );

                                for (int i = 1; i < subSensors.size(); i++) {
                                    Map<String, Object> additionalSubSensorMap = subSensors.get(i);
                                    SubRESTSensor additionalSubSensor = new SubRESTSensor(
                                            (String) additionalSubSensorMap.get("unterPfad"),
                                            (String) additionalSubSensorMap.get("datenTyp"),
                                            ((Number) additionalSubSensorMap.get("wertIntervalMin")).doubleValue(),
                                            ((Number) additionalSubSensorMap.get("wertIntervalMax")).doubleValue(),
                                            (Boolean) additionalSubSensorMap.get("zeitStempel")
                                    );
                                    sensor.addSubSensor(additionalSubSensor);
                                }

                                RESTSensorList.add(sensor);
                            }
                        } catch (Exception e) {
                            System.err.println("Fehler beim Laden eines REST-Sensors: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
                showAlertForLoadedSensors();
            } catch (Exception e) {
                System.err.println("Fehler beim Laden der Datei:");
                e.printStackTrace();
            }
        }
    }

    public void showAlertForLoadedSensors() {
        int mqttSensorCount = MQTTSensorList.size();
        int restSensorCount = RESTSensorList.size();
        int totalSensorCount = mqttSensorCount + restSensorCount;

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sensoren geladen");
        alert.setHeaderText("Ladevorgang abgeschlossen");
        alert.setContentText("Es wurden insgesamt " + totalSensorCount + " Sensoren hinzugefügt:\n" +
                "- MQTT-Sensoren: " + mqttSensorCount + "\n" +
                "- REST-Sensoren: " + restSensorCount);
        alert.showAndWait();
    }
    @FXML
    private void handleGenerieren() {
        if (MQTTSensorList.isEmpty() && RESTSensorList.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Keine Sensoren vorhanden");
            alert.setContentText("Bitte erstellen Sie mindestens einen Sensor bevor Sie die Klassen generieren.");
            alert.showAndWait();
            return;
        }

        String outputPath = "generatedClasses";
        new File(outputPath).mkdirs();

        try {
            List<String> mqttSensorClassNames = new ArrayList<>();
            List<String> restSensorClassNames = new ArrayList<>();

            for (MQTTSensor mqttSensor : MQTTSensorList) {
                String className = mqttSensor.getSensorName().replaceAll("\\s+", "");
                mqttSensorClassNames.add(className);
                String classContent = generateMqttClass(mqttSensor);
                File classFile = new File(outputPath, className + ".java");
                try (FileWriter writer = new FileWriter(classFile)) {
                    writer.write(classContent);
                }
            }

            for (RESTSensor restSensor : RESTSensorList) {
                String className = restSensor.getSensorName().replaceAll("\\s+", "");
                restSensorClassNames.add(className);
                String classContent = generateRestClass(restSensor);
                File classFile = new File(outputPath, className + ".java");
                try (FileWriter writer = new FileWriter(classFile)) {
                    writer.write(classContent);
                }
            }

            String mainClassContent = generateMainClass(mqttSensorClassNames, restSensorClassNames);
            File mainClassFile = new File(outputPath, "SimpleClient.java");
            try (FileWriter writer = new FileWriter(mainClassFile)) {
                writer.write(mainClassContent);
            }

            showAlert(Alert.AlertType.INFORMATION, "Success", "Java classes generated successfully!",
                    "Classes are located in: " + new File(outputPath).getAbsolutePath());

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "File Generation Error",
                    "Could not generate Java classes: " + e.getMessage());
        }
    }

    private String generateMainClass(List<String> mqttSensorNames, List<String> restSensorNames) {
        StringBuilder classBuilder = new StringBuilder();
        classBuilder.append("package simpleclient.program.normalClients;\n\n")
                .append("import sensorframework.main.SensorFramework;\n")
                .append("import sensorframework.sensor.Sensor;\n")
                .append("import simpleclient.sensors.json.normalSensors.*;\n")
                .append("import java.util.ArrayList;\n")
                .append("import java.util.HashMap;\n\n")
                .append("public class SimpleClient {\n")
                .append("    public static void main(String[] args) throws InterruptedException {\n")
                .append("        // Get instance of main class of the sensor framework\n")
                .append("        SensorFramework thisFramework = new SensorFramework();\n\n")
                .append("        // MQTT Sensors:\n");

        for (String sensorName : mqttSensorNames) {
            classBuilder.append("        Sensor ").append(sensorName.toLowerCase())
                    .append(" = new ").append(sensorName)
                    .append("(\"").append(sensorName).append("\");\n");
        }

        classBuilder.append("\n        // REST Sensors:\n");

        for (String sensorName : restSensorNames) {
            classBuilder.append("        Sensor ").append(sensorName.toLowerCase())
                    .append(" = new ").append(sensorName)
                    .append("(\"").append(sensorName).append("\");\n");
        }

        classBuilder.append("\n        // Add all sensors to list\n")
                .append("        ArrayList<Sensor> allSensors = new ArrayList<>();\n");

        for (String sensorName : mqttSensorNames) {
            classBuilder.append("        allSensors.add(").append(sensorName.toLowerCase()).append(");\n");
        }
        for (String sensorName : restSensorNames) {
            classBuilder.append("        allSensors.add(").append(sensorName.toLowerCase()).append(");\n");
        }

        classBuilder.append("\n        // Start the framework with the list of sensor objects\n")
                .append("        thisFramework.startNewSensors(allSensors);\n\n")
                .append("        // Wait for 5 seconds to collect some data\n")
                .append("        Thread.sleep(5000);\n\n")
                .append("        // Print values for each sensor\n");

        for (String sensorName : mqttSensorNames) {
            addSensorValuePrinting(classBuilder, sensorName);
        }
        for (String sensorName : restSensorNames) {
            addSensorValuePrinting(classBuilder, sensorName);
        }

        classBuilder.append("\n        // Save data and stop sensors\n")
                .append("        thisFramework.writeStorageToFile(\"data.json\");\n")
                .append("        thisFramework.stopAllSensors();\n")
                .append("    }\n")
                .append("}\n");

        return classBuilder.toString();
    }

    private void addSensorValuePrinting(StringBuilder classBuilder, String sensorName) {
        classBuilder.append("\n        System.out.println(\"Values from sensor ").append(sensorName).append(":\");\n")
                .append("        HashMap<String, ArrayList<String>> values").append(sensorName)
                .append(" = thisFramework.getSensorValues(").append(sensorName.toLowerCase()).append(".getName());\n")
                .append("        for (String subSensorName : values").append(sensorName).append(".keySet()) {\n")
                .append("            ArrayList<String> arrayList = values").append(sensorName).append(".get(subSensorName);\n")
                .append("            System.out.println(subSensorName + \": \");\n")
                .append("            for (String value : arrayList) {\n")
                .append("                System.out.println(value);\n")
                .append("            }\n")
                .append("        }\n")
                .append("        System.out.println();\n");
    }

    private String generateMqttClass(MQTTSensor sensor) {
        StringBuilder classBuilder = new StringBuilder();
        String className = sensor.getSensorName().replaceAll("\\s+", "");

        classBuilder.append("package simpleclient.sensors.json.normalSensors;\n\n")
                .append("import sensorframework.annotations.MqttSensor;\n")
                .append("import sensorframework.annotations.ValueInfo;\n")
                .append("import sensorframework.enums.DataType;\n")
                .append("import sensorframework.sensor.Sensor;\n\n")
                .append("@MqttSensor(brokerName = \"").append(sensor.getBroker()).append("\", ")
                .append("baseTopic = \"").append(sensor.getBaseTopic()).append("\")\n")
                .append("public class ").append(className)
                .append(" extends Sensor {\n\n");

        int valueCounter = 1;
        for (SubSensor subSensor : sensor.getSubSensors()) {
            SubMQTTSensor mqttSubSensor = (SubMQTTSensor) subSensor;
            String dataType = mqttSubSensor.getDatenTyp().toUpperCase();
            if (dataType.equals("INTEGER")) {
                dataType = "INT";
            }
            classBuilder.append("    @ValueInfo(name = \"").append(mqttSubSensor.getSubTopic())
                    .append("\", dataType = DataType.").append(dataType)
                    .append(", meaning = \"Sensor measurement for ").append(mqttSubSensor.getSubTopic())
                    .append("\", topic = \"").append(mqttSubSensor.getSubTopic())
                    .append("\")\n")
                    .append("    private double value").append(valueCounter).append(";\n\n");
            valueCounter++;
        }

        classBuilder.append("    public ").append(className)
                .append("(String name) {\n")
                .append("        super(name);\n")
                .append("    }\n\n")
                .append("}\n");

        return classBuilder.toString();
    }

    private String generateRestClass(RESTSensor sensor) {
        StringBuilder classBuilder = new StringBuilder();
        String className = sensor.getSensorName().replaceAll("\\s+", "");

        classBuilder.append("package simpleclient.sensors.json.normalSensors;\n\n")
                .append("import sensorframework.annotations.RestSensor;\n")
                .append("import sensorframework.annotations.ValueInfo;\n")
                .append("import sensorframework.enums.DataType;\n")
                .append("import sensorframework.sensor.Sensor;\n\n")
                .append("@RestSensor(serverUrl = \"").append(sensor.getRestPath()).append("\")\n")
                .append("public class ").append(className)
                .append(" extends Sensor {\n\n");

        int valueCounter = 1;
        for (SubSensor subSensor : sensor.getSubSensors()) {
            SubRESTSensor restSubSensor = (SubRESTSensor) subSensor;
            String dataType = restSubSensor.getDatenTyp().toUpperCase();
            if (dataType.equals("INTEGER")) {
                dataType = "INT";
            }
            classBuilder.append("    @ValueInfo(name = \"").append(restSubSensor.getUnterPfad())
                    .append("\", dataType = DataType.").append(dataType)
                    .append(", meaning = \"Sensor measurement for ").append(restSubSensor.getUnterPfad())
                    .append("\", path = \"").append(restSubSensor.getUnterPfad())
                    .append("\")\n")
                    .append("    private double value").append(valueCounter).append(";\n\n");
            valueCounter++;
        }

        classBuilder.append("    public ").append(className)
                .append("(String name) {\n")
                .append("        super(name);\n")
                .append("    }\n\n")
                .append("}\n");

        return classBuilder.toString();
    }

    private void showAlert(Alert.AlertType alertType, String title, String headerText, String contentText) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
    }
}