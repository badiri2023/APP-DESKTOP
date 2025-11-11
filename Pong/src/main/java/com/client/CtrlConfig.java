package com.client;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class CtrlConfig implements Initializable {

    @FXML
    public TextField txtPlayerName;
    
    @FXML
    public TextField txtServerUrl;

    @FXML
    public Label txtMessage;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Cargar configuración al iniciar
        loadSavedConfig();
    }

    private void loadSavedConfig() {
        ConfigManager.ConfigData config = ConfigManager.loadConfig();
        txtPlayerName.setText(config.playerName);
        txtServerUrl.setText(config.serverUrl);
    }

    @FXML
    private void connectToServer() {
        String playerName = txtPlayerName.getText().trim();
        String serverUrl = txtServerUrl.getText().trim();
        
        // Validar campos
        if (playerName.isEmpty()) {
            txtMessage.setText("Por favor, introduce tu nombre");
            return;
        }
        
        if (serverUrl.isEmpty()) {
            serverUrl = "matrixplay5.ieti.site";
            txtServerUrl.setText(serverUrl);
        }
        
        // Guardar configuración
        saveConfig();
        
        // Mostrar popup con la información
        showConnectionInfo(playerName, serverUrl);
        
        txtMessage.setText("Configuración guardada correctamente");
    }

    private void saveConfig() {
        ConfigManager.ConfigData config = new ConfigManager.ConfigData();
        config.playerName = txtPlayerName.getText().trim();
        config.serverUrl = txtServerUrl.getText().trim();
        
        ConfigManager.saveConfig(config);
    }

    private void showConnectionInfo(String playerName, String serverUrl) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información de Conexión");
        alert.setHeaderText("Configuración guardada y lista para conectar");
        alert.setContentText("Nombre del jugador: " + playerName + "\nServidor: " + serverUrl);
        alert.showAndWait();
    }

    @FXML
    private void setConfigLocal() {
        txtServerUrl.setText("localhost");
    }

    @FXML
    private void setConfigProxmox() {
        txtServerUrl.setText("matrixplay5.ieti.site");
    }
}