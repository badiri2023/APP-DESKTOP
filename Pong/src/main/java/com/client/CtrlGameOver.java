package com.client;

import java.net.URL;
import java.util.ResourceBundle;

import org.json.JSONObject;

import javafx.application.Platform;
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
        // Estilo del título GAME OVER - BLANCO
        if (gameOverLabel != null) {
            gameOverLabel.setFont(Font.font(retroFont.getFamily(), FontWeight.BOLD, 64));
            gameOverLabel.setTextFill(Color.WHITE);
            gameOverLabel.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(255,255,255,0.8), 10, 0, 0, 0);");
        }
        
        // Estilo del label del ganador - BLANCO
        if (winnerLabel != null) {
            winnerLabel.setFont(Font.font(retroFont.getFamily(), FontWeight.BOLD, 42));
            winnerLabel.setTextFill(Color.WHITE);
            winnerLabel.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(255,255,255,0.6), 5, 0, 0, 0);");
        }
        
        // Estilo de las puntuaciones - BLANCO
        if (finalScore1 != null) {
            finalScore1.setFont(Font.font(retroFont.getFamily(), FontWeight.BOLD, 24));
            finalScore1.setTextFill(Color.WHITE);
        }
        
        if (finalScore2 != null) {
            finalScore2.setFont(Font.font(retroFont.getFamily(), FontWeight.BOLD, 24));
            finalScore2.setTextFill(Color.WHITE);
        }
        
        // Estilo para labels de jugadores - BLANCO
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
        
        // Estilo del estado de revancha - GRIS CLARO
        if (rematchStatusLabel != null) {
            rematchStatusLabel.setFont(Font.font(retroFont.getFamily(), FontWeight.BOLD, 14));
            rematchStatusLabel.setTextFill(Color.rgb(204, 204, 204)); // #cccccc
        }
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
            
            // Efecto hover - GRIS OSCURO
            button.setOnMouseEntered(e -> {
                if (!button.isDisabled()) {
                    button.setStyle(
                        "-fx-background-color: #333333; " +
                        "-fx-text-fill: #ffffff; " +
                        "-fx-border-color: #ffffff; " +
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
    public void setWinner(String winnerName, int score1, int score2) {
        this.winnerName = winnerName;
        this.player1Score = score1;
        this.player2Score = score2;
        
        System.out.println("Configurando GameOver - Ganador: " + winnerName + " | Score: " + score1 + "-" + score2);
        
        // Obtener información del juego anterior
        String currentPlayer = Main.ctrlLogin.getUserName();
        System.out.println("Jugador actual: " + currentPlayer);
        
        // Determinar si el jugador actual es el ganador
        this.isCurrentPlayerWinner = winnerName.equals(currentPlayer);
        System.out.println("¿Es ganador el jugador actual? " + isCurrentPlayerWinner);
        
        // Obtener los nombres de los jugadores desde CtrlGame
        CtrlGame ctrlGame = (CtrlGame) UtilsViews.getController("ViewGame");
        if (ctrlGame != null) {
            try {
                String opponent = getOpponentNameFromGame(ctrlGame);
                System.out.println("Oponente: " + opponent);
                
                if (opponent != null) {
                    // Determinar qué jugador es P1 y qué jugador es P2
                    if (ctrlGameIsPlayer1(ctrlGame)) {
                        this.player1Name = currentPlayer;
                        this.player2Name = opponent;
                        System.out.println("Asignación: P1=" + currentPlayer + ", P2=" + opponent);
                    } else {
                        this.player1Name = opponent;
                        this.player2Name = currentPlayer;
                        System.out.println("Asignación: P1=" + opponent + ", P2=" + currentPlayer);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error obteniendo nombres de jugadores: " + e.getMessage());
                // Valores por defecto si hay error
                this.player1Name = "Jugador 1";
                this.player2Name = "Jugador 2";
            }
        } else {
            System.err.println("CtrlGame es null - no se puede obtener información del juego");
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
    
    // MODIFICADO: Actualizar toda la interfaz de usuario (sin colores rojo/verde)
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
        
        // Configurar winnerLabel
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
        
        // NOTA: Ya no aplicamos colores diferentes a ganador/perdedor
        // Ambos jugadores se muestran en BLANCO para mantener consistencia
        
        System.out.println("UI de GameOver actualizada correctamente");
    }
    
    // NUEVO: Configurar winnerLabel para mostrar información completa
    private void configureWinnerLabel() {
        if (winnerLabel != null) {
            // winnerLabel ahora muestra solo el nombre del ganador
            winnerLabel.setText("GANADOR: " + winnerName);
            System.out.println("Configurado ganador: " + winnerName);
        }
    }
    
    @FXML
    private void handleRematch() {
        if (!rematchProposed) {
            rematchProposed = true;
            rematchButton.setText("ESPERANDO...");
            rematchButton.setDisable(true);
            
            if (rematchStatus != null) {
                rematchStatus.setVisible(true);
                rematchStatusLabel.setText("Solicitando revancha...");
            }
            
            sendRematchRequest();
        }
    }

    @FXML
    private void handleReturnToLobby() {
        // Limpiar estado de revancha
        rematchProposed = false;
        rematchAccepted = false;
        
        // Volver al lobby
        UtilsViews.setViewAnimating("ViewOpponentSelection");
        
        // Actualizar lista de jugadores
        if (Main.ctrlOpponentSelection != null) {
            Main.requestPlayersList();
        }
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