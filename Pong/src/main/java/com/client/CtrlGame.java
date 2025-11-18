package com.client;

import java.net.URL;
import java.util.ResourceBundle;

import org.json.JSONObject;

import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

public class CtrlGame implements Initializable {

    @FXML private AnchorPane gameContainer;
    @FXML private Canvas gameCanvas;
    @FXML private Label scoreLabel;
    
    private GraphicsContext gc;
    private Font retroFont;

    @FXML private Label player1ScoreLabel;
    @FXML private Label player2ScoreLabel;
    @FXML private Label countdownLabel;
    @FXML private VBox countdownOverlay;
    
    // Variables del juego
    private double player1Y, player2Y;
    private double ballX, ballY;
    private int player1Score = 0, player2Score = 0;
    private boolean gameActive = false;
    private boolean countdownActive = false;
    private String countdownValue = "";
    
    // Nuevas variables para el flujo mejorado
    private String playerRole = "";
    private String opponentName = "";
    private String currentPhase = "waiting"; // waiting, choosing, announcing, countdown, playing
    
    // Dimensiones del juego
    private final double PADDLE_WIDTH = 15;
    private final double PADDLE_HEIGHT = 80;
    private final double BALL_SIZE = 15;
    private final double FIELD_WIDTH = 800;
    private final double FIELD_HEIGHT = 500;
    
    private AnimationTimer gameLoop;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Configurar canvas
            gameCanvas.setWidth(FIELD_WIDTH);
            gameCanvas.setHeight(FIELD_HEIGHT);
            gc = gameCanvas.getGraphicsContext2D();
            
            // Cargar fuente
            retroFont = Font.loadFont(getClass().getResourceAsStream("/assets/fonts/8bitOperatorPlus8-Regular.ttf"), 14);
            if (retroFont == null) {
                retroFont = Font.font("Consolas", 14);
            }
            
            // Aplicar estilos
            applyStyles();
            
            // Inicializar posiciones
            resetGame();
            
            // Configurar controles
            setupControls();
            
            // Iniciar game loop (solo renderizado)
            startGameLoop();
            
        } catch (Exception e) {
            System.err.println("Error en CtrlGame: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // NUEVO MÉTODO: Iniciar secuencia completa del juego
    public void startGameSequence() {
        resetGame();
        currentPhase = "waiting"; // Cambiar a "waiting" para esperar mensajes del servidor
        System.out.println("🎮 Secuencia de juego iniciada - Esperando mensajes del servidor...");
    }
    
    // NUEVO MÉTODO: Mostrar animación de elección de jugador inicial
    public void showChoosingStarter() {
        currentPhase = "choosing";
        System.out.println("Servidor eligiendo jugador inicial...");
    }
    
    // NUEVO MÉTODO: Mostrar anuncio de quién inicia
    public void showStarterAnnouncement(String message, long duration) {
        currentPhase = "announcing";
        countdownValue = message;
        countdownActive = true;
        
        // Configurar timer para ocultar el anuncio
        PauseTransition pause = new PauseTransition(Duration.millis(duration));
        pause.setOnFinished(e -> {
            countdownActive = false;
            // La cuenta regresiva comenzará automáticamente desde el servidor
        });
        pause.play();
    }
    
    public void setPlayerRole(String role, String opponent) {
        this.playerRole = role;
        this.opponentName = opponent;
        System.out.println("Rol asignado: " + role + ", Oponente: " + opponent);
        
        // Actualizar interfaz según el rol
        if (scoreLabel != null) {
            if ("p1".equals(role)) {
                scoreLabel.setText(Main.ctrlLogin.getUserName() + " vs " + opponent);
            } else {
                scoreLabel.setText(opponent + " vs " + Main.ctrlLogin.getUserName());
            }
        }
    }
    
    public void updateGameState(double p1Y, double p2Y, double ballX, double ballY, int score1, int score2) {
        // Solo actualizar estado si estamos en fase de juego activo
        if ("playing".equals(currentPhase)) {
            this.player1Y = p1Y * (FIELD_HEIGHT - PADDLE_HEIGHT);
            this.player2Y = p2Y * (FIELD_HEIGHT - PADDLE_HEIGHT);
            this.ballX = ballX * (FIELD_WIDTH - BALL_SIZE);
            this.ballY = ballY * (FIELD_HEIGHT - BALL_SIZE);
            this.player1Score = score1;
            this.player2Score = score2;
            
            updateScoreDisplay();
        }
    }
    
    public void handleCountdown(String value) {
        if ("GO!".equals(value)) {
            countdownActive = false;
            gameActive = true;
            currentPhase = "playing";
            countdownValue = "";
            System.out.println("¡JUEGO INICIADO!");
        } else {
            countdownActive = true;
            countdownValue = value;
            currentPhase = "countdown";
        }
    }
    
    public void handleGameOver(String winner, int finalScore1, int finalScore2) {
        gameActive = false;
        currentPhase = "finished";
        stopGame();
        
        Main.pauseDuring(2000, () -> {
            UtilsViews.setViewAnimating("ViewGameOver");
            CtrlGameOver ctrlGameOver = (CtrlGameOver) UtilsViews.getController("ViewGameOver");
            if (ctrlGameOver != null) {
                ctrlGameOver.setWinner(winner, finalScore1, finalScore2);
            }
        });
    }
    
    private void applyStyles() {
        if (gameContainer != null) {
            gameContainer.setStyle("-fx-background-color: #000000;");
        }
        
        if (scoreLabel != null) {
            scoreLabel.setFont(Font.font(retroFont.getFamily(), FontWeight.BOLD, 32));
            scoreLabel.setTextFill(Color.WHITE);
            scoreLabel.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(255,255,255,0.8), 5, 0, 0, 0);");
            updateScoreDisplay();
        }
    }
    
    private void resetGame() {
        player1Y = FIELD_HEIGHT / 2 - PADDLE_HEIGHT / 2;
        player2Y = FIELD_HEIGHT / 2 - PADDLE_HEIGHT / 2;
        ballX = FIELD_WIDTH / 2 - BALL_SIZE / 2;
        ballY = FIELD_HEIGHT / 2 - BALL_SIZE / 2;
        
        player1Score = 0;
        player2Score = 0;
        gameActive = false;
        countdownActive = false;
        countdownValue = "";
        currentPhase = "waiting";
        
        updateScoreDisplay();
    }
    
    private void setupControls() {
        gameContainer.setFocusTraversable(true);
        gameContainer.requestFocus();
        
        gameContainer.setOnKeyPressed(this::handleKeyPress);
        gameContainer.setOnKeyReleased(this::handleKeyRelease);
    }
    
    private void handleKeyPress(KeyEvent event) {
        // Solo procesar movimientos si el juego está activo
        if (!gameActive || !"playing".equals(currentPhase)) {
            return;
        }
        
        double moveDelta = 0;
        boolean moved = false;
        
        // ✅ CONTROL ESPECÍFICO POR ROL (opcional)
        if ("p1".equals(playerRole)) {
            // Jugador 1 (izquierda) - solo flechas
            if (event.getCode() == KeyCode.UP) {
                moveDelta = -0.05;
                moved = true;
            } else if (event.getCode() == KeyCode.DOWN) {
                moveDelta = 0.05;
                moved = true;
            }
        } else if ("p2".equals(playerRole)) {
            // Jugador 2 (derecha) - también solo flechas
            if (event.getCode() == KeyCode.UP) {
                moveDelta = -0.05;
                moved = true;
            } else if (event.getCode() == KeyCode.DOWN) {
                moveDelta = 0.05;
                moved = true;
            }
        }
        
        // Enviar movimiento al servidor
        if (moved) {
            sendMoveToServer(moveDelta);
        }
        
        event.consume();
    }
    
    private void sendMoveToServer(double delta) {
        try {
            // Calcular nueva posición (0-1)
            double currentY = "p1".equals(playerRole) ? 
                player1Y / (FIELD_HEIGHT - PADDLE_HEIGHT) : 
                player2Y / (FIELD_HEIGHT - PADDLE_HEIGHT);
            double newY = Math.max(0, Math.min(1, currentY + delta));
            
            JSONObject moveMsg = new JSONObject();
            moveMsg.put("type", "move");
            moveMsg.put("y_pos", newY);
            
            if (Main.wsClient != null && Main.wsClient.isOpen()) {
                Main.wsClient.safeSend(moveMsg.toString());
            }
        } catch (Exception e) {
            System.err.println("Error enviando movimiento al servidor: " + e.getMessage());
        }
    }
    
    private void handleKeyRelease(KeyEvent event) {
        // Para movimiento suave si se implementa
    }
    
    private void startGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                renderGame();
            }
        };
        gameLoop.start();
    }
    
    private void renderGame() {
        gc.clearRect(0, 0, FIELD_WIDTH, FIELD_HEIGHT);
        
        // Dibujar campo (siempre visible)
        drawField();
        
        // Dibujar elementos según la fase actual
        switch (currentPhase) {
            case "choosing":
                drawChoosingPhase();
                break;
            case "announcing":
                drawAnnouncingPhase();
                break;
            case "countdown":
                drawCountdownPhase();
                break;
            case "playing":
                drawPlayingPhase();
                break;
            case "waiting":
            default:
                drawWaitingPhase();
                break;
        }
    }
    
    private void drawField() {
        // Campo de juego
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeRect(0, 0, FIELD_WIDTH, FIELD_HEIGHT);
        
        // Línea central punteada
        gc.setLineWidth(1);
        gc.setLineDashes(10);
        gc.strokeLine(FIELD_WIDTH / 2, 0, FIELD_WIDTH / 2, FIELD_HEIGHT);
        gc.setLineDashes(null);
    }
    
    private void drawChoosingPhase() {
        gc.setFill(Color.YELLOW);
        gc.setFont(Font.font(retroFont.getFamily(), FontWeight.BOLD, 32));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("ELEGIENDO JUGADOR INICIAL...", FIELD_WIDTH / 2, FIELD_HEIGHT / 2);
        gc.setTextAlign(TextAlignment.LEFT);
    }
    
    private void drawAnnouncingPhase() {
        if (countdownActive) {
            gc.setFill(Color.CYAN);
            gc.setFont(Font.font(retroFont.getFamily(), FontWeight.BOLD, 28));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(countdownValue, FIELD_WIDTH / 2, FIELD_HEIGHT / 2);
            gc.setTextAlign(TextAlignment.LEFT);
        }
    }
    
    private void drawCountdownPhase() {
        if (countdownActive) {
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font(retroFont.getFamily(), FontWeight.BOLD, 48));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(countdownValue, FIELD_WIDTH / 2, FIELD_HEIGHT / 2);
            gc.setTextAlign(TextAlignment.LEFT);
        }
    }
    
    private void drawPlayingPhase() {
        // Dibujar palas
        gc.setFill(Color.WHITE);
        gc.fillRect(0, player1Y, PADDLE_WIDTH, PADDLE_HEIGHT);
        gc.fillRect(FIELD_WIDTH - PADDLE_WIDTH, player2Y, PADDLE_WIDTH, PADDLE_HEIGHT);
        
        // Dibujar pelota
        gc.fillOval(ballX, ballY, BALL_SIZE, BALL_SIZE);
        
        // Información del rol
        if (!playerRole.isEmpty()) {
            gc.setFont(Font.font(retroFont.getFamily(), 12));
            gc.setFill(Color.GRAY);
            gc.fillText("Tú: " + ("p1".equals(playerRole) ? "Jugador 1 (Izq)" : "Jugador 2 (Der)"), 10, 20);
        }
    }
    
    private void drawWaitingPhase() {
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(retroFont.getFamily(), FontWeight.BOLD, 24));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("ESPERANDO INICIO DE PARTIDA", FIELD_WIDTH / 2, FIELD_HEIGHT / 2);
        gc.setTextAlign(TextAlignment.LEFT);
    }
    
    private void updateScoreDisplay() {
        if (scoreLabel != null) {
            scoreLabel.setText(player1Score + " : " + player2Score);
        }
        if (player1ScoreLabel != null) {
            player1ScoreLabel.setText(String.valueOf(player1Score));
        }
        if (player2ScoreLabel != null) {
            player2ScoreLabel.setText(String.valueOf(player2Score));
        }
    }
    
    public void stopGame() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }
}