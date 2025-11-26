package com.client;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

public class CtrlLoading implements Initializable {

    @FXML private Label loadingText;
    @FXML private Label messageLabel;
    @FXML private ProgressBar loadingBar;
    
    private Timeline loadingTimeline;
    private Timeline textAnimation;
    private Timeline messageTimeline;
    private Font retroFont;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Cargar fuente retro
            retroFont = Font.loadFont(getClass().getResourceAsStream("/assets/fonts/BrunoAce-Regular.ttf"), 14);
            if (retroFont == null) {
                retroFont = Font.font("Consolas", 14);
            }
            
            applyStyles();
            
        } catch (Exception e) {
            System.err.println("Error en CtrlLoading: " + e.getMessage());
        }
    }
    
    private void applyStyles() {
        // Estilo del texto de carga - BLANCO
        if (loadingText != null) {
            loadingText.setFont(Font.font(retroFont.getFamily(), FontWeight.BOLD, 32));
            loadingText.setTextFill(Color.WHITE);
            loadingText.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(255,255,255,0.8), 8, 0, 0, 0);");
        }
        
        // Estilo del label de mensajes - BLANCO
        if (messageLabel != null) {
            messageLabel.setFont(Font.font(retroFont.getFamily(), FontWeight.BOLD, 20));
            messageLabel.setTextFill(Color.WHITE);
            messageLabel.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(255,255,255,0.6), 5, 0, 0, 0);");
            messageLabel.setVisible(false);
        }
        
        // Estilo de la barra de progreso - BLANCO/GRIS
        if (loadingBar != null) {
            loadingBar.setStyle(
                "-fx-accent: #ffffff; " +  // Barra de progreso BLANCA
                "-fx-background-color: #333333; " +  // Fondo GRIS oscuro
                "-fx-border-color: #ffffff; " +  // Borde BLANCO
                "-fx-border-width: 2; " +
                "-fx-border-radius: 5; " +
                "-fx-background-radius: 5; " +
                "-fx-pref-width: 400; " +
                "-fx-pref-height: 25;"
            );
        }
    }
    
    /**
     * Muestra un mensaje temporal en la pantalla de carga
     */
    public void showTemporaryMessage(String message, long durationMs) {
        if (messageLabel != null) {
            // Detener timeline anterior si existe
            if (messageTimeline != null) {
                messageTimeline.stop();
            }
            
            // Mostrar el mensaje
            messageLabel.setText(message);
            messageLabel.setVisible(true);
            
            System.out.println("Mostrando mensaje en Loading: " + message + " por " + durationMs + "ms");
            
            // Crear timeline para ocultar el mensaje después del tiempo especificado
            messageTimeline = new Timeline(
                new KeyFrame(Duration.millis(durationMs), e -> {
                    messageLabel.setVisible(false);
                    System.out.println("Mensaje ocultado: " + message);
                })
            );
            messageTimeline.play();
        }
    }

    /**
     * Inicia una carga indeterminada (espera por mensajes del servidor)
     */
    public void startIndeterminateLoading() {
        stopAllAnimations();
        
        // Mensaje inicial
        setLoadingMessage("CARGANDO PARTIDA...");
        
        // Barra de progreso indeterminada
        loadingBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        
        // Animación de texto parpadeante
        textAnimation = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(loadingText.opacityProperty(), 1.0)),
            new KeyFrame(Duration.seconds(0.7), new KeyValue(loadingText.opacityProperty(), 0.5)),
            new KeyFrame(Duration.seconds(1.4), new KeyValue(loadingText.opacityProperty(), 1.0))
        );
        textAnimation.setCycleCount(Timeline.INDEFINITE);
        textAnimation.play();
        
        System.out.println("🔄 Carga indeterminada iniciada");
    }

    /**
     * Completa la carga y va al juego
     */
    public void completeLoadingAndGoToGame(Runnable onComplete) {
        stopAllAnimations();
        
        // Animación rápida de finalización
        loadingBar.setProgress(1.0);
        setLoadingMessage("¡LISTO!");
        
        // Esperar un momento y luego ejecutar el callback
        Timeline completionTimeline = new Timeline(
            new KeyFrame(Duration.millis(800), e -> {
                if (onComplete != null) {
                    onComplete.run();
                }
            })
        );
        completionTimeline.play();
    }
    
    /**
     * Inicia la animación de carga completa con barra de progreso y texto parpadeante
     */
    public void startLoadingAnimation(Runnable onFinished) {
        // Detener animaciones previas si existen
        stopAllAnimations();
        
        // Reiniciar la barra de progreso
        loadingBar.setProgress(0);
        
        // Ocultar mensaje temporal si está visible
        if (messageLabel != null) {
            messageLabel.setVisible(false);
        }
        
        // Animación del texto (parpadeo)
        textAnimation = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(loadingText.opacityProperty(), 1.0)),
            new KeyFrame(Duration.seconds(0.5), new KeyValue(loadingText.opacityProperty(), 0.3)),
            new KeyFrame(Duration.seconds(1.0), new KeyValue(loadingText.opacityProperty(), 1.0))
        );
        textAnimation.setCycleCount(Timeline.INDEFINITE);
        textAnimation.play();
        
        // Animación de la barra de progreso (2.5 segundos)
        loadingTimeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(loadingBar.progressProperty(), 0)),
            new KeyFrame(Duration.seconds(2.5), new KeyValue(loadingBar.progressProperty(), 1))
        );
        
        loadingTimeline.setOnFinished(event -> {
            stopAllAnimations();
            if (onFinished != null) {
                onFinished.run();
            }
        });
        
        loadingTimeline.play();
    }
    
    /**
     * Muestra solo el mensaje de carga sin animación de progreso (para esperas indeterminadas)
     */
    public void showLoadingMessage(String message) {
        stopAllAnimations();
        
        if (loadingText != null) {
            loadingText.setText(message);
        }
        
        // Solo animación de texto parpadeante, sin barra de progreso
        textAnimation = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(loadingText.opacityProperty(), 1.0)),
            new KeyFrame(Duration.seconds(0.7), new KeyValue(loadingText.opacityProperty(), 0.5)),
            new KeyFrame(Duration.seconds(1.4), new KeyValue(loadingText.opacityProperty(), 1.0))
        );
        textAnimation.setCycleCount(Timeline.INDEFINITE);
        textAnimation.play();
        
        // Barra al 50% para indicar espera
        loadingBar.setProgress(0.5);
    }
    
    /**
     * Establece el mensaje de carga sin iniciar animaciones
     */
    public void setLoadingMessage(String message) {
        if (loadingText != null) {
            loadingText.setText(message);
        }
    }
    
    /**
     * Completa la carga inmediatamente (para casos de carga rápida)
     */
    public void completeLoading() {
        stopAllAnimations();
        loadingBar.setProgress(1.0);
        if (loadingText != null) {
            loadingText.setOpacity(1.0);
        }
    }
    
    /**
     * Detiene todas las animaciones y reinicia el estado
     */
    public void stopLoading() {
        stopAllAnimations();
        loadingBar.setProgress(0);
        if (loadingText != null) {
            loadingText.setOpacity(1.0);
        }
        if (messageLabel != null) {
            messageLabel.setVisible(false);
        }
    }
    
    /**
     * Detiene todas las animaciones en curso
     */
    private void stopAllAnimations() {
        if (loadingTimeline != null) {
            loadingTimeline.stop();
            loadingTimeline = null;
        }
        if (textAnimation != null) {
            textAnimation.stop();
            textAnimation = null;
        }
        if (messageTimeline != null) {
            messageTimeline.stop();
            messageTimeline = null;
        }
        
        // Asegurar que el texto sea visible al detener
        if (loadingText != null) {
            loadingText.setOpacity(1.0);
        }
    }
    
    /**
     * Método para limpiar recursos cuando se cambia de vista
     */
    public void cleanup() {
        stopAllAnimations();
    }
}