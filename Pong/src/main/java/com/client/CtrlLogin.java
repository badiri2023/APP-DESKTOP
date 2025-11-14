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
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class CtrlLogin implements Initializable {

    @FXML
    private AnchorPane anchorPane;

    // Elementos FXML
    @FXML private VBox configPanel;
    @FXML private Label titleLabel;
    @FXML private Label configLabel;
    @FXML private Label playerLabel;
    @FXML private Label serverLabel;
    @FXML private Label footerLabel;
    @FXML private TextField playerNameField;
    @FXML private TextField urlField;
    @FXML private Button connectButton;

    private Font retroFont;
    public static String playerName;
    public static String url;

    public static UtilsWS wsClient;
    
    private static final String CONFIG_PATH = "data/pong_config.json";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Cargar configuración guardada
            loadConfig();
            
            // Intentar cargar fuente retro
            try {
                retroFont = Font.loadFont(getClass().getResourceAsStream("/assets/fonts/8bitOperatorPlus8-Regular.ttf"), 14);
                System.out.println("✅ Fuente cargada: " + retroFont.getFamily());
            } catch (Exception e) {
                System.out.println("❌ No se pudo cargar fuente 8bitOperatorPlus8: " + e.getMessage());
                retroFont = Font.font("Consolas", 14);
            }
            
            // Aplicar todos los estilos desde Java
            applyAllStyles();
            setupConnectButton();

        } catch (Exception e) {
            System.out.println("Error en initialize: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Aplicar todos los estilos desde Java
    private void applyAllStyles() {
        // Fondo principal
        anchorPane.setStyle("-fx-background-color: #000000;");
        
        // Título PONG
        if (titleLabel != null) {
            titleLabel.setFont(Font.font(retroFont.getFamily(), FontWeight.BOLD, 48));
            titleLabel.setTextFill(Color.WHITE);
            titleLabel.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(255,255,255,0.8), 10, 0, 0, 0);");
        }
        
        // Panel de configuración
        if (configPanel != null) {
            configPanel.setStyle(
                "-fx-background-color: #1a1a1a; " +
                "-fx-background-radius: 8; " +
                "-fx-border-color: #ffffff; " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 8; " +
                "-fx-padding: 20;"
            );
        }
        
        // Label CONFIGURACIÓN
        if (configLabel != null) {
            configLabel.setFont(Font.font(retroFont.getFamily(), FontWeight.BOLD, 18));
            configLabel.setTextFill(Color.WHITE);
        }
        
        // Labels de campos (Jugador, Server)
        applyLabelStyle(playerLabel);
        applyLabelStyle(serverLabel);
        
        // Campos de texto
        applyTextFieldStyle(playerNameField);
        applyTextFieldStyle(urlField);
        
        // Footer
        if (footerLabel != null) {
            footerLabel.setFont(Font.font(retroFont.getFamily(), 12));
            footerLabel.setTextFill(Color.rgb(102, 102, 102)); // #666666
        }
    }
    
    private void applyLabelStyle(Label label) {
        if (label != null) {
            label.setFont(Font.font(retroFont.getFamily(), FontWeight.BOLD, 14));
            label.setTextFill(Color.WHITE);
        }
    }
    
    private void applyTextFieldStyle(TextField textField) {
        if (textField != null) {
            textField.setFont(Font.font(retroFont.getFamily(), 14));
            textField.setStyle(
                "-fx-background-color: #000000; " +
                "-fx-text-fill: #ffffff; " +
                "-fx-border-color: #ffffff; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 3; " +
                "-fx-background-radius: 3; " +
                "-fx-pref-height: 30; " +
                "-fx-pref-width: 250; " +
                "-fx-padding: 5 8;"
            );
            
            // Efecto focus
            textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    textField.setStyle(
                        "-fx-background-color: #000000; " +
                        "-fx-text-fill: #ffffff; " +
                        "-fx-border-color: #ffcc00; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 3; " +
                        "-fx-background-radius: 3; " +
                        "-fx-pref-height: 30; " +
                        "-fx-pref-width: 250; " +
                        "-fx-padding: 5 8; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(255,204,0,0.5), 5, 0, 0, 0);"
                    );
                } else {
                    applyTextFieldStyle(textField);
                }
            });
        }
    }

    private void setupConnectButton() {
        if (connectButton == null) return;

        // Estilo base del botón
        connectButton.setFont(Font.font(retroFont.getFamily(), FontWeight.BOLD, 14));
        applyConnectButtonNormalStyle();
        
        // Hover
        connectButton.setOnMouseEntered(e -> {
            if (!connectButton.isDisabled()) {
                applyConnectButtonHoverStyle();
            }
        });

        // Mouse exited
        connectButton.setOnMouseExited(e -> {
            if (!connectButton.isDisabled()) {
                applyConnectButtonNormalStyle();
            }
        });
    }
    
    private void applyConnectButtonNormalStyle() {
        connectButton.setStyle(
            "-fx-background-color: #ffffff; " +
            "-fx-text-fill: #000000; " +
            "-fx-background-radius: 3; " +
            "-fx-border-radius: 3; " +
            "-fx-border-color: #000000; " +
            "-fx-border-width: 1; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(255,255,255,0.3), 4, 0, 0, 2);" +
            "-fx-pref-width: 180; " +
            "-fx-pref-height: 35; " +
            "-fx-cursor: hand;"
        );
    }
    
    private void applyConnectButtonHoverStyle() {
        connectButton.setStyle(
            "-fx-background-color: #e6e6e6; " +
            "-fx-text-fill: #000000; " +
            "-fx-background-radius: 3; " +
            "-fx-border-radius: 3; " +
            "-fx-border-color: #000000; " +
            "-fx-border-width: 1; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(255,255,255,0.6), 6, 0, 0, 3);" +
            "-fx-pref-width: 180; " +
            "-fx-pref-height: 35; " +
            "-fx-cursor: hand;"
        );
    }
    
    // Resto de métodos permanecen igual...
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
    
    private void saveConfig() {
        try {
            Path dataDir = Paths.get("data");
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
                System.out.println("Carpeta 'data' creada");
            }
            
            JSONObject config = new JSONObject();
            config.put("playerName", playerNameField.getText().trim());
            config.put("serverUrl", urlField.getText().trim());
            config.put("lastSaved", System.currentTimeMillis());
            
            String jsonString = config.toString(2);
            Files.write(Paths.get(CONFIG_PATH), jsonString.getBytes());
            
            System.out.println("Configuración guardada en: " + Paths.get(CONFIG_PATH).toAbsolutePath());
            
        } catch (Exception e) {
            System.out.println("Error guardando configuración: " + e.getMessage());
        }
    }

    @FXML
    private void handleConnect() {
        playerName = playerNameField.getText().trim();
        url = urlField.getText().trim();

        if (playerName.isEmpty() || url.isEmpty()) {
            showAlert("Error", "Si us plau, completa tots els camps");
            return;
        }

        saveConfig();
        setConnectingState();

        new Thread(() -> {
            try {
                Thread.sleep(2000);
                
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
        connectButton.setFont(Font.font(retroFont.getFamily(), FontWeight.BOLD, 12));
        connectButton.setStyle(
            "-fx-background-color: #666666; " +
            "-fx-text-fill: #999999; " +
            "-fx-background-radius: 3; " +
            "-fx-border-radius: 3; " +
            "-fx-border-color: #444444; " +
            "-fx-border-width: 1;"
        );
    }

    private void setConnectedState() {
        connectButton.setText("CONNECTAT!");
        connectButton.setDisable(false);
        connectButton.setFont(Font.font(retroFont.getFamily(), FontWeight.BOLD, 14));
        connectButton.setStyle(
            "-fx-background-color: #00ff00; " +
            "-fx-text-fill: #000000; " +
            "-fx-background-radius: 3; " +
            "-fx-border-radius: 3; " +
            "-fx-border-color: #000000; " +
            "-fx-border-width: 1;"
        );
    }

    private void setErrorState() {
        connectButton.setText("Connectar");
        connectButton.setDisable(false);
        connectButton.setFont(Font.font(retroFont.getFamily(), FontWeight.BOLD, 14));
        applyConnectButtonNormalStyle(); // Volver al estilo normal
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        
        // Estilo del diálogo
        alert.getDialogPane().setStyle(
            "-fx-background-color: #000000; " +
            "-fx-border-color: #ffffff; " +
            "-fx-border-width: 3; " +
            "-fx-border-radius: 5; " +
            "-fx-background-radius: 5;"
        );
        
        Button okButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        
        if (okButton != null && retroFont != null) {
            okButton.setFont(Font.font(retroFont.getFamily(), FontWeight.BOLD, 12));
            okButton.setStyle(
                "-fx-background-color: #ffffff; " +
                "-fx-text-fill: #000000; " +
                "-fx-background-radius: 3; " +
                "-fx-border-radius: 3; " +
                "-fx-border-color: #000000; " +
                "-fx-border-width: 1;"
            );
        }
        
        alert.setContentText(message);
        alert.showAndWait();
    }

    public String getUrl() {return url;}
    public String getUserName() {return playerName;}
}