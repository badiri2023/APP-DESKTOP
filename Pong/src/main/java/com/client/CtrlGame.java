package com.client;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.animation.AnimationTimer;
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
    private double ballX, ballY, ballSpeedX, ballSpeedY;
    private int player1Score = 0, player2Score = 0;
    private boolean gameStarted = false;
    private boolean countdownActive = true;
    private int countdownValue = 3;
    private long lastCountdownUpdate = 0;
    
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
            
            // Iniciar game loop
            startGameLoop();
            
        } catch (Exception e) {
            System.err.println("Error en CtrlGame: " + e.getMessage());
            e.printStackTrace();
        }
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
        // Posiciones iniciales
        player1Y = FIELD_HEIGHT / 2 - PADDLE_HEIGHT / 2;
        player2Y = FIELD_HEIGHT / 2 - PADDLE_HEIGHT / 2;
        ballX = FIELD_WIDTH / 2 - BALL_SIZE / 2;
        ballY = FIELD_HEIGHT / 2 - BALL_SIZE / 2;
        
        // Reset scores
        player1Score = 0;
        player2Score = 0;
        gameStarted = false;
        countdownActive = true;
        countdownValue = 3;
        lastCountdownUpdate = System.nanoTime();
        
        updateScoreDisplay();
    }
    
    private void startGame() {
        // Dirección inicial aleatoria
        ballSpeedX = (Math.random() > 0.5 ? 1 : -1) * 5;
        ballSpeedY = (Math.random() * 2 - 1) * 3;
        gameStarted = true;
    }
    
    private void setupControls() {
        gameContainer.setFocusTraversable(true);
        gameContainer.requestFocus();
        
        gameContainer.setOnKeyPressed(this::handleKeyPress);
        gameContainer.setOnKeyReleased(this::handleKeyRelease);
    }
    
    private void handleKeyPress(KeyEvent event) {
        // Controles para jugador 1 (W/S o Flechas arriba/abajo)
        if (event.getCode() == KeyCode.W || event.getCode() == KeyCode.UP) {
            movePlayer1(-8);
        } else if (event.getCode() == KeyCode.S || event.getCode() == KeyCode.DOWN) {
            movePlayer1(8);
        }
        
        // Controles para jugador 2 (I/K)
        if (event.getCode() == KeyCode.I) {
            movePlayer2(-8);
        } else if (event.getCode() == KeyCode.K) {
            movePlayer2(8);
        }
        
        // Espacio para iniciar juego
        if (event.getCode() == KeyCode.SPACE && !gameStarted && !countdownActive) {
            startGame();
        }
    }
    
    private void handleKeyRelease(KeyEvent event) {
        // Puedes implementar movimiento suave aquí si es necesario
    }
    
    private void movePlayer1(double delta) {
        player1Y += delta;
        // Limitar dentro del campo
        player1Y = Math.max(0, Math.min(FIELD_HEIGHT - PADDLE_HEIGHT, player1Y));
    }
    
    private void movePlayer2(double delta) {
        player2Y += delta;
        // Limitar dentro del campo
        player2Y = Math.max(0, Math.min(FIELD_HEIGHT - PADDLE_HEIGHT, player2Y));
    }
    
    private void startGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateGame();
                renderGame();
            }
        };
        gameLoop.start();
    }
    
    private void updateGame() {
        if (countdownActive) {
            handleCountdown();
            return;
        }
        
        if (!gameStarted) {
            return;
        }
        
        // Mover pelota
        ballX += ballSpeedX;
        ballY += ballSpeedY;
        
        // Rebotes en paredes superior e inferior
        if (ballY <= 0 || ballY >= FIELD_HEIGHT - BALL_SIZE) {
            ballSpeedY = -ballSpeedY;
        }
        
        // Colisión con pala izquierda
        if (ballX <= PADDLE_WIDTH && 
            ballY + BALL_SIZE >= player1Y && 
            ballY <= player1Y + PADDLE_HEIGHT) {
            ballSpeedX = Math.abs(ballSpeedX) * 1.1; // Aumentar velocidad
            ballSpeedY += (Math.random() * 2 - 1) * 2; // Variación aleatoria
        }
        
        // Colisión con pala derecha
        if (ballX >= FIELD_WIDTH - PADDLE_WIDTH - BALL_SIZE && 
            ballY + BALL_SIZE >= player2Y && 
            ballY <= player2Y + PADDLE_HEIGHT) {
            ballSpeedX = -Math.abs(ballSpeedX) * 1.1; // Aumentar velocidad
            ballSpeedY += (Math.random() * 2 - 1) * 2; // Variación aleatoria
        }
        
        // Puntuación
        if (ballX < 0) {
            player2Score++;
            resetBall();
            updateScoreDisplay();
            checkGameOver();
        } else if (ballX > FIELD_WIDTH) {
            player1Score++;
            resetBall();
            updateScoreDisplay();
            checkGameOver();
        }
    }
    
    private void handleCountdown() {
        long currentTime = System.nanoTime();
        if (currentTime - lastCountdownUpdate >= 1_000_000_000) { // 1 segundo
            countdownValue--;
            lastCountdownUpdate = currentTime;
            
            if (countdownValue <= 0) {
                countdownActive = false;
            }
        }
    }
    
    private void resetBall() {
        ballX = FIELD_WIDTH / 2 - BALL_SIZE / 2;
        ballY = FIELD_HEIGHT / 2 - BALL_SIZE / 2;
        gameStarted = false;
    }
    
    private void checkGameOver() {
        if (player1Score >= 5 || player2Score >= 5) {
            gameStarted = false;
            // Pasar a vista de Game Over
            Main.pauseDuring(2000, () -> {
                String winner = player1Score >= 5 ? "Jugador 1" : "Jugador 2";
                UtilsViews.setViewAnimating("ViewGameOver");
                CtrlGameOver ctrlGameOver = (CtrlGameOver) UtilsViews.getController("ViewGameOver");
                if (ctrlGameOver != null) {
                    ctrlGameOver.setWinner(winner, player1Score, player2Score);
                }
            });
        }
    }
    
    private void renderGame() {
        gc.clearRect(0, 0, FIELD_WIDTH, FIELD_HEIGHT);
        
        // Dibujar campo
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeRect(0, 0, FIELD_WIDTH, FIELD_HEIGHT);
        
        // Línea central punteada
        gc.setLineWidth(1);
        gc.setLineDashes(10);
        gc.strokeLine(FIELD_WIDTH / 2, 0, FIELD_WIDTH / 2, FIELD_HEIGHT);
        gc.setLineDashes(null);
        
        // Dibujar palas
        gc.setFill(Color.WHITE);
        gc.fillRect(0, player1Y, PADDLE_WIDTH, PADDLE_HEIGHT);
        gc.fillRect(FIELD_WIDTH - PADDLE_WIDTH, player2Y, PADDLE_WIDTH, PADDLE_HEIGHT);
        
        if (countdownActive) {
            // Mostrar cuenta regresiva
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font(retroFont.getFamily(), FontWeight.BOLD, 48));
            gc.fillText(String.valueOf(countdownValue), FIELD_WIDTH / 2 - 10, FIELD_HEIGHT / 2);
        } else if (!gameStarted) {
            // Mostrar pelota centrada
            gc.setFill(Color.WHITE);
            gc.fillOval(ballX, ballY, BALL_SIZE, BALL_SIZE);
            
            // Mensaje de inicio
            gc.setFont(Font.font(retroFont.getFamily(), FontWeight.BOLD, 24));
            gc.fillText("PRESIONA ESPACIO", FIELD_WIDTH / 2 - 120, FIELD_HEIGHT / 2 + 50);
        } else {
            // Dibujar pelota en movimiento
            gc.setFill(Color.WHITE);
            gc.fillOval(ballX, ballY, BALL_SIZE, BALL_SIZE);
        }
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