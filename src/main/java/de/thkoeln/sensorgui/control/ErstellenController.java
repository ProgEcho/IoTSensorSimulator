package de.thkoeln.sensorgui.control;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;


public class ErstellenController  {
    @FXML
    private TextField nameField;

    @FXML
    private void handleMQTT() {
        openSensorWindow("/MQTTSensor.fxml", "MQTT Sensor erstellen");
        closeDialog();

    }

    @FXML
    private void handleREST() {
        openSensorWindow("/RESTSensor.fxml", "REST Sensor erstellen");
        closeDialog();
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

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
}


