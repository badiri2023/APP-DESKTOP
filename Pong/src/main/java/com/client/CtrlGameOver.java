package com.client;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class CtrlGameOver implements Initializable {

    @FXML private Label gameOverLabel;
    @FXML private Label winnerLabel;
    @FXML private Label finalScore1;
    @FXML private Label finalScore2;
    @FXML private Button rematchButton;
    @FXML private Button returnButton;
    @FXML private VBox rematchStatus;
    @FXML private Label rematchStatusLabel;
    
    private Font retroFont;
    private boolean rematchProposed = false;
    private boolean rematchAccepted = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Cargar fuente
            retroFont = Font.loadFont(getClass().getResourceAsStream("/assets/fonts/8bitOperatorPlus8-Regular.ttf"), 14);
            if (retroFont == null) {
                retroFont = Font.font("Consolas", 14);
            }
            
            applyStyles();
            setupButtonActions();
            
        } catch (Exception e) {
            System.err.println("Error en CtrlGameOver: " + e.getMessage());
        }
    }
    
    private void applyStyles() {
        // Estilo del título GAME OVER
        if (gameOverLabel != null) {
            gameOverLabel.setFont(Font.font(retroFont.getFamily(), FontWeight.BOLD, 48));
            gameOverLabel.setTextFill(Color.RED);
            gameOverLabel.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(255,0,0,0.8), 10, 0, 0, 0);");
        }
        
        // Estilo del label del ganador
        if (winnerLabel != null) {
            winnerLabel.setFont(Font.font(retroFont.getFamily(), FontWeight.BOLD, 32));
            winnerLabel.setTextFill(Color.WHITE);
            winnerLabel.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(255,255,255,0.8), 5, 0, 0, 0);");
        }
        
        // Estilo de las puntuaciones
        if (finalScore1 != null) {
            finalScore1.setFont(Font.font(retroFont.getFamily(), FontWeight.BOLD, 24));
            finalScore1.setTextFill(Color.WHITE);
        }
        
        if (finalScore2 != null) {
            finalScore2.setFont(Font.font(retroFont.getFamily(), FontWeight.BOLD, 24));
            finalScore2.setTextFill(Color.WHITE);
        }
        
        // Estilo de botones
        applyButtonStyle(rematchButton);
        applyButtonStyle(returnButton);
    }
    
    private void applyButtonStyle(Button button) {
        if (button != null) {
            button.setFont(Font.font(retroFont.getFamily(), FontWeight.BOLD, 16));
            button.setStyle(
                "-fx-background-color: #000000; " +
                "-fx-text-fill: #ffffff; " +
                "-fx-border-color: #ffffff; " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 5; " +
                "-fx-background-radius: 5; " +
                "-fx-pref-width: 200; " +
                "-fx-pref-height: 50;"
            );
            
            // Efecto hover
            button.setOnMouseEntered(e -> {
                if (!button.isDisabled()) {
                    button.setStyle(
                        "-fx-background-color: #333333; " +
                        "-fx-text-fill: #ffffff; " +
                        "-fx-border-color: #ffcc00; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 5; " +
                        "-fx-background-radius: 5; " +
                        "-fx-pref-width: 200; " +
                        "-fx-pref-height: 50;"
                    );
                }
            });
            
            button.setOnMouseExited(e -> {
                if (!button.isDisabled()) {
                    applyButtonStyle(button);
                }
            });
        }
    }
    
    private void setupButtonActions() {
        if (rematchButton != null) {
            rematchButton.setOnAction(event -> handleRematch());
        }
        
        if (returnButton != null) {
            returnButton.setOnAction(event -> handleReturnToLobby());
        }
    }
    
    public void setWinner(String winnerName, int score1, int score2) {
        if (winnerLabel != null) {
            winnerLabel.setText("GANADOR: " + winnerName);
        }
        if (finalScore1 != null) {
            finalScore1.setText(String.valueOf(score1));
        }
        if (finalScore2 != null) {
            finalScore2.setText(String.valueOf(score2));
        }
    }
    
    @FXML
    private void handleRematch() {
        if (!rematchProposed) {
            // Proponer revancha
            rematchProposed = true;
            rematchButton.setText("ESPERANDO...");
            rematchButton.setDisable(true);
            
            if (rematchStatus != null) {
                rematchStatus.setVisible(true);
            }
            
            // Enviar solicitud de revancha al servidor
            sendRematchRequest();
        }
    }
    
    @FXML
    private void handleReturnToLobby() {
        // Volver al lobby de jugadores
        UtilsViews.setView("ViewOpponentSelection");
        
        // Limpiar estado de revancha
        rematchProposed = false;
        rematchAccepted = false;
        
        // Actualizar lista de jugadores
        if (Main.ctrlOpponentSelection != null) {
            Main.requestPlayersList();
        }
    }
    
    private void sendRematchRequest() {
        try {
            // Aquí enviarías la solicitud de revancha al servidor
            // JSONObject request = new JSONObject();
            // request.put("type", "rematchRequest");
            // Main.wsClient.safeSend(request.toString());
            
            System.out.println("Solicitud de revancha enviada");
            
        } catch (Exception e) {
            System.err.println("Error enviando solicitud de revancha: " + e.getMessage());
        }
    }
    
    public void handleRematchResponse(boolean accepted) {
        if (accepted) {
            rematchAccepted = true;
            // Iniciar nueva partida
            Main.pauseDuring(1000, () -> {
                UtilsViews.setViewAnimating("ViewLoading");
            });
        } else {
            // El oponente rechazó la revancha
            rematchButton.setDisable(true);
            rematchButton.setText("REVANCHA RECHAZADA");
        }
    }
    
    public void disableRematch() {
        if (rematchButton != null) {
            rematchButton.setDisable(true);
            rematchButton.setText("REVANCHA NO DISPONIBLE");
        }
    }
}