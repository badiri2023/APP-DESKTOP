package com.client;

import java.net.URL;
import java.util.ResourceBundle;

import org.json.JSONObject;

import com.client.enums.GamePhase;

import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
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
    @FXML private Label player1Label;
    @FXML private Label player2Label;
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
    private GamePhase currentPhase = GamePhase.WAITING;
    
    // Dimensiones del juego
    private final double PADDLE_WIDTH = 15;
    private final double PADDLE_HEIGHT = 80;
    private final double BALL_SIZE = 15;
    private final double FIELD_WIDTH = 800;
    private final double FIELD_HEIGHT = 500;

    private boolean upPressed = false;
    private boolean downPressed = false;
    private final double MOVE_SPEED = 0.03; 
    
    private Timeline movementTimeline;
    
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
            
            Platform.runLater(() -> {
                gameContainer.requestFocus();
                System.out.println("✅ Focus forzado en gameContainer");
            });
            
            // Iniciar game loop (solo renderizado)
            startGameLoop();
            
        } catch (Exception e) {
            System.err.println("Error en CtrlGame: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupContinuousMovement() {
        movementTimeline = new Timeline(
            new KeyFrame(Duration.millis(16), e -> handleContinuousMovement())
        );
        movementTimeline.setCycleCount(Timeline.INDEFINITE);
    }

    private void handleContinuousMovement() {
        if (!gameActive || GamePhase.PLAYING != currentPhase) {
            return;
        }
        
        double moveDelta = 0;
        
        if (upPressed && !downPressed) {
            moveDelta = -MOVE_SPEED;
        } else if (downPressed && !upPressed) {
            moveDelta = MOVE_SPEED;
        }
        
        if (moveDelta != 0) {
            sendMoveToServer(moveDelta);
        }
    }
    
    // NUEVO MÉTODO: Iniciar secuencia completa del juego
    public void startGameSequence() {
        resetGame();
        currentPhase = GamePhase.WAITING;
        System.out.println("🎮 Secuencia de juego iniciada - Esperando mensajes del servidor...");
    }
    
    // NUEVO MÉTODO: Mostrar animación de elección de jugador inicial
    public void showChoosingStarter() {
        currentPhase = GamePhase.CHOOSING;
        System.out.println("Servidor eligiendo jugador inicial...");
    }
    
    // NUEVO MÉTODO: Mostrar anuncio de quién inicia
    public void showStarterAnnouncement(String message, long duration) {
        currentPhase = GamePhase.ANNOUNCING;
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
        
        // Obtener nombre del jugador actual
        String currentPlayerName = Main.ctrlLogin.getUserName();
        String selectedPlayerName = opponent;
        
        // Asignar nombres a labels según el rol
        if ("p1".equals(role)) {
            // Este jugador es P1 (izquierda)
            if (player1Label != null) {
                player1Label.setText(currentPlayerName);
            }
            if (player2Label != null) {
                player2Label.setText(selectedPlayerName);
            }
        } else {
            // Este jugador es P2 (derecha)
            if (player1Label != null) {
                player1Label.setText(selectedPlayerName);
            }
            if (player2Label != null) {
                player2Label.setText(currentPlayerName);
            }
        }
        
        // Actualizar interfaz según el rol
        if (scoreLabel != null) {
            if ("p1".equals(role)) {
                scoreLabel.setText(currentPlayerName + " vs " + selectedPlayerName);
            } else {
                scoreLabel.setText(selectedPlayerName + " vs " + currentPlayerName);
            }
        }
    }

    public void updateGameState(double p1Y, double p2Y, double ballX, double ballY, int score1, int score2) {
        // Suavizado de movimiento para reducir latencia visual
        double smoothing = 0.7;
        
        this.player1Y = smoothing * this.player1Y + (1 - smoothing) * (p1Y * (FIELD_HEIGHT - PADDLE_HEIGHT));
        this.player2Y = smoothing * this.player2Y + (1 - smoothing) * (p2Y * (FIELD_HEIGHT - PADDLE_HEIGHT));
        
        // Para la pelota, menos suavizado para mayor responsividad
        this.ballX = ballX * (FIELD_WIDTH - BALL_SIZE);
        this.ballY = ballY * (FIELD_HEIGHT - BALL_SIZE);
        
        this.player1Score = score1;
        this.player2Score = score2;
        
        updateScoreDisplay();
    }
    
    public void handleCountdown(String value) {
        if ("GO!".equals(value)) {
            countdownActive = false;
            gameActive = true;
            currentPhase = GamePhase.PLAYING;
            countdownValue = "";
            System.out.println("¡JUEGO INICIADO!");
            
            Platform.runLater(() -> {
                gameContainer.requestFocus();
                System.out.println("JUEGO ACTIVO - Focus forzado en gameContainer");
            });
        } else {
            countdownActive = true;
            countdownValue = value;
            currentPhase = GamePhase.COUNTDOWN;
        }
    }
    
    public void handleGameOver(String winner, int finalScore1, int finalScore2) {
        gameActive = false;
        currentPhase = GamePhase.FINISHED;
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
        currentPhase = GamePhase.WAITING;
        
        updateScoreDisplay();
    }
    
    private void setupControls() {
        gameContainer.setFocusTraversable(true);
        
        gameContainer.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                System.out.println("Game container OBTUVO el focus - Teclado ACTIVO");
            } else {
                System.out.println("Game container PERDIÓ el focus");
                // Intentar recuperar el focus automáticamente
                Platform.runLater(() -> {
                    gameContainer.requestFocus();
                    System.out.println("Intentando recuperar focus...");
                });
            }
        });
        
        gameContainer.setOnKeyPressed(this::handleKeyPress);
        gameContainer.setOnKeyReleased(this::handleKeyRelease);
        
        gameCanvas.setOnMouseClicked(e -> {
            gameContainer.requestFocus();
            System.out.println("🖱️ Canvas clickeado - Focus solicitado");
        });
        
        // Solicitar focus inicial
        gameContainer.requestFocus();
        System.out.println("Controles configurados - Focus solicitado");
    }

    private void debugKeyEvent(KeyEvent event, String action) {
        System.out.println("=== TECLA " + action + " ===");
        System.out.println("Tecla: " + event.getCode());
        System.out.println("GameActive: " + gameActive);
        System.out.println("Phase: " + currentPhase);
        System.out.println("Role: " + playerRole);
        System.out.println("Container focused: " + gameContainer.isFocused());
        System.out.println("Canvas focused: " + gameCanvas.isFocused());
        System.out.println("===========================");
    }

    private void handleKeyPress(KeyEvent event) {
        debugKeyEvent(event, "PRESIONADA");
        
        // Solo procesar movimientos si el juego está activo
        if (!gameActive || GamePhase.PLAYING != currentPhase) {
            System.out.println("❌ Movimiento ignorado - Juego no activo o fase incorrecta");
            return;
        }
        
        double moveDelta = 0;
        boolean moved = false;
        
        if (event.getCode() == KeyCode.UP) {
            moveDelta = -0.08;
            moved = true;
            System.out.println("↑ Moviendo hacia ARRIBA");
        } else if (event.getCode() == KeyCode.DOWN) {
            moveDelta = 0.08;
            moved = true;
            System.out.println("↓ Moviendo hacia ABAJO");
        }
        
        if (moved) {
            sendMoveToServer(moveDelta);
        }
        
        event.consume();
    }

    private void startContinuousMovement() {
        if (movementTimeline != null && movementTimeline.getStatus() != Animation.Status.RUNNING) {
            movementTimeline.play();
            System.out.println("🎮 Movimiento continuo INICIADO");
        }
    }
    
    // ✅ NUEVO: Detener movimiento continuo
    private void stopContinuousMovement() {
        if (movementTimeline != null) {
            movementTimeline.stop();
            System.out.println("🎮 Movimiento continuo DETENIDO");
        }
    }

    private void sendMoveToServer(double delta) {
        try {
            System.out.println("=== ENVIANDO MOVIMIENTO ===");
            
            // Obtener posición actual NORMALIZADA (0-1)
            double currentY;
            if ("p1".equals(playerRole)) {
                currentY = player1Y / (FIELD_HEIGHT - PADDLE_HEIGHT);
                System.out.println("Jugador: P1 (Izquierda)");
            } else {
                currentY = player2Y / (FIELD_HEIGHT - PADDLE_HEIGHT);
                System.out.println("Jugador: P2 (Derecha)");
            }
            
            double newY = Math.max(0, Math.min(1, currentY + delta));
            System.out.println("Posición actual: " + String.format("%.3f", currentY));
            System.out.println("Delta: " + String.format("%.3f", delta));
            System.out.println("Nueva posición: " + String.format("%.3f", newY));
            
            JSONObject moveMsg = new JSONObject();
            moveMsg.put("type", "move");
            moveMsg.put("y_pos", newY);
            
            String message = moveMsg.toString();
            System.out.println("Mensaje JSON: " + message);
            
            if (Main.wsClient != null) {
                System.out.println("WebSocket estado: " + (Main.wsClient.isOpen() ? "CONECTADO" : "DESCONECTADO"));
                Main.wsClient.safeSend(message);
                System.out.println("Mensaje enviado al servidor");
                
                // Actualizar visualmente localmente también
                if ("p1".equals(playerRole)) {
                    player1Y = newY * (FIELD_HEIGHT - PADDLE_HEIGHT);
                } else {
                    player2Y = newY * (FIELD_HEIGHT - PADDLE_HEIGHT);
                }
            } else {
                System.out.println("ERROR: WebSocket client es NULL");
            }
            
            System.out.println("=== FIN ENVÍO ===");
            
        } catch (Exception e) {
            System.err.println("ERROR CRÍTICO en sendMoveToServer: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void handleKeyRelease(KeyEvent event) {
        debugKeyEvent(event, "LIBERADA");
        event.consume();
    }

    public void verifyGameState() {
        if (currentPhase == GamePhase.PLAYING && !gameActive) {
            System.err.println("⚠️ Estado inconsistente - Corrigiendo...");
            gameActive = true;
        }
        
        // Verificar que las coordenadas estén en rango
        player1Y = Math.max(0, Math.min(FIELD_HEIGHT - PADDLE_HEIGHT, player1Y));
        player2Y = Math.max(0, Math.min(FIELD_HEIGHT - PADDLE_HEIGHT, player2Y));
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
            case CHOOSING:
                drawChoosingPhase();
                break;
            case ANNOUNCING:
                drawAnnouncingPhase();
                break;
            case COUNTDOWN:
                drawCountdownPhase();
                break;
            case PLAYING:
                drawPlayingPhase();
                break;
            case WAITING:
            default:
                drawWaitingPhase();
                break;
            case FINISHED:
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
        gc.fillText("ELIGIENDO JUGADOR INICIAL...", FIELD_WIDTH / 2, FIELD_HEIGHT / 2);
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
        stopContinuousMovement(); 
        upPressed = false;
        downPressed = false;
    }
}