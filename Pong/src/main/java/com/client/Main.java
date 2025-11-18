package com.client;

import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONObject;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Main extends Application {

    public static CtrlLogin ctrlLogin;
    public static CtrlOpponentSelection ctrlOpponentSelection; 
    public static UtilsWS wsClient;

    // Variables para manejar invitaciones
    public static boolean invitationPending = false;
    public static String pendingOpponent = "";

    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage stage) throws Exception {
        try {
            final int windowWidth = 1200;
            final int windowHeight = 700;

            System.out.println("Sistema de configuración activado");

            UtilsViews.parentContainer.setStyle("-fx-font: 14 arial;");
            
            // Cargar solo las vistas necesarias
            try {
                UtilsViews.addView(getClass(), "ViewLogin", "/assets/viewLogin.fxml");
                ctrlLogin = (CtrlLogin) UtilsViews.getController("ViewLogin");
                System.out.println("✅ Vista Login cargada correctamente");
            } catch (Exception e) {
                System.err.println("❌ Error cargando ViewLogin: " + e.getMessage());
                showErrorAndExit("No se pudo cargar la vista de login");
                return;
            }
            
            try {
                UtilsViews.addView(getClass(), "ViewOpponentSelection", "/assets/viewOpponentSelection.fxml");
                ctrlOpponentSelection = (CtrlOpponentSelection) UtilsViews.getController("ViewOpponentSelection");
                System.out.println("✅ Vista OpponentSelection cargada correctamente");
            } catch (Exception e) {
                System.err.println("❌ Error cargando ViewOpponentSelection: " + e.getMessage());
                System.err.println("La funcionalidad de selección de oponente no estará disponible");
            }

            try {
                UtilsViews.addView(getClass(), "ViewLoading", "/assets/viewLoading.fxml");
                System.out.println("✅ Vista Loading cargada correctamente");
            } catch (Exception e) {
                System.err.println("❌ Error cargando ViewLoading: " + e.getMessage());
            }

            try {
                UtilsViews.addView(getClass(), "ViewGame", "/assets/viewGame.fxml");
                System.out.println("✅ Vista Game cargada - Controlador: " + 
                    (UtilsViews.getController("ViewGame") != null ? "OK" : "NULL"));
            } catch (Exception e) {
                System.err.println("❌ Error cargando ViewGame: " + e.getMessage());
                e.printStackTrace(); // ← Esto te dirá exactamente qué falla
            }

            try {
                UtilsViews.addView(getClass(), "ViewGameOver", "/assets/viewGameOver.fxml");
                System.out.println("✅ Vista GameOver cargada correctamente");
            } catch (Exception e) {
                System.err.println("❌ Error cargando ViewGameOver: " + e.getMessage());
            }

            Scene scene = new Scene(UtilsViews.parentContainer, windowWidth, windowHeight);

            UtilsViews.setStage(stage);
            stage.setScene(scene);
            stage.setTitle("Pong");
            stage.setMinWidth(windowWidth);
            stage.setMinHeight(windowHeight);
            
            try {
                Image icon = new Image(getClass().getResourceAsStream("/assets/icon.png"));
                stage.getIcons().add(icon);
            } catch (Exception e) {
                System.err.println("No se pudo cargar el icono: " + e.getMessage());
            }

            stage.show();

        } catch (Exception e) {
            System.err.println("Error no controlado en start: " + e.getMessage());
            e.printStackTrace();
            showErrorAndExit("Error crítico al iniciar la aplicación");
        }
    }

    private void showErrorAndExit(String message) {
        showAlert("Error Crítico", message + "\nLa aplicación se cerrará.", AlertType.ERROR);
        Platform.exit();
    }

    @Override
    public void stop() { 
        if (wsClient != null) {
            wsClient.forceExit();
        }
        System.exit(0);
    }
    
    public static void pauseDuring(long milliseconds, Runnable action) {
        PauseTransition pause = new PauseTransition(Duration.millis(milliseconds));
        pause.setOnFinished(event -> Platform.runLater(action));
        pause.play();
    }

    public static void connectToServer(){
        pauseDuring(1500, () -> {
            String url = ctrlLogin.getUrl();
            if (url == null || url.isEmpty()) {
                System.err.println("URL del servidor no válida");
                return;
            }
            
            wsClient = UtilsWS.getSharedInstance(url);

            wsClient.onMessage((response) -> { 
                Platform.runLater(() -> { 
                    wsMessage(response); 
                }); 
            });
            
            // Esperar a que la conexión se establezca
            pauseDuring(2000, () -> {
                if (wsClient != null && wsClient.isOpen()) {
                    // ✅ CORREGIDO: Enviar NICKNAME: en lugar de JSON
                    String nicknameMessage = "NICKNAME:" + ctrlLogin.getUserName().trim();
                    wsClient.safeSend(nicknameMessage);
                    System.out.println("Enviando nombre de usuario (NICKNAME): " + ctrlLogin.getUserName().trim());
                    
                    // Cambiar a vista de selección de oponente después del registro
                    pauseDuring(1000, () -> {
                        if (ctrlOpponentSelection != null) {
                            UtilsViews.setViewAnimating("ViewOpponentSelection");
                            requestPlayersList();
                        } else {
                            System.err.println("Vista de selección de oponente no disponible");
                        }
                    });
                } else {
                    System.err.println("No se pudo establecer conexión WebSocket");
                    Platform.runLater(() -> {
                        showAlert("Error de Conexión", "No se pudo conectar al servidor: " + url, AlertType.ERROR);
                    });
                }
            });
        });
    }
    
    public static void requestPlayersList() {
        try {
            // Esperar a que el WebSocket esté conectado
            if (wsClient == null || !wsClient.isOpen()) {
                System.err.println("WebSocket no conectado, no se puede solicitar lista de jugadores");
                return;
            }
            
            JSONObject request = new JSONObject();
            request.put("type", "getPlayers");
            wsClient.safeSend(request.toString());
            System.out.println("Solicitando lista de jugadores...");
        } catch (Exception e) {
            System.err.println("Error solicitando lista de jugadores: " + e.getMessage());
        }
    }
    
    // En el método wsMessage, agregar manejo para las respuestas del registro:

    private static void wsMessage(String response) {
        try {
            System.out.println("Mensaje recibido del servidor: " + response);
            
            // Manejar respuestas de texto plano del registro
            if ("ACCEPTED".equals(response)) {
                Platform.runLater(() -> {
                    System.out.println("✅ Registro aceptado por el servidor");
                    requestPlayersList();
                });
                return;
            }
            
            if ("REJECTED".equals(response)) {
                Platform.runLater(() -> {
                    System.err.println("❌ Registro rechazado por el servidor");
                    Main.showAlert("Registro Rechazado", "El nombre de usuario ya está en uso o es inválido", AlertType.ERROR);
                });
                return;
            }
            
            // Si no es texto plano, intentar procesar como JSON
            JSONObject json = new JSONObject(response);
            String type = json.optString("type", "");
            
            switch (type) {
                case "welcome":
                    String welcomeMsg = json.optString("message", "¡Bienvenido al servidor PONG!");
                    Platform.runLater(() -> {
                        Main.showAlert("Bienvenida", welcomeMsg, AlertType.INFORMATION);
                    });
                    break;
                    
                case "clients":
                case "playersList":
                    if (ctrlOpponentSelection != null) {
                        JSONArray playersArray;
                        if (json.has("list")) {
                            playersArray = json.getJSONArray("list");
                        } else if (json.has("players")) {
                            playersArray = json.getJSONArray("players");
                        } else {
                            playersArray = new JSONArray();
                        }
                        ctrlOpponentSelection.updatePlayersList(playersArray);
                    }
                    break;
                    
                // El servidor envía "challenge_received" pero el cliente esperaba otro nombre
                case "challenge_received":
                    String fromPlayer = json.optString("from", "");
                    System.out.println("INVITACIÓN RECIBIDA DE: " + fromPlayer);
                    Platform.runLater(() -> {
                        showIncomingInvitationDialog(fromPlayer);
                    });
                    break;
                    
                case "challenge_declined":
                    String decliner = json.optString("from", "");
                    Platform.runLater(() -> {
                        Main.showAlert("Invitación Rechazada", decliner + " rechazó tu invitación", AlertType.INFORMATION);
                        // Limpiar estado de invitación pendiente
                        CtrlOpponentSelection.clearInvitation();
                        // Actualizar lista
                        requestPlayersList();
                    });
                    break;

                // JUEGO - FLUJO PRINCIPAL
                case "game_start":
                    String opponent = json.optString("opponent", "");
                    String role = json.optString("role", "");
                    Platform.runLater(() -> {
                        System.out.println("Iniciando partida - Rol: " + role + ", Oponente: " + opponent);
                        // Limpiar estado de invitación pendiente
                        CtrlOpponentSelection.clearInvitation();
                        // Cambiar directamente a ViewGame
                        UtilsViews.setViewAnimating("ViewGame");
                        CtrlGame ctrlGame = (CtrlGame) UtilsViews.getController("ViewGame");
                        if (ctrlGame != null) {
                            ctrlGame.setPlayerRole(role, opponent);
                            ctrlGame.startGameSequence();
                        }
                    });
                    break;
                    
                case "choosing_starter":
                    Platform.runLater(() -> {
                        CtrlGame ctrlGame = (CtrlGame) UtilsViews.getController("ViewGame");
                        if (ctrlGame != null && "ViewGame".equals(UtilsViews.getActiveView())) {
                            ctrlGame.showChoosingStarter();
                        }
                    });
                    break;
                    
                case "text":
                    String textMessage = json.optString("message", "");
                    long ttlMs = json.optLong("ttl_ms", 5000);
                    
                    if (textMessage.contains("Starts Player")) {
                        Platform.runLater(() -> {
                            CtrlGame ctrlGame = (CtrlGame) UtilsViews.getController("ViewGame");
                            if (ctrlGame != null && "ViewGame".equals(UtilsViews.getActiveView())) {
                                ctrlGame.showStarterAnnouncement(textMessage, ttlMs);
                            }
                        });
                    } else {
                        // Otros mensajes de texto
                        if (!textMessage.isEmpty()) {
                            Platform.runLater(() -> {
                                Main.showAlert("Mensaje del Servidor", textMessage, AlertType.INFORMATION, ttlMs);
                            });
                        }
                    }
                    break;
                    
                case "countdown":
                    String countdownValue = json.optString("value", "3");
                    Platform.runLater(() -> {
                        CtrlGame ctrlGame = (CtrlGame) UtilsViews.getController("ViewGame");
                        if (ctrlGame != null && "ViewGame".equals(UtilsViews.getActiveView())) {
                            ctrlGame.handleCountdown(countdownValue);
                        }
                    });
                    break;
                    
                case "game_state":
                    // Actualizar estado del juego en tiempo real
                    double p1Y = json.optDouble("p1_y", 0.5);
                    double p2Y = json.optDouble("p2_y", 0.5);
                    double ballX = json.optDouble("ball_x", 0.5);
                    double ballY = json.optDouble("ball_y", 0.5);
                    int score1 = json.optInt("score1", 0);
                    int score2 = json.optInt("score2", 0);
                    
                    Platform.runLater(() -> {
                        CtrlGame ctrlGame = (CtrlGame) UtilsViews.getController("ViewGame");
                        if (ctrlGame != null && "ViewGame".equals(UtilsViews.getActiveView())) {
                            ctrlGame.updateGameState(p1Y, p2Y, ballX, ballY, score1, score2);
                        }
                    });
                    break;
                    
                case "game_over":
                    String winner = json.optString("winner", "");
                    String reason = json.optString("reason", "");
                    int finalScore1 = json.optInt("score1", 0);
                    int finalScore2 = json.optInt("score2", 0);
                    
                    Platform.runLater(() -> {
                        CtrlGame ctrlGame = (CtrlGame) UtilsViews.getController("ViewGame");
                        if (ctrlGame != null) {
                            if (!reason.isEmpty()) {
                                Main.showAlert("Partida Terminada", reason, AlertType.INFORMATION);
                            }
                            ctrlGame.handleGameOver(winner, finalScore1, finalScore2);
                        }
                    });
                    break;
                    
                default:
                    System.out.println("Mensaje no manejado - Tipo: " + type);
            }
            
        } catch (Exception e) {
            System.err.println("Error procesando mensaje: " + e.getMessage());
        }
    }

    //  Mostrar diálogo de invitación entrante
    private static void showIncomingInvitationDialog(String fromPlayer) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Invitación de Partida");
        alert.setHeaderText("¡Invitación recibida!");
        alert.setContentText("¿Aceptas jugar contra " + fromPlayer + "?\n\nLa partida comenzará inmediatamente después de aceptar.");
        
        applyAlertStyle(alert);
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Aceptar invitación
                acceptIncomingInvitation(fromPlayer);
            } else {
                // Rechazar invitación
                rejectIncomingInvitation(fromPlayer);
            }
        });
    }

    //  Aceptar invitación entrante
    private static void acceptIncomingInvitation(String fromPlayer) {
        try {
            JSONObject response = new JSONObject();
            response.put("type", "challenge_response");
            response.put("to", fromPlayer); 
            response.put("accepted", true);
            
            System.out.println("📤 Enviando respuesta de invitación: " + response.toString());
            
            if (wsClient != null && wsClient.isOpen()) {
                wsClient.safeSend(response.toString());
                System.out.println("✅ Invitación aceptada - Enviando respuesta al servidor");
            }
        } catch (Exception e) {
            System.err.println("Error aceptando invitación: " + e.getMessage());
            Main.showAlert("Error", "No se pudo aceptar la invitación: " + e.getMessage(), AlertType.ERROR);
        }
    }

    // ✅ NUEVO MÉTODO: Rechazar invitación entrante
    private static void rejectIncomingInvitation(String fromPlayer) {
        try {
            JSONObject response = new JSONObject();
            response.put("type", "challenge_response");
            response.put("to", fromPlayer);
            response.put("accepted", false);
            
            if (wsClient != null && wsClient.isOpen()) {
                wsClient.safeSend(response.toString());
                System.out.println("❌ Invitación rechazada - Enviando respuesta al servidor");
            }
        } catch (Exception e) {
            System.err.println("Error rechazando invitación: " + e.getMessage());
        }
    }

    /**
     * Maneja mensajes de texto del servidor con TTL (Time To Live)
     */
    private static void handleTextMessage(JSONObject json) {
        String message = json.optString("message", "");
        long ttlMs = json.optLong("ttl_ms", 5000); // Default 5 segundos
        
        if (!message.isEmpty()) {
            Platform.runLater(() -> {
                // Usar el método showAlert mejorado con auto-cierre
                showAlert("Mensaje del Servidor", message, Alert.AlertType.INFORMATION, ttlMs);
            });
        }
    }

    // En Main.java, cambiar la visibilidad de los métodos:

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
     * Aplica estilo retro a un Alert con texto BLANCO
     */
    public static void applyAlertStyle(Alert alert) {
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
            if (button != null && ctrlLogin != null && ctrlLogin.retroFont != null) {
                button.setFont(Font.font(ctrlLogin.retroFont.getFamily(), FontWeight.BOLD, 12));
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
    
    // Métodos estáticos para manejar invitaciones
    public static void acceptInvitation(String fromPlayer) {
        try {
            JSONObject response = new JSONObject();
            response.put("type", "invitationResponse");
            response.put("to", fromPlayer);
            response.put("from", ctrlLogin.getUserName());
            response.put("accepted", true);
            
            if (wsClient != null && wsClient.isOpen()) {
                wsClient.safeSend(response.toString());
            }
        } catch (Exception e) {
            System.err.println("Error aceptando invitación: " + e.getMessage());
        }
    }
    
    public static void rejectInvitation(String fromPlayer) {
        try {
            JSONObject response = new JSONObject();
            response.put("type", "invitationResponse");
            response.put("to", fromPlayer);
            response.put("from", ctrlLogin.getUserName());
            response.put("accepted", false);
            
            if (wsClient != null && wsClient.isOpen()) {
                wsClient.safeSend(response.toString());
            }
        } catch (Exception e) {
            System.err.println("Error rechazando invitación: " + e.getMessage());
        }
    }
    
    public static void startInvitationTimeout(String opponentName) {
        new Thread(() -> {
            try {
                Thread.sleep(30000);
                
                Platform.runLater(() -> {
                    if (invitationPending && pendingOpponent.equals(opponentName)) {
                        invitationPending = false;
                        pendingOpponent = "";
                        
                        if (ctrlOpponentSelection != null) {
                            ctrlOpponentSelection.updateStatus("Invitación expirada. Selecciona otro jugador.");
                            requestPlayersList();
                        }
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
    
    public static void clearInvitationState() {
        invitationPending = false;
        pendingOpponent = "";
    }
}