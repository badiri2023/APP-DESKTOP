package com.client;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.ResourceBundle;

import org.json.JSONObject;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;

public class CtrlLogin implements Initializable {

    @FXML
    private AnchorPane anchorPane;

    // interactivos
    @FXML private TextField playerNameField;
    @FXML private TextField urlField;
    @FXML private Button connectButton;

    private Font retroFont;
    public static String playerName;
    public static String url;

    public static UtilsWS wsClient;
    
    // Configuración - EN CARPETA DATA
    private static final String CONFIG_PATH = "data/pong_config.json";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Cargar configuración guardada
            loadConfig();
            
            // Intentar cargar fuente retro, si no usa Consolas
            try {
                retroFont = Font.loadFont(getClass().getResourceAsStream("/assets/fonts/8bitOperatorPlus8-Regular.ttf"), 14);
                if (retroFont == null) {
                    // Si no encuentra .ttf, intenta con otros formatos
                    retroFont = Font.loadFont(getClass().getResourceAsStream("/assets/fonts/8bitOperatorPlus8-Regular.otf"), 14);
                }
            } catch (Exception e) {
                System.out.println("No se pudo cargar fuente 8bitOperatorPlus8: " + e.getMessage());
            }
            
            if (retroFont == null) {
                System.out.println("Usando Consolas como fuente por defecto");
                retroFont = Font.font("Consolas", 14);
            }
            
            setupConnectButton();

        } catch (Exception e) {
            System.out.println("Error en initialize: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Cargar configuración desde JSON
    private void loadConfig() {
        try {
            if (!Files.exists(Paths.get(CONFIG_PATH))) {
                System.out.println("No se encontró archivo de configuración en data/, usando valores por defecto");
                return;
            }
            
            String content = new String(Files.readAllBytes(Paths.get(CONFIG_PATH)));
            JSONObject json = new JSONObject(content);
            
            String savedName = json.optString("playerName", "");
            String savedUrl = json.optString("serverUrl", "");
            
            // Rellenar campos con valores guardados
            if (!savedName.isEmpty()) {
                playerNameField.setText(savedName);
            }
            if (!savedUrl.isEmpty()) {
                urlField.setText(savedUrl);
            }
            
            System.out.println("Configuración cargada desde: " + Paths.get(CONFIG_PATH).toAbsolutePath());
            
        } catch (Exception e) {
            System.out.println("Error cargando configuración: " + e.getMessage());
        }
    }
    
    // Guardar configuración en JSON
    private void saveConfig() {
        try {
            // Crear la carpeta data si no existe
            Path dataDir = Paths.get("data");
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
                System.out.println("Carpeta 'data' creada");
            }
            
            JSONObject config = new JSONObject();
            config.put("playerName", playerNameField.getText().trim());
            config.put("serverUrl", urlField.getText().trim());
            config.put("lastSaved", System.currentTimeMillis());
            
            String jsonString = config.toString(2); // Pretty print
            Files.write(Paths.get(CONFIG_PATH), jsonString.getBytes());
            
            System.out.println("Configuración guardada en: " + Paths.get(CONFIG_PATH).toAbsolutePath());
            
        } catch (Exception e) {
            System.out.println("Error guardando configuración: " + e.getMessage());
        }
    }

    private void setupConnectButton() {
        if (connectButton == null) 
            return;

        // boton sin presionar
        connectButton.setStyle(
            "-fx-background-color: #ffffff; " +
            "-fx-text-fill: #000000; " +
            "-fx-font-family: 'Consolas'; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 3; " +
            "-fx-border-radius: 3; " +
            "-fx-border-color: #000000; " +
            "-fx-border-width: 1; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(255,255,255,0.3), 4, 0, 0, 2);"
        );
        
        connectButton.setPrefWidth(180);
        connectButton.setPrefHeight(35);

        // hover
        connectButton.setOnMouseEntered(e -> {
            if (!connectButton.isDisabled()) {
                connectButton.setStyle(
                    "-fx-background-color: #e6e6e6; " +
                    "-fx-text-fill: #000000; " +
                    "-fx-font-family: 'Consolas'; " +
                    "-fx-font-size: 14px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-background-radius: 3; " +
                    "-fx-border-radius: 3; " +
                    "-fx-border-color: #000000; " +
                    "-fx-border-width: 1; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(255,255,255,0.6), 6, 0, 0, 3);"
                );
            }
        });

        connectButton.setOnMouseExited(e -> {
            if (!connectButton.isDisabled()) {
                connectButton.setStyle(
                    "-fx-background-color: #ffffff;" +
                    "-fx-text-fill: #000000; " +
                    "-fx-font-family: 'Consolas'; " +
                    "-fx-font-size: 14px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-background-radius: 3; " +
                    "-fx-border-radius: 3; " +
                    "-fx-border-color: #000000; " +
                    "-fx-border-width: 1; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(255,255,255,0.3), 4, 0, 0, 2);"
                );
            }
        });
    }

    @FXML
    private void handleConnect() {
        playerName = playerNameField.getText().trim();
        url = urlField.getText().trim();

        if (playerName.isEmpty() || url.isEmpty()) {
            showAlert("Error", "Si us plau, completa tots els camps");
            return;
        }

        // Guardar configuración antes de conectar
        saveConfig();

        setConnectingState();

        // conexion simulador
        new Thread(() -> {
            try {
                
                Thread.sleep(2000); // tiempo conex
                
                Platform.runLater(() -> {
                    setConnectedState();
                    Main.connectToServer();
                    showAlert("Connexió Exitosa", "Connectat com: " + playerName + "\n" + "Servidor: " + url);
                    UtilsViews.setViewAnimating("ViewWait");
                });
                
            } catch (InterruptedException ex) {
                Platform.runLater(() -> {
                    setErrorState();
                    showAlert("Error de Connexió", "No s'ha pogut connectar al servidor");
                });
            }
        }).start();
    }

    private void setConnectingState() {
        connectButton.setText("CONNECTANT...");
        connectButton.setDisable(true);
        connectButton.setStyle(
            "-fx-background-color: #666666; " +
            "-fx-text-fill: #999999; " +
            "-fx-font-family: 'Consolas'; " +
            "-fx-font-size: 12px; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 3; " +
            "-fx-border-radius: 3; " +
            "-fx-border-color: #444444; " +
            "-fx-border-width: 1;"
        );
    }

    private void setConnectedState() {
        connectButton.setText("CONNECTAT!");
        connectButton.setDisable(false);
        connectButton.setStyle(
            "-fx-background-color: #00ff00; " +
            "-fx-text-fill: #000000; " +
            "-fx-font-family: 'Consolas'; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 3; " +
            "-fx-border-radius: 3; " +
            "-fx-border-color: #000000; " +
            "-fx-border-width: 1;"
        );
    }

    private void setErrorState() {
        connectButton.setText("Connectar");
        connectButton.setDisable(false);
        connectButton.setStyle(
            "-fx-background-color: #ff0000; " +
            "-fx-text-fill: #ffffff; " +
            "-fx-font-family: 'Consolas'; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 3; " +
            "-fx-border-radius: 3; " +
            "-fx-border-color: #000000; " +
            "-fx-border-width: 1;"
        );
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        
        // Estilo personalizado para el diálogo
        alert.getDialogPane().setStyle(
            "-fx-background-color: #000000; " +
            "-fx-border-color: #ffffff; " +
            "-fx-border-width: 3; " +
            "-fx-border-radius: 5; " +
            "-fx-background-radius: 5;"
        );
        
        // Estilo para el contenido
        alert.getDialogPane().lookup(".content.label").setStyle(
            "-fx-text-fill: #ffffff; " +
            "-fx-font-family: 'Consolas'; " +
            "-fx-font-size: 14px; " +
            "-fx-background-color: transparent;"
        );
        
        // Estilo para los botones
        alert.getDialogPane().lookupButton(ButtonType.OK).setStyle(
            "-fx-background-color: #ffffff; " +
            "-fx-text-fill: #000000; " +
            "-fx-font-family: 'Consolas'; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 3; " +
            "-fx-border-radius: 3; " +
            "-fx-border-color: #000000; " +
            "-fx-border-width: 1;"
        );
        
        // Efecto hover para el botón
        alert.getDialogPane().lookupButton(ButtonType.OK).setOnMouseEntered(e -> {
            alert.getDialogPane().lookupButton(ButtonType.OK).setStyle(
                "-fx-background-color: #e6e6e6; " +
                "-fx-text-fill: #000000; " +
                "-fx-font-family: 'Consolas'; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 3; " +
                "-fx-border-radius: 3; " +
                "-fx-border-color: #000000; " +
                "-fx-border-width: 1;"
            );
        });
        
        alert.getDialogPane().lookupButton(ButtonType.OK).setOnMouseExited(e -> {
            alert.getDialogPane().lookupButton(ButtonType.OK).setStyle(
                "-fx-background-color: #ffffff; " +
                "-fx-text-fill: #000000; " +
                "-fx-font-family: 'Consolas'; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 3; " +
                "-fx-border-radius: 3; " +
                "-fx-border-color: #000000; " +
                "-fx-border-width: 1;"
            );
        });
        
        alert.setContentText(message);
        alert.showAndWait();
    }

    // getters
    public String getUrl(){
        return url;
    }

    public String getUserName(){
        return playerName;
    }
}