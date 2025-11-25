package com.client;

import java.net.URL;
import java.util.ResourceBundle;

import org.json.JSONObject;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
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

    @FXML private Label player1Label;
    @FXML private Label player2Label;
    
    private Font retroFont;
    private boolean rematchProposed = false;
    private boolean rematchAccepted = false;

    // Variables para almacenar información de los jugadores
    private String player1Name = "";
    private String player2Name = "";
    private String winnerName = "";
    private int player1Score = 0;
    private int player2Score = 0;
    private boolean isCurrentPlayerWinner = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Cargar fuente
            retroFont = Font.loadFont(getClass().getResourceAsStream("/assets/fonts/BrunoAce-Regular.ttf"), 14);
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
        // Estilo del título GAME OVER (color se ajustará dinámicamente)
        if (gameOverLabel != null) {
            gameOverLabel.setFont(Font.font(retroFont.getFamily(), FontWeight.BOLD, 64));
            gameOverLabel.setTextFill(Color.RED); // Color inicial rojo
            gameOverLabel.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(255,0,0,0.8), 10, 0, 0, 0);");
        }
        
        // Estilo del label del ganador (ahora más grande para compensar la falta de resultLabel)
        if (winnerLabel != null) {
            winnerLabel.setFont(Font.font(retroFont.getFamily(), FontWeight.BOLD, 42)); // Aumentado de 36 a 42
            winnerLabel.setTextFill(Color.GOLD);
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
        
        // Estilo inicial para labels de jugadores
        if (player1Label != null) {
            player1Label.setFont(Font.font(retroFont.getFamily(), FontWeight.BOLD, 14));
            player1Label.setTextFill(Color.WHITE);
        }
        
        if (player2Label != null) {
            player2Label.setFont(Font.font(retroFont.getFamily(), FontWeight.BOLD, 14));
            player2Label.setTextFill(Color.WHITE);
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
    
    // Método para establecer el ganador
    public void setWinner(String winner, int score1, int score2) {
        this.winnerName = winner;
        this.player1Score = score1;
        this.player2Score = score2;
        
        System.out.println("Configurando GameOver - Ganador: " + winner + " | Score: " + score1 + "-" + score2);
        
        // Obtener información del oponente desde CtrlGame
        CtrlGame ctrlGame = (CtrlGame) UtilsViews.getController("ViewGame");
        if (ctrlGame != null) {
            try {
                java.lang.reflect.Field opponentField = CtrlGame.class.getDeclaredField("opponentName");
                opponentField.setAccessible(true);
                this.opponentName = (String) opponentField.get(ctrlGame);
                System.out.println("Oponente para revancha: " + this.opponentName);
            } catch (Exception e) {
                System.err.println("Error obteniendo opponentName: " + e.getMessage());
                this.opponentName = "Oponente";
            }
        }
        
        updateUI();
    }
    
    // Método para obtener el nombre del oponente desde CtrlGame
    private String getOpponentNameFromGame(CtrlGame ctrlGame) {
        try {
            java.lang.reflect.Field opponentField = CtrlGame.class.getDeclaredField("opponentName");
            opponentField.setAccessible(true);
            return (String) opponentField.get(ctrlGame);
        } catch (Exception e) {
            System.err.println("Error obteniendo opponentName: " + e.getMessage());
            return "Oponente";
        }
    }
    
    // Método para determinar si el jugador actual era P1
    private boolean ctrlGameIsPlayer1(CtrlGame ctrlGame) {
        try {
            java.lang.reflect.Field roleField = CtrlGame.class.getDeclaredField("playerRole");
            roleField.setAccessible(true);
            String role = (String) roleField.get(ctrlGame);
            System.out.println("🎭 Rol del jugador: " + role);
            return "p1".equals(role);
        } catch (Exception e) {
            System.err.println("Error obteniendo playerRole: " + e.getMessage());
            return false;
        }
    }
    
    // MODIFICADO: Actualizar toda la interfaz de usuario (sin resultLabel)
    private void updateUI() {
        System.out.println(" Actualizando UI de GameOver...");
        
        // Actualizar labels de jugadores
        if (player1Label != null) {
            player1Label.setText(player1Name);
            System.out.println("player1Label: " + player1Name);
        }
        if (player2Label != null) {
            player2Label.setText(player2Name);
            System.out.println("player2Label: " + player2Name);
        }
        
        // CORREGIDO: Configurar winnerLabel sin resultLabel
        configureWinnerLabel();
        
        // Actualizar puntuaciones
        if (finalScore1 != null) {
            finalScore1.setText(String.valueOf(player1Score));
            System.out.println("finalScore1: " + player1Score);
        }
        if (finalScore2 != null) {
            finalScore2.setText(String.valueOf(player2Score));
            System.out.println("finalScore2: " + player2Score);
        }
        
        // Aplicar colores a AMBAS playerLabels
        applyWinnerColors();
        
        System.out.println("UI de GameOver actualizada correctamente");
    }
    
    // NUEVO: Configurar winnerLabel para mostrar información completa
    private void configureWinnerLabel() {
        if (winnerLabel != null) {
            // winnerLabel ahora muestra: "WINNER: [Nombre]" o "LOSER: [TuNombre]"
            if (isCurrentPlayerWinner) {
                winnerLabel.setText("WINNER: " + Main.ctrlLogin.getUserName());
                System.out.println("Configurado como GANADOR: " + Main.ctrlLogin.getUserName());
            } else {
                winnerLabel.setText("LOSER: " + Main.ctrlLogin.getUserName());
                System.out.println("Configurado como PERDEDOR: " + Main.ctrlLogin.getUserName());
            }
        }
    }
    
    // MODIFICADO: Aplicar colores verde/rojo a AMBAS playerLabels
    private void applyWinnerColors() {
        if (player1Label != null && player2Label != null && gameOverLabel != null && winnerLabel != null) {
            
            // Resetear colores primero
            player1Label.setStyle("-fx-text-fill: #ffffff;");
            player2Label.setStyle("-fx-text-fill: #ffffff;");
            
            // Determinar qué jugador es el ganador
            boolean player1IsWinner = player1Name.equals(winnerName);
            boolean player2IsWinner = player2Name.equals(winnerName);
            
            System.out.println("Aplicando colores - P1 es ganador: " + player1IsWinner + ", P2 es ganador: " + player2IsWinner);
            
            // // Aplicar colores a AMBAS playerLabels
            // if (player1IsWinner) {
            //     // Player 1 GANADOR (VERDE), Player 2 PERDEDOR (ROJO)
            //     player1Label.setStyle("-fx-text-fill: #00ff00; -fx-effect: dropshadow(three-pass-box, rgba(0,255,0,0.8), 3, 0, 0, 0);");
            //     player2Label.setStyle("-fx-text-fill: #ff0000; -fx-effect: dropshadow(three-pass-box, rgba(255,0,0,0.8), 3, 0, 0, 0);");
            //     System.out.println("Player1 VERDE (ganador), Player2 ROJO (perdedor)");
            // } else if (player2IsWinner) {
            //     // Player 2 GANADOR (VERDE), Player 1 PERDEDOR (ROJO)
            //     player1Label.setStyle("-fx-text-fill: #ff0000; -fx-effect: dropshadow(three-pass-box, rgba(255,0,0,0.8), 3, 0, 0, 0);");
            //     player2Label.setStyle("-fx-text-fill: #00ff00; -fx-effect: dropshadow(three-pass-box, rgba(0,255,0,0.8), 3, 0, 0, 0);");
            //     System.out.println("Player2 VERDE (ganador), Player1 ROJO (perdedor)");
            // }
            
            // GameOverLabel y winnerLabel según el jugador actual
            if (isCurrentPlayerWinner) {
                // JUGADOR ACTUAL GANÓ - VERDE
                gameOverLabel.setStyle("-fx-text-fill: #00ff00; -fx-effect: dropshadow(three-pass-box, rgba(0,255,0,0.8), 10, 0, 0, 0);");
                winnerLabel.setStyle("-fx-text-fill: #00ff00; -fx-effect: dropshadow(three-pass-box, rgba(0,255,0,0.8), 5, 0, 0, 0);");
                System.out.println("Color VERDE aplicado a GameOver - JUGADOR GANADOR");
            } else {
                // JUGADOR ACTUAL PERDIÓ - ROJO
                gameOverLabel.setStyle("-fx-text-fill: #ff0000; -fx-effect: dropshadow(three-pass-box, rgba(255,0,0,0.8), 10, 0, 0, 0);");
                winnerLabel.setStyle("-fx-text-fill: #ff0000; -fx-effect: dropshadow(three-pass-box, rgba(255,0,0,0.8), 5, 0, 0, 0);");
                System.out.println(" Color ROJO aplicado a GameOver - JUGADOR PERDEDOR");
            }
        }
    }
    
    @FXML
    private void handleRematch() {
        // Simplemente volver al lobby para invitar manualmente al mismo oponente
        UtilsViews.setViewAnimating("ViewOpponentSelection");
        
        // Actualizar lista de jugadores
        if (Main.ctrlOpponentSelection != null) {
            Main.requestPlayersList();
            
            // Opcional: Mostrar mensaje informativo
            Platform.runLater(() -> {
                AlertManager.showAlert("Revancha", 
                    "Volviendo al lobby. Puedes invitar a " + opponentName + " para una revancha.", 
                    AlertType.INFORMATION, 3000);
            });
        }
    }

    @FXML
    private void handleReturnToLobby() {
        UtilsViews.setViewAnimating("ViewOpponentSelection");
        
        if (Main.ctrlOpponentSelection != null) {
            Main.requestPlayersList();
        }
    }

    private void sendRematchInvitation() {
        try {
            // Usar el sistema normal de invitaciones para la revancha
            JSONObject invitation = new JSONObject();
            invitation.put("type", "challenge");
            invitation.put("to", opponentName);
            invitation.put("from", Main.ctrlLogin.getUserName());
            
            Main.wsClient.safeSend(invitation.toString());
            System.out.println("Invitación de revancha enviada a: " + opponentName);
            
            // Iniciar timeout para la invitación
            startInvitationTimeout();
            
        } catch (Exception e) {
            System.err.println("Error enviando invitación de revancha: " + e.getMessage());
            // Restablecer botón en caso de error
            rematchButton.setText("Revancha");
            rematchButton.setDisable(false);
            if (rematchStatus != null) {
                rematchStatusLabel.setText("Error al enviar invitación");
            }
        }
    }

    private void startInvitationTimeout() {
        new Thread(() -> {
            try {
                Thread.sleep(30000); // 30 segundos timeout
                
                Platform.runLater(() -> {
                    if (rematchProposed && !rematchAccepted) {
                        rematchProposed = false;
                        rematchButton.setText("Revancha");
                        rematchButton.setDisable(false);
                        if (rematchStatus != null) {
                            rematchStatusLabel.setText("La invitación ha expirado");
                        }
                        
                        // Mostrar alerta
                        AlertManager.showAlert("Tiempo Agotado", 
                            "La invitación de revancha a " + opponentName + " ha expirado.", 
                            AlertType.WARNING);
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    // Método para manejar cuando la invitación es aceptada (desde Main)
    public void handleInvitationAccepted() {
        Platform.runLater(() -> {
            rematchAccepted = true;
            rematchStatusLabel.setText("¡Revancha aceptada! Iniciando...");
            
            // La transición a ViewLoading se hará desde Main cuando llegue game_start
            System.out.println("Revancha aceptada - esperando inicio de partida...");
        });
    }

    // Método para manejar cuando la invitación es rechazada (desde Main)
    public void handleInvitationDeclined() {
        Platform.runLater(() -> {
            rematchProposed = false;
            rematchButton.setText("Revancha");
            rematchButton.setDisable(false);
            rematchStatusLabel.setText("Revancha rechazada");
            
            AlertManager.showAlert("Revancha Rechazada", 
                opponentName + " ha rechazado tu invitación de revancha.", 
                AlertType.INFORMATION);
        });
    }

    private void sendRematchRequest() {
        try {
            JSONObject rematchMsg = new JSONObject();
            rematchMsg.put("type", "rematch");
            rematchMsg.put("opponent", opponentName); // opponentName debería estar disponible
            
            Main.wsClient.safeSend(rematchMsg.toString());
            System.out.println("Solicitud de revancha enviada a: " + opponentName);
            
        } catch (Exception e) {
            System.err.println("Error enviando solicitud de revancha: " + e.getMessage());
            // Restablecer botón en caso de error
            rematchButton.setText("Revancha");
            rematchButton.setDisable(false);
        }
    }

    public void handleRematchResponse(boolean accepted) {
        Platform.runLater(() -> {
            if (accepted) {
                rematchAccepted = true;
                rematchStatusLabel.setText("¡Revancha aceptada! Iniciando...");
                
                Main.pauseDuring(2000, () -> {
                    UtilsViews.setViewAnimating("ViewLoading");
                    // El servidor se encargará de iniciar la nueva partida
                });
            } else {
                rematchButton.setDisable(true);
                rematchButton.setText("REVANCHA RECHAZADA");
                rematchStatusLabel.setText("El oponente rechazó la revancha");
            }
        });
    }

    // Añade estos campos a la clase
    private String opponentName = "";

    // Método para establecer el oponente
    public void setOpponent(String opponent) {
        this.opponentName = opponent;
    }
    
    public void disableRematch() {
        if (rematchButton != null) {
            rematchButton.setDisable(true);
            rematchButton.setText("REVANCHA NO DISPONIBLE");
        }
    }
}