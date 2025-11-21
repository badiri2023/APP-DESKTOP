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
    @FXML private ProgressBar loadingBar;
    
    private Timeline loadingTimeline;
    private Timeline textAnimation;
    private Font retroFont;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Cargar fuente retro
            retroFont = Font.loadFont(getClass().getResourceAsStream("/assets/fonts/8bitOperatorPlus8-Regular.ttf"), 14);
            if (retroFont == null) {
                retroFont = Font.font("Consolas", 14);
            }
            
            applyStyles();
            // NO iniciar la animación automáticamente - solo cuando se solicite
            
        } catch (Exception e) {
            System.err.println("Error en CtrlLoading: " + e.getMessage());
        }
    }
    
    private void applyStyles() {
        // Estilo del texto de carga
        if (loadingText != null) {
            loadingText.setFont(Font.font(retroFont.getFamily(), FontWeight.BOLD, 24));
            loadingText.setTextFill(Color.WHITE);
            loadingText.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(255,255,255,0.8), 5, 0, 0, 0);");
        }
        
        // Estilo de la barra de progreso
        if (loadingBar != null) {
            loadingBar.setStyle(
                "-fx-accent: #00ff00; " +
                "-fx-background-color: #333333; " +
                "-fx-border-color: #ffffff; " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 5; " +
                "-fx-background-radius: 5; " +
                "-fx-pref-width: 400; " +
                "-fx-pref-height: 20;"
            );
        }
    }
    
    /**
     * Inicia la animación de carga completa con barra de progreso y texto parpadeante
     * @param onFinished Callback que se ejecuta cuando termina la animación
     */
    public void startLoadingAnimation(Runnable onFinished) {
        // Detener animaciones previas si existen
        stopAllAnimations();
        
        // Reiniciar la barra de progreso
        loadingBar.setProgress(0);
        
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
     * @param message Mensaje a mostrar
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
     * @param message Mensaje a mostrar
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