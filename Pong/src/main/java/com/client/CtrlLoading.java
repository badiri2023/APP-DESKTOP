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
            // startLoadingAnimation();
            
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
    
    // Este método debe llamarse explícitamente cuando se necesite
    public void startLoadingAnimation(Runnable onFinished) {
        // Reiniciar la barra de progreso
        loadingBar.setProgress(0);
        
        // Animación del texto
        Timeline textAnimation = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(loadingText.opacityProperty(), 1.0)),
            new KeyFrame(Duration.seconds(0.5), new KeyValue(loadingText.opacityProperty(), 0.3)),
            new KeyFrame(Duration.seconds(1.0), new KeyValue(loadingText.opacityProperty(), 1.0))
        );
        textAnimation.setCycleCount(Timeline.INDEFINITE);
        textAnimation.play();
        
        // Animación de la barra de progreso
        loadingTimeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(loadingBar.progressProperty(), 0)),
            new KeyFrame(Duration.seconds(3), new KeyValue(loadingBar.progressProperty(), 1))
        );
        
        loadingTimeline.setOnFinished(event -> {
            textAnimation.stop(); // Detener la animación del texto
            if (onFinished != null) {
                onFinished.run(); // Ejecutar el callback proporcionado
            }
        });
        
        loadingTimeline.play();
    }
    
    public void setLoadingMessage(String message) {
        if (loadingText != null) {
            loadingText.setText(message);
        }
    }
    
    public void completeLoading() {
        if (loadingTimeline != null) {
            loadingTimeline.stop();
        }
        loadingBar.setProgress(1.0);
    }
    
    public void stopLoading() {
        if (loadingTimeline != null) {
            loadingTimeline.stop();
        }
    }
}