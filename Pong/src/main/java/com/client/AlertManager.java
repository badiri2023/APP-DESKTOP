package com.client;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

/**
 * Gestor centralizado de alertas para la aplicación Pong
 * Proporciona métodos estáticos para mostrar alertas con estilo retro
 */
public class AlertManager {

    /**
     * Muestra un Alert con estilo retro (versión bloqueante)
     */
    public static void showAlert(String title, String message, AlertType type) {
        showAlert(title, message, type, 0); // Por defecto sin auto-cierre
    }

    /**
     * Muestra un Alert con estilo retro y auto-cierre opcional
     */
    public static void showAlert(String title, String message, AlertType type, long autoCloseMs) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            applyAlertStyle(alert);
            
            if (autoCloseMs > 0) {
                // Mostrar sin bloquear y cerrar automáticamente
                alert.show();
                PauseTransition delay = new PauseTransition(Duration.millis(autoCloseMs));
                delay.setOnFinished(event -> {
                    if (alert.isShowing()) {
                        alert.close();
                    }
                });
                delay.play();
            } else {
                // Mostrar de forma bloqueante (comportamiento original)
                alert.showAndWait();
            }
        });
    }

    /**
     * Muestra un diálogo de confirmación con estilo retro
     */
    public static void showConfirmationDialog(String title, String message, Runnable onConfirm, Runnable onCancel) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            applyAlertStyle(alert);
            
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    if (onConfirm != null) onConfirm.run();
                } else {
                    if (onCancel != null) onCancel.run();
                }
            });
        });
    }

    /**
     * Muestra un diálogo de invitación entrante
     */
    public static void showIncomingInvitationDialog(String fromPlayer, Runnable onAccept, Runnable onReject) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Invitación de Partida");
            alert.setHeaderText("¡Invitación recibida!");
            alert.setContentText("¿Aceptas jugar contra " + fromPlayer + "?\n\nLa partida comenzará inmediatamente después de aceptar.");
            
            applyAlertStyle(alert);
            
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    if (onAccept != null) onAccept.run();
                } else {
                    if (onReject != null) onReject.run();
                }
            });
        });
    }

    /**
     * Muestra un error crítico y cierra la aplicación
     */
    public static void showErrorAndExit(String message) {
        showAlert("Error Crítico", message + "\nLa aplicación se cerrará.", AlertType.ERROR);
        Platform.exit();
    }

    /**
     * Aplica estilo retro a un Alert
     */
    private static void applyAlertStyle(Alert alert) {
        // Estilo del panel principal del Alert
        alert.getDialogPane().setStyle(
            "-fx-background-color: #000000; " +
            "-fx-border-color: #ffffff; " +
            "-fx-border-width: 3; " +
            "-fx-border-radius: 5; " +
            "-fx-background-radius: 5;"
        );
        
        // Estilo del contenido (texto) a BLANCO
        Label contentLabel = (Label) alert.getDialogPane().lookup(".content.label");
        if (contentLabel != null) {
            contentLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 14px;");
        }
        
        // Estilo del header (si existe)
        Node header = alert.getDialogPane().lookup(".header-panel");
        if (header != null) {
            header.setStyle("-fx-background-color: #000000;");
            Label headerLabel = (Label) header.lookup(".label");
            if (headerLabel != null) {
                headerLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 16px; -fx-font-weight: bold;");
            }
        }
        
        // Aplicar estilo a los botones
        alert.getDialogPane().getButtonTypes().forEach(buttonType -> {
            Button button = (Button) alert.getDialogPane().lookupButton(buttonType);
            if (button != null) {
                // Intentar usar la fuente retro, fallback a Consolas
                Font retroFont = getRetroFont();
                button.setFont(Font.font(retroFont.getFamily(), FontWeight.BOLD, 12));
                button.setStyle(
                    "-fx-background-color: #ffffff; " +
                    "-fx-text-fill: #000000; " +
                    "-fx-background-radius: 3; " +
                    "-fx-border-radius: 3; " +
                    "-fx-border-color: #000000; " +
                    "-fx-border-width: 1;"
                );
                
                // Efecto hover para los botones
                button.setOnMouseEntered(e -> {
                    button.setStyle(
                        "-fx-background-color: #e6e6e6; " +
                        "-fx-text-fill: #000000; " +
                        "-fx-background-radius: 3; " +
                        "-fx-border-radius: 3; " +
                        "-fx-border-color: #000000; " +
                        "-fx-border-width: 1;"
                    );
                });
                
                button.setOnMouseExited(e -> {
                    button.setStyle(
                        "-fx-background-color: #ffffff; " +
                        "-fx-text-fill: #000000; " +
                        "-fx-background-radius: 3; " +
                        "-fx-border-radius: 3; " +
                        "-fx-border-color: #000000; " +
                        "-fx-border-width: 1;"
                    );
                });
            }
        });
    }

    /**
     * Obtiene la fuente retro (para uso interno)
     */
    private static Font getRetroFont() {
        try {
            Font retroFont = Font.loadFont(AlertManager.class.getResourceAsStream("/assets/fonts/BrunoAce-Regular.ttf"), 14);
            if (retroFont != null) {
                return retroFont;
            }
        } catch (Exception e) {
            System.err.println("No se pudo cargar la fuente retro: " + e.getMessage());
        }
        return Font.font("Consolas", 14);
    }
}