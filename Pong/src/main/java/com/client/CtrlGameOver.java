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
            // winnerLabel ahora muestra: "GANADOR: [Nombre]" o "PERDEDOR: [TuNombre]"
            if (isCurrentPlayerWinner) {
                winnerLabel.setText("GANADOR: " + Main.ctrlLogin.getUserName());
                System.out.println("Configurado como GANADOR: " + Main.ctrlLogin.getUserName());
            } else {
                winnerLabel.setText("PERDEDOR: " + Main.ctrlLogin.getUserName());
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
            
            // Aplicar colores a AMBAS playerLabels
            if (player1IsWinner) {
                // Player 1 GANADOR (VERDE), Player 2 PERDEDOR (ROJO)
                player1Label.setStyle("-fx-text-fill: #00ff00; -fx-effect: dropshadow(three-pass-box, rgba(0,255,0,0.8), 3, 0, 0, 0);");
                player2Label.setStyle("-fx-text-fill: #ff0000; -fx-effect: dropshadow(three-pass-box, rgba(255,0,0,0.8), 3, 0, 0, 0);");
                System.out.println("Player1 VERDE (ganador), Player2 ROJO (perdedor)");
            } else if (player2IsWinner) {
                // Player 2 GANADOR (VERDE), Player 1 PERDEDOR (ROJO)
                player1Label.setStyle("-fx-text-fill: #ff0000; -fx-effect: dropshadow(three-pass-box, rgba(255,0,0,0.8), 3, 0, 0, 0);");
                player2Label.setStyle("-fx-text-fill: #00ff00; -fx-effect: dropshadow(three-pass-box, rgba(0,255,0,0.8), 3, 0, 0, 0);");
                System.out.println("Player2 VERDE (ganador), Player1 ROJO (perdedor)");
            }
            
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
    
    // Resto de métodos permanecen igual...
    @FXML
    private void handleRematch() {
        if (!rematchProposed) {
            rematchProposed = true;
            rematchButton.setText("ESPERANDO...");
            rematchButton.setDisable(true);
            
            if (rematchStatus != null) {
                rematchStatus.setVisible(true);
            }
            
            sendRematchRequest();
        }
    }
    
    @FXML
    private void handleReturnToLobby() {
        UtilsViews.setView("ViewOpponentSelection");
        rematchProposed = false;
        rematchAccepted = false;
        this.player1Name = "";
        this.player2Name = "";
        this.winnerName = "";
        
        if (Main.ctrlOpponentSelection != null) {
            Main.requestPlayersList();
        }
    }
    
    private void sendRematchRequest() {
        try {
            System.out.println("Solicitud de revancha enviada - " + player1Name + " vs " + player2Name);
        } catch (Exception e) {
            System.err.println("Error enviando solicitud de revancha: " + e.getMessage());
        }
    }
    
    public void handleRematchResponse(boolean accepted) {
        if (accepted) {
            rematchAccepted = true;
            Main.pauseDuring(1000, () -> {
                UtilsViews.setViewAnimating("ViewLoading");
            });
        } else {
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