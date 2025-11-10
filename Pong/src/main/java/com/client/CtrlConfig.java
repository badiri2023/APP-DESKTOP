package com.client;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class CtrlConfig implements Initializable {

    @FXML
    public TextField txtPlayerName;  // NUEVO: Campo para nombre del jugador
    
    @FXML
    public TextField txtProtocol;

    @FXML
    public TextField txtHost;

    @FXML
    public TextField txtPort;

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
        txtProtocol.setText(config.protocol);
        txtHost.setText(config.host);
        txtPort.setText(config.port);
    }

    @FXML
    private void connectToServer() {
        // Guardar configuración antes de conectar
        saveConfig();
        Main.connectToServer();
    }

    private void saveConfig() {
        ConfigManager.ConfigData config = new ConfigManager.ConfigData();
        config.playerName = txtPlayerName.getText().trim();
        config.protocol = txtProtocol.getText().trim();
        config.host = txtHost.getText().trim();
        config.port = txtPort.getText().trim();
        
        ConfigManager.saveConfig(config);
    }

    @FXML
    private void setConfigLocal() {
        txtProtocol.setText("ws");
        txtHost.setText("localhost");
        txtPort.setText("3000");
    }

    @FXML
    private void setConfigProxmox() {
        txtProtocol.setText("wss");
        txtHost.setText("matrixplay5.ieti.site");
        txtPort.setText("443");
    }
}