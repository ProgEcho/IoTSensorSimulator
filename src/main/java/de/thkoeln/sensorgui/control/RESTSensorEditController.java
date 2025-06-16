package de.thkoeln.sensorgui.control;

import de.thkoeln.sensordaten.RESTSensor;
import de.thkoeln.sensordaten.SubRESTSensor;
import de.thkoeln.sensordaten.SubSensor;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public class RESTSensorEditController {

    @FXML
    private TextField nameField;
    @FXML
    private ComboBox<String> baseUrlField;
    @FXML
    private VBox subSensorContainer;

    private RESTSensor currentSensor;
    private int sensorIndex;

    private final List<String> knownEndpoints = List.of(
            "http://localhost:8080"
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
        baseUrlField.getItems().addAll(knownEndpoints);
        baseUrlField.setEditable(true);
        baseUrlField.setPromptText("Basis-URL auswählen oder eingeben");

        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                nameField.setText(newValue.substring(0, 1).toUpperCase() + newValue.substring(1));
            }
        });
    }
    private void addCapitalizationListener(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                textField.setText(newValue.substring(0, 1).toUpperCase() + newValue.substring(1));
            }
        });
    }
    public void initializeSensor(RESTSensor sensor, int index) {
        this.currentSensor = sensor;
        this.sensorIndex = index;

        nameField.setText(sensor.getSensorName());
        baseUrlField.setValue(sensor.getRestPath());

        populateSubSensors(sensor.getSubSensors());
    }

    private void populateSubSensors(List<SubSensor> subSensors) {
        subSensorContainer.getChildren().clear();

        for (SubSensor subSensor : subSensors) {
            if (subSensor instanceof SubRESTSensor restSubSensor) {
                VBox subSensorBox = createSubSensorBox(restSubSensor);
                subSensorContainer.getChildren().add(subSensorBox);
            }
        }
        updateDeleteButtonVisibility();
    }

    private boolean isStringOrBoolean(String dataType) {
        return dataType != null && (dataType.equals("String") || dataType.equals("Boolean"));
    }

    private VBox createSubSensorBox(SubRESTSensor subSensor) {
        VBox subSensorBox = new VBox(10);
        subSensorBox.setPadding(new Insets(5, 5, 5, 5));
        subSensorBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 1;");

        TextField subPathField = new TextField(subSensor.getUnterPfad());
        subPathField.setPromptText("Sub-Path");
        addCapitalizationListener(subPathField);
        subPathField.setTextFormatter(createCapitalizeFormatter());



        ComboBox<String> dataTypeComboBox = new ComboBox<>();
        dataTypeComboBox.getItems().addAll("Integer", "Double", "Float", "String", "Boolean");
        dataTypeComboBox.setValue(subSensor.getDatenTyp());

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

        HBox rangeBox = new HBox(10, new Label("Wertebereich:"), minValueField, maxValueField);

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
                subPathField,
                dataTypeComboBox,
                rangeBox,
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

        TextField subPathField = new TextField();
        subPathField.setPromptText("Sub-Path");
        subPathField.setTextFormatter(createCapitalizeFormatter());


        ComboBox<String> dataTypeComboBox = new ComboBox<>();
        dataTypeComboBox.getItems().addAll("Integer", "Double", "Float", "String", "Boolean");

        TextField minValueField = new TextField();
        minValueField.setPromptText("Min");
        minValueField.setMaxWidth(80);

        TextField maxValueField = new TextField();
        maxValueField.setPromptText("Max");
        maxValueField.setMaxWidth(80);

        HBox rangeBox = new HBox(10, new Label("Wertebereich:"), minValueField, maxValueField);

        CheckBox addTimestampCheckbox = new CheckBox("Zeitstempel hinzufügen");

        Button deleteButton = new Button("Löschen");
        deleteButton.setOnAction(e -> {
            subSensorContainer.getChildren().remove(subSensorBox);
            updateDeleteButtonVisibility();
        });

        dataTypeComboBox.setOnAction(event -> {
            boolean shouldHideRangeBox = dataTypeComboBox.getValue() != null &&
                    (dataTypeComboBox.getValue().equals("String") ||
                            dataTypeComboBox.getValue().equals("Boolean"));
            rangeBox.setVisible(!shouldHideRangeBox);
            rangeBox.setManaged(!shouldHideRangeBox);
        });

        if (dataTypeComboBox.getValue() != null && dataTypeComboBox.getValue().equals("String")) {
            rangeBox.setVisible(false);
            rangeBox.setManaged(false);
        }

        subSensorBox.getChildren().addAll(
                subPathField,
                dataTypeComboBox,
                rangeBox,
                addTimestampCheckbox,
                deleteButton
        );

        subSensorContainer.getChildren().add(subSensorBox);
        updateDeleteButtonVisibility();
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
        currentSensor.setRestPath(baseUrlField.getValue());

        List<SubSensor> newSubSensors = new ArrayList<>();
        for (var child : subSensorContainer.getChildren()) {
            if (child instanceof VBox subSensorBox) {
                try {
                    SubRESTSensor subSensor = extractSubSensorFromBox(subSensorBox);
                    newSubSensors.add(subSensor);
                } catch (IllegalArgumentException e) {
                    showErrorDialog(e.getMessage());
                    return;
                }
            }
        }

        currentSensor.getSubSensors().clear();
        currentSensor.getSubSensors().addAll(newSubSensors);

        Main.RESTSensorList.set(sensorIndex, currentSensor);
        showInfoDialog();
        closeDialog();
    }

    private SubRESTSensor extractSubSensorFromBox(VBox subSensorBox) throws IllegalArgumentException {
        TextField subPathField = (TextField) subSensorBox.getChildren().get(0);
        ComboBox<String> dataTypeComboBox = (ComboBox<String>) subSensorBox.getChildren().get(1);
        HBox rangeBox = (HBox) subSensorBox.getChildren().get(2);
        TextField minValueField = (TextField) rangeBox.getChildren().get(1);
        TextField maxValueField = (TextField) rangeBox.getChildren().get(2);
        CheckBox addTimestampCheckbox = (CheckBox) subSensorBox.getChildren().get(3);

        double minValue = 0;
        double maxValue = 0;

        if (!isStringOrBoolean(dataTypeComboBox.getValue())) {
            try {
                minValue = Double.parseDouble(minValueField.getText());
                maxValue = Double.parseDouble(maxValueField.getText());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Werte für Min und Max müssen gültige Zahlen sein.");
            }

            if (minValue >= maxValue) {
                throw new IllegalArgumentException("Minimaler Wert muss kleiner als Maximalwert sein.");
            }
        }

        return new SubRESTSensor(
                subPathField.getText(),
                dataTypeComboBox.getValue(),
                minValue,
                maxValue,
                addTimestampCheckbox.isSelected()
        );
    }

    private boolean validateInputs() {
        if (nameField.getText().trim().isEmpty()) {
            showErrorDialog("Fehler: Sensorname darf nicht leer sein.");
            return false;
        }

        if (baseUrlField.getValue() == null || baseUrlField.getValue().trim().isEmpty()) {
            showErrorDialog("Fehler: Basis-URL darf nicht leer sein.");
            return false;
        }

        if (subSensorContainer.getChildren().isEmpty()) {
            showErrorDialog("Fehler: Mindestens ein Sub-Sensor muss definiert werden.");
            return false;
        }

        for (var child : subSensorContainer.getChildren()) {
            if (child instanceof VBox subSensorBox) {
                TextField subPathField = (TextField) subSensorBox.getChildren().get(0);
                subPathField.setTextFormatter(createCapitalizeFormatter());
                if (subPathField.getText().trim().isEmpty()) {
                    showErrorDialog("Fehler: Sub-Path darf nicht leer sein.");
                    return false;
                }

                ComboBox<String> dataTypeComboBox = (ComboBox<String>) subSensorBox.getChildren().get(1);
                if (dataTypeComboBox.getValue() == null) {
                    showErrorDialog("Fehler: Datentyp muss ausgewählt werden.");
                    return false;
                }

                if (!isStringOrBoolean(dataTypeComboBox.getValue())) {
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
        alert.showAndWait();
    }

    private void showInfoDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Erfolgreich");
        alert.setHeaderText(null);
        alert.setContentText("REST-Sensor erfolgreich aktualisiert.");
        alert.showAndWait();
    }

    private void closeDialog() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
}