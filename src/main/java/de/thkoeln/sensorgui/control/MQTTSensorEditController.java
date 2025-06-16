package de.thkoeln.sensorgui.control;

import de.thkoeln.sensordaten.*;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public class MQTTSensorEditController {
    @FXML
    private TextField nameField;
    @FXML
    private ComboBox<String> brokerField;
    @FXML
    private TextField baseTopicField;
    @FXML
    private VBox subSensorContainer;

    private MQTTSensor currentSensor;
    private int sensorIndex;

    private final List<String> knownBrokers = List.of(
            "ssl://broker.emqx.io:8883"
    );
    private TextFormatter<String> createCapitalizeFormatter() {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            if (change.getControlNewText().isEmpty()) {
                return change;
            }

            if (change.getControlNewText().length() == 1) {
                change.setText(change.getText().toUpperCase());
            } else if (change.getControlNewText().length() > change.getControlText().length()) {
                int pos = change.getRangeStart();
                if (pos == 0) {
                    change.setText(change.getText().substring(0, 1).toUpperCase() +
                            change.getText().substring(1));
                }
            }
            return change;
        };

        return new TextFormatter<>(filter);
    }
    @FXML
    private void initialize() {
        nameField.setTextFormatter(createCapitalizeFormatter());
        baseTopicField.setTextFormatter(createCapitalizeFormatter());
        brokerField.getItems().addAll(knownBrokers);
        brokerField.setEditable(true);
        brokerField.setPromptText("Broker-Adresse auswählen oder eingeben");

        brokerField.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
        });
    }





    public void initializeSensor(MQTTSensor sensor, int index) {
        this.currentSensor = sensor;
        this.sensorIndex = index;

        nameField.setText(sensor.getSensorName());
        brokerField.setValue(sensor.getBroker());
        baseTopicField.setText(sensor.getBaseTopic());

        populateSubSensors(sensor.getSubSensors());
    }

    private void populateSubSensors(List<SubSensor> subSensors) {
        subSensorContainer.getChildren().clear();

        for (SubSensor subSensor : subSensors) {
            if (subSensor instanceof SubMQTTSensor mqttSubSensor) {
                VBox subSensorBox = createSubSensorBox(mqttSubSensor);
                subSensorContainer.getChildren().add(subSensorBox);
            }
        }
        updateDeleteButtonVisibility();
    }

    private TextField createCapitalizedTextField(String initialValue, String promptText) {
        TextField textField = new TextField(initialValue);
        textField.setPromptText(promptText);
        return textField;
    }

    private VBox createSubSensorBox(SubMQTTSensor subSensor) {
        VBox subSensorBox = new VBox(10);
        subSensorBox.setPadding(new Insets(5, 5, 5, 5));
        subSensorBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 1;");

        TextField subTopicField = createCapitalizedTextField(subSensor.getSubTopic(), "Sub-Topic");
        subTopicField.setTextFormatter(createCapitalizeFormatter());


        ComboBox<String> dataTypeComboBox = new ComboBox<>();
        dataTypeComboBox.getItems().addAll("Integer", "Double", "Float", "String", "Boolean");
        dataTypeComboBox.setValue(subSensor.getDatenTyp());

        Label rangeLabel = new Label("Wertebereich:");
        TextField minValueField = new TextField();
        TextField maxValueField = new TextField();

        if (!isStringOrBoolean(subSensor.getDatenTyp())) {
            minValueField.setText(String.valueOf(subSensor.getWertIntervalMin()));
            maxValueField.setText(String.valueOf(subSensor.getWertIntervalMax()));
        }

        minValueField.setPromptText("Min");
        minValueField.setMaxWidth(80);
        maxValueField.setPromptText("Max");
        maxValueField.setMaxWidth(80);

        HBox rangeBox = new HBox(10, rangeLabel, minValueField, maxValueField);

        TextField intervalField = createCapitalizedTextField(String.valueOf(subSensor.getZeitIntervall()), "Millisekunden");
        intervalField.setMaxWidth(80);

        HBox intervalBox = new HBox(10, new Label("Zeitintervall:"), intervalField);

        CheckBox addTimestampCheckbox = new CheckBox("Zeitstempel hinzufügen");
        addTimestampCheckbox.setSelected(subSensor.isZeitStempel());

        Button deleteButton = new Button("Löschen");
        deleteButton.setOnAction(e -> {
            subSensorContainer.getChildren().remove(subSensorBox);
            updateDeleteButtonVisibility();
        });

        dataTypeComboBox.setOnAction(event -> {
            boolean isStringOrBool = isStringOrBoolean(dataTypeComboBox.getValue());
            rangeBox.setVisible(!isStringOrBool);
            rangeBox.setManaged(!isStringOrBool);
            if (isStringOrBool) {
                minValueField.setText("");
                maxValueField.setText("");
            }
        });

        if (isStringOrBoolean(dataTypeComboBox.getValue())) {
            rangeBox.setVisible(false);
            rangeBox.setManaged(false);
            minValueField.setText("");
            maxValueField.setText("");
        }

        subSensorBox.getChildren().addAll(
                subTopicField,
                dataTypeComboBox,
                rangeBox,
                intervalBox,
                addTimestampCheckbox,
                deleteButton
        );

        return subSensorBox;
    }

    @FXML
    private void handleAddSubsensor() {
        VBox subSensorBox = new VBox(10);
        subSensorBox.setPadding(new Insets(5, 5, 5, 5));
        subSensorBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 1;");

        TextField subTopicField = createCapitalizedTextField("", "Sub-Topic");
        subTopicField.setTextFormatter(createCapitalizeFormatter());

        ComboBox<String> dataTypeComboBox = new ComboBox<>();
        dataTypeComboBox.getItems().addAll("Integer", "Double", "Float", "String", "Boolean");

        Label rangeLabel = new Label("Wertebereich:");
        TextField minValueField = new TextField();
        minValueField.setPromptText("Min");
        minValueField.setMaxWidth(80);

        TextField maxValueField = new TextField();
        maxValueField.setPromptText("Max");
        maxValueField.setMaxWidth(80);

        HBox rangeBox = new HBox(10, rangeLabel, minValueField, maxValueField);

        TextField intervalField = createCapitalizedTextField("", "Millisekunden");
        intervalField.setMaxWidth(80);

        HBox intervalBox = new HBox(10, new Label("Zeitintervall:"), intervalField);

        CheckBox addTimestampCheckbox = new CheckBox("Zeitstempel hinzufügen");

        Button deleteButton = new Button("Löschen");
        deleteButton.setOnAction(e -> {
            subSensorContainer.getChildren().remove(subSensorBox);
            updateDeleteButtonVisibility();
        });

        dataTypeComboBox.setOnAction(event -> {
            boolean shouldHideRangeBox = isStringOrBoolean(dataTypeComboBox.getValue());
            rangeBox.setVisible(!shouldHideRangeBox);
            rangeBox.setManaged(!shouldHideRangeBox);
        });

        subSensorBox.getChildren().addAll(
                subTopicField,
                dataTypeComboBox,
                rangeBox,
                intervalBox,
                addTimestampCheckbox,
                deleteButton
        );

        subSensorContainer.getChildren().add(subSensorBox);
        updateDeleteButtonVisibility();
    }

    private boolean isStringOrBoolean(String dataType) {
        return dataType != null && (dataType.equals("String") || dataType.equals("Boolean"));
    }

    private void updateDeleteButtonVisibility() {
        int subSensorCount = subSensorContainer.getChildren().size();
        for (int i = 0; i < subSensorCount; i++) {
            VBox subSensorBox = (VBox) subSensorContainer.getChildren().get(i);
            Button deleteButton = (Button) subSensorBox.getChildren().getLast();
            deleteButton.setVisible(subSensorCount > 1);
        }
    }

    @FXML
    private void handleSave() {
        if (!validateInputs()) {
            return;
        }

        currentSensor.setSensorName(nameField.getText());
        currentSensor.setBroker(brokerField.getValue());
        currentSensor.setBaseTopic(baseTopicField.getText());

        List<SubSensor> newSubSensors = new ArrayList<>();

        for (var child : subSensorContainer.getChildren()) {
            if (child instanceof VBox subSensorBox) {
                TextField subTopicField = (TextField) subSensorBox.getChildren().get(0);
                ComboBox<String> dataTypeComboBox = (ComboBox<String>) subSensorBox.getChildren().get(1);
                HBox rangeBox = (HBox) subSensorBox.getChildren().get(2);
                TextField minValueField = (TextField) rangeBox.getChildren().get(1);
                TextField maxValueField = (TextField) rangeBox.getChildren().get(2);
                HBox intervalBox = (HBox) subSensorBox.getChildren().get(3);
                TextField intervalField = (TextField) intervalBox.getChildren().get(1);
                CheckBox addTimestampCheckbox = (CheckBox) subSensorBox.getChildren().get(4);

                double minValue = 0;
                double maxValue = 0;

                if (!isStringOrBoolean(dataTypeComboBox.getValue())) {
                    minValue = Double.parseDouble(minValueField.getText());
                    maxValue = Double.parseDouble(maxValueField.getText());
                }

                SubMQTTSensor subSensor = new SubMQTTSensor(
                        subTopicField.getText(),
                        dataTypeComboBox.getValue(),
                        minValue,
                        maxValue,
                        Integer.parseInt(intervalField.getText()),
                        addTimestampCheckbox.isSelected()
                );

                newSubSensors.add(subSensor);
            }
        }

        currentSensor.getSubSensors().clear();
        currentSensor.getSubSensors().addAll(newSubSensors);

        Main.MQTTSensorList.set(sensorIndex, currentSensor);

        showInfoDialog();
        closeDialog();
    }


    private boolean validateInputs() {
        if (nameField.getText().trim().isEmpty()) {
            showErrorDialog("Fehler: Sensorname darf nicht leer sein.");
            return false;
        }

        if (brokerField.getValue() == null || brokerField.getValue().trim().isEmpty()) {
            showErrorDialog("Fehler: Broker-Adresse darf nicht leer sein.");
            return false;
        }
        

        if (baseTopicField.getText().trim().isEmpty()) {
            showErrorDialog("Fehler: Base Topic darf nicht leer sein.");
            return false;
        }

        if (subSensorContainer.getChildren().isEmpty()) {
            showErrorDialog("Fehler: Mindestens ein Sub-Sensor muss definiert werden.");
            return false;
        }

        for (var child : subSensorContainer.getChildren()) {
            if (child instanceof VBox subSensorBox) {
                TextField subTopicField = (TextField) subSensorBox.getChildren().get(0);
                subTopicField.setTextFormatter(createCapitalizeFormatter());
                if (subTopicField.getText().trim().isEmpty()) {
                    showErrorDialog("Fehler: Sub-Topic darf nicht leer sein.");
                    return false;
                }

                ComboBox<String> dataTypeComboBox = (ComboBox<String>) subSensorBox.getChildren().get(1);
                if (dataTypeComboBox.getValue() == null) {
                    showErrorDialog("Fehler: Datentyp muss ausgewählt werden.");
                    return false;
                }

                if (isStringOrBoolean(dataTypeComboBox.getValue())) {
                    continue;
                }

                HBox rangeBox = (HBox) subSensorBox.getChildren().get(2);
                TextField minValueField = (TextField) rangeBox.getChildren().get(1);
                TextField maxValueField = (TextField) rangeBox.getChildren().get(2);

                try {
                    double minValue = Double.parseDouble(minValueField.getText());
                    double maxValue = Double.parseDouble(maxValueField.getText());

                    if (minValue >= maxValue) {
                        showErrorDialog("Fehler: Minimaler Wert muss kleiner als maximaler Wert sein.");
                        return false;
                    }
                } catch (NumberFormatException e) {
                    showErrorDialog("Fehler: Wertebereich muss gültige Zahlen enthalten.");
                    return false;
                }

                HBox intervalBox = (HBox) subSensorBox.getChildren().get(3);
                TextField intervalField = (TextField) intervalBox.getChildren().get(1);

                try {
                    Integer.parseInt(intervalField.getText());
                } catch (NumberFormatException e) {
                    showErrorDialog("Fehler: Zeitintervall muss eine gültige Zahl sein.");
                    return false;
                }
            }
        }

        return true;
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private void showErrorDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ungültige Eingabe");
        alert.setHeaderText("Bitte korrigieren Sie die folgenden Fehler:");
        alert.setContentText(message);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setPrefSize(400, 200);
        alert.showAndWait();
    }

    private void showInfoDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Erfolgreich");
        alert.setHeaderText(null);
        alert.setContentText("MQTT-Sensor erfolgreich aktualisiert.");
        alert.showAndWait();
    }

    private void closeDialog() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
}