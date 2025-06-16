package de.thkoeln.sensorgui.control;

import de.thkoeln.sensordaten.RESTSensor;
import de.thkoeln.sensordaten.SubRESTSensor;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.TextFormatter;
import java.util.function.UnaryOperator;

import java.util.ArrayList;
import java.util.List;

public class RESTSensorController {
    @FXML
    private TextField nameField;
    @FXML
    private ComboBox<String> baseUrlComboBox;
    @FXML
    private VBox subSensorContainer;

    private TextFormatter<String> createCapitalizeFormatter() {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            if (change.getControlNewText().isEmpty()) {
                return change;
            }

            if (change.getControlNewText().length() == 1) {
                change.setText(change.getText().toUpperCase());
            } else if (change.getControlNewText().length() > change.getControlText().length()) {
                // Only for new text being added
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
        baseUrlComboBox.getItems().addAll(
                "http://localhost:8080"  // Updated with trailing slash
        );
        baseUrlComboBox.setEditable(true);
        baseUrlComboBox.setPromptText("REST-URL auswählen oder eingeben");

        handleAddSubsensor();
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

        List<SubRESTSensor> subSensors = new ArrayList<>();
        for (var child : subSensorContainer.getChildren()) {
            if (child instanceof VBox subSensorBox) {
                TextField subPathField = (TextField) subSensorBox.getChildren().get(0);
                ComboBox<String> dataTypeComboBox = (ComboBox<String>) subSensorBox.getChildren().get(1);
                HBox rangeBox = (HBox) subSensorBox.getChildren().get(2);
                TextField minValueField = (TextField) rangeBox.getChildren().get(1);
                TextField maxValueField = (TextField) rangeBox.getChildren().get(2);

                if (subSensorBox.getChildren().get(3) instanceof CheckBox addTimestampCheckbox) {
                    double minValue = 0;
                    double maxValue = 0;

                    if (!dataTypeComboBox.getValue().equals("String") &&
                            !dataTypeComboBox.getValue().equals("Boolean")) {
                        minValue = Double.parseDouble(minValueField.getText());
                        maxValue = Double.parseDouble(maxValueField.getText());
                    }

                    SubRESTSensor subSensor = new SubRESTSensor(
                            subPathField.getText(),
                            dataTypeComboBox.getValue(),
                            minValue,
                            maxValue,
                            addTimestampCheckbox.isSelected()
                    );

                    subSensors.add(subSensor);
                }
            }
        }

        RESTSensor restsensor = new RESTSensor(
                nameField.getText(),
                baseUrlComboBox.getValue(),
                subSensors.getFirst()
        );

        for (int i = 1; i < subSensors.size(); i++) {
            restsensor.addSubSensor(subSensors.get(i));
        }

        Main.RESTSensorList.add(restsensor);

        showInfoDialog("REST-Sensor erfolgreich gespeichert. Anzahl der Sensoren: " + Main.RESTSensorList.size());
        closeDialog();
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private boolean validateInputs() {
        if (nameField.getText().trim().isEmpty()) {
            showErrorDialog("Fehler: Sensorname darf nicht leer sein.");
            return false;
        }

        if (baseUrlComboBox.getValue() == null || baseUrlComboBox.getValue().trim().isEmpty()) {
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
                if (subPathField.getText().trim().isEmpty()) {
                    showErrorDialog("Fehler: Sub-Path darf nicht leer sein.");
                    return false;
                }

                ComboBox<String> dataTypeComboBox = (ComboBox<String>) subSensorBox.getChildren().get(1);
                if (dataTypeComboBox.getValue() == null) {
                    showErrorDialog("Fehler: Datentyp muss ausgewählt werden.");
                    return false;
                }

                if (!dataTypeComboBox.getValue().equals("String") && !dataTypeComboBox.getValue().equals("Boolean")) {
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

    private void showErrorDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ungültige Eingabe");
        alert.setHeaderText("Bitte korrigieren Sie die folgenden Fehler:");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfoDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Erfolgreich");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeDialog() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
}