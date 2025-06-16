package de.thkoeln.sensorgui.control;
import de.thkoeln.sensordaten.MQTTSensor;
import de.thkoeln.sensordaten.RESTSensor;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.List;
import static de.thkoeln.sensorgui.control.Main.MQTTSensorList;
import static de.thkoeln.sensorgui.control.Main.RESTSensorList;

public class SensorListController {

    @FXML
    private ListView<String> sensorListView;

    @FXML
    private Button deleteButton;

    @FXML
    private Button editButton;

    @FXML
    private Button closeButton;

    private final ObservableList<String> sensorList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        loadSensors();
        setupListViewSelection();
        setupContextMenu();
        setupButtonHoverEffects(deleteButton, "#ff6b6b", "#ff4040");
        setupButtonHoverEffects(editButton, "#4dabf7", "#339af0");
        setupButtonHoverEffects(closeButton, "#d1d1d1", "#a6a6a6");
    }

    private void setupButtonHoverEffects(Button button, String defaultColor, String hoverColor) {
        button.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: white; -fx-background-radius: 20px; -fx-font-weight: bold;", defaultColor));
        button.setOnMouseEntered(e -> button.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: white; -fx-background-radius: 20px; -fx-font-weight: bold;", hoverColor)));
        button.setOnMouseExited(e -> button.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: white; -fx-background-radius: 20px; -fx-font-weight: bold;", defaultColor)));
    }

    private void setupListViewSelection() {
        // Enable multiple selection
        sensorListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        sensorListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            boolean hasSelection = !sensorListView.getSelectionModel().getSelectedItems().isEmpty();
            deleteButton.setDisable(!hasSelection);
            // Edit button should only be enabled when exactly one item is selected
            editButton.setDisable(sensorListView.getSelectionModel().getSelectedItems().size() != 1);
        });
    }

    private void setupContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem editItem = new MenuItem("Bearbeiten");
        MenuItem deleteItem = new MenuItem("Löschen");

        editItem.setOnAction(event -> handleEdit());
        deleteItem.setOnAction(event -> handleDelete());

        contextMenu.getItems().addAll(editItem, deleteItem);
        sensorListView.setContextMenu(contextMenu);
    }

    private void loadSensors() {
        sensorList.clear();
        for (MQTTSensor sensor : MQTTSensorList) {
            sensorList.add("MQTT: " + sensor.getSensorName());
        }
        for (RESTSensor sensor : RESTSensorList) {
            sensorList.add("REST: " + sensor.getSensorName());
        }
        sensorListView.setItems(sensorList);

        deleteButton.setDisable(sensorList.isEmpty());
        editButton.setDisable(sensorList.isEmpty());
    }

    @FXML
    private void handleDelete() {
        List<String> selectedItems = sensorListView.getSelectionModel().getSelectedItems();
        if (!selectedItems.isEmpty()) {
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Sensoren löschen");
            confirmDialog.setHeaderText(String.format("%d Sensor(en) wirklich löschen?", selectedItems.size()));
            confirmDialog.setContentText("Diese Aktion kann nicht rückgängig gemacht werden.");

            confirmDialog.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    // Convert to array to avoid ConcurrentModificationException
                    Object[] selectedItemsArray = selectedItems.toArray();
                    for (Object item : selectedItemsArray) {
                        String sensorItem = (String) item;
                        int index = sensorList.indexOf(sensorItem);

                        if (sensorItem.startsWith("MQTT")) {
                            MQTTSensorList.remove(index);
                        } else {
                            RESTSensorList.remove(index - MQTTSensorList.size());
                        }
                        sensorList.remove(sensorItem);
                    }
                }
            });
        }
    }

    @FXML
    private void handleEdit() {
        int selectedIndex = sensorListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            try {
                FXMLLoader loader;
                Stage stage = new Stage();

                if (sensorList.get(selectedIndex).startsWith("MQTT")) {
                    MQTTSensor sensor = MQTTSensorList.get(selectedIndex);
                    loader = new FXMLLoader(getClass().getResource("/MQTTSensorEdit.fxml"));
                    stage.setTitle("MQTT Sensor bearbeiten");

                    stage.setScene(new Scene(loader.load()));
                    MQTTSensorEditController controller = loader.getController();
                    controller.initializeSensor(sensor, selectedIndex);

                    stage.setOnHidden(event -> loadSensors());
                }
                else if (sensorList.get(selectedIndex).startsWith("REST")) {
                    RESTSensor sensor = RESTSensorList.get(selectedIndex - MQTTSensorList.size());
                    loader = new FXMLLoader(getClass().getResource("/RESTSrnsorEdit.fxml"));
                    stage.setTitle("REST Sensor bearbeiten");

                    stage.setScene(new Scene(loader.load()));
                    RESTSensorEditController controller = loader.getController();
                    controller.initializeSensor(sensor, selectedIndex - MQTTSensorList.size());

                    stage.setOnHidden(event -> loadSensors());
                }

                stage.initModality(Modality.APPLICATION_MODAL);
                stage.show();
            } catch (IOException e) {
                showErrorDialog(e.getMessage());
            }
        }
    }

    @FXML
    private void handleClose() {
        ((Stage) sensorListView.getScene().getWindow()).close();
    }

    private void showErrorDialog(String content) {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setTitle("Fehler");
        errorAlert.setHeaderText("Fehler beim Öffnen");
        errorAlert.setContentText(content);
        errorAlert.showAndWait();
    }
}