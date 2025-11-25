package com.client;

import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONObject;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
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
            
            // Cargar vistas
            loadViews();
            
            Scene scene = new Scene(UtilsViews.parentContainer, windowWidth, windowHeight);

            UtilsViews.setStage(stage);
            stage.setScene(scene);
            stage.setTitle("Pong");
            stage.setMinWidth(windowWidth);
            stage.setMinHeight(windowHeight);
            
            loadAppIcon(stage);
            stage.show();

        } catch (Exception e) {
            System.err.println("Error no controlado en start: " + e.getMessage());
            e.printStackTrace();
            AlertManager.showErrorAndExit("Error crítico al iniciar la aplicación");
        }
    }

    /**
     * Carga todas las vistas de la aplicación
     */
    private void loadViews() {
        String[] views = {
            "ViewLogin", "/assets/viewLogin.fxml",
            "ViewOpponentSelection", "/assets/viewOpponentSelection.fxml", 
            "ViewLoading", "/assets/viewLoading.fxml",
            "ViewGame", "/assets/viewGame.fxml",
            "ViewGameOver", "/assets/viewGameOver.fxml"
        };
        
        for (int i = 0; i < views.length; i += 2) {
            String viewName = views[i];
            String viewPath = views[i + 1];
            
            try {
                UtilsViews.addView(getClass(), viewName, viewPath);
                
                // Asignar controladores principales
                if ("ViewLogin".equals(viewName)) {
                    ctrlLogin = (CtrlLogin) UtilsViews.getController(viewName);
                } else if ("ViewOpponentSelection".equals(viewName)) {
                    ctrlOpponentSelection = (CtrlOpponentSelection) UtilsViews.getController(viewName);
                }
                
                System.out.println("Vista " + viewName + " cargada correctamente");
            } catch (Exception e) {
                System.err.println("Error cargando " + viewName + ": " + e.getMessage());
                if ("ViewLogin".equals(viewName)) {
                    AlertManager.showErrorAndExit("No se pudo cargar la vista de login");
                    return;
                }
            }
        }
    }

    /**
     * Carga el icono de la aplicación
     */
    private void loadAppIcon(Stage stage) {
        try {
            Image icon = new Image(getClass().getResourceAsStream("/assets/icon.png"));
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.err.println("No se pudo cargar el icono: " + e.getMessage());
        }
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
                        AlertManager.showAlert("Error de Conexión", "No se pudo conectar al servidor: " + url, AlertType.ERROR);
                    });
                }
            });
        });
    }
    
    public static void requestPlayersList() {
        try {
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

    /**
     * Procesa mensajes recibidos del servidor WebSocket
     */
    private static void wsMessage(String response) {
        try {
            System.out.println("Mensaje recibido del servidor: " + response);
            
            // Manejar respuestas de texto plano del registro
            if ("ACCEPTED".equals(response)) {
                Platform.runLater(() -> {
                    System.out.println("Registro aceptado por el servidor");
                    requestPlayersList();
                });
                return;
            }
            
            if ("REJECTED".equals(response)) {
                Platform.runLater(() -> {
                    System.err.println("Registro rechazado por el servidor");
                    AlertManager.showAlert("Registro Rechazado", "El nombre de usuario ya está en uso o es inválido", AlertType.ERROR);
                });
                return;
            }
            
            // Procesar mensaje JSON
            JSONObject json = new JSONObject(response);
            String type = json.optString("type", "");
            
            switch (type) {
                case "welcome":
                    handleWelcomeMessage(json);
                    break;
                    
                case "clients":
                case "playersList":
                    handlePlayersList(json);
                    break;
                    
                case "challenge_received":
                    handleChallengeReceived(json);
                    break;
                    
                case "challenge_declined":
                    handleChallengeDeclined(json);
                    break;

                // JUEGO - FLUJO PRINCIPAL
                case "game_start":
                    handleGameStart(json);
                    break;
                    
                case "choosing_starter":
                    handleChoosingStarter();
                    break;
                    
                case "text":
                    handleTextMessage(json);
                    break;
                    
                case "countdown":
                    handleCountdown(json);
                    break;
                    
                case "game_state":
                    handleGameState(json);
                    break;
                    
                case "game_over":
                    handleGameOver(json);
                    break;
                    
                case "player_disconnected":
                    handlePlayerDisconnected(json);
                    break;

                case "rematch_request":
                    handleRematchRequest(json);
                    break;
                    
                case "rematch_accepted":
                    handleRematchAccepted(json);
                    break;
                    
                case "rematch_declined":
                    handleRematchDeclined(json);
                    break;
                    
                case "rematch_start":
                    handleRematchStart(json);
                    break;
                    
                default:
                    System.out.println("Mensaje no manejado - Tipo: " + type);
            }
            
        } catch (Exception e) {
            System.err.println("Error procesando mensaje: " + e.getMessage());
        }
    }

    // ========== HANDLERS ESPECÍFICOS ==========

    private static void handleWelcomeMessage(JSONObject json) {
        String welcomeMsg = json.optString("message", "¡Bienvenido al servidor PONG!");
        Platform.runLater(() -> {
            AlertManager.showAlert("Bienvenida", welcomeMsg, AlertType.INFORMATION);
        });
    }

    private static void handlePlayersList(JSONObject json) {
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
    }

    private static void handleChallengeReceived(JSONObject json) {
        String fromPlayer = json.optString("from", "");
        System.out.println("INVITACIÓN RECIBIDA DE: " + fromPlayer);
        
        AlertManager.showIncomingInvitationDialog(fromPlayer,
            () -> acceptIncomingInvitation(fromPlayer),  // onAccept
            () -> rejectIncomingInvitation(fromPlayer)   // onReject
        );
    }

    private static void handleChallengeDeclined(JSONObject json) {
        String decliner = json.optString("from", "");
        Platform.runLater(() -> {
            UtilsViews.setViewAnimating("ViewOpponentSelection");
            AlertManager.showAlert("Invitación Rechazada", decliner + " rechazó tu invitación", AlertType.INFORMATION);
            CtrlOpponentSelection.clearInvitation();
            requestPlayersList();
        });
    }

    private static void handleGameStart(JSONObject json) {
        String opponent = json.optString("opponent", "");
        String role = json.optString("role", "");
        Platform.runLater(() -> {
            System.out.println("Iniciando partida - Rol: " + role + ", Oponente: " + opponent);
            CtrlOpponentSelection.clearInvitation();

            UtilsViews.setViewAnimating("ViewLoading");
            
            CtrlLoading ctrlLoading = (CtrlLoading) UtilsViews.getController("ViewLoading");
            if (ctrlLoading != null) {
                ctrlLoading.setLoadingMessage("CARGANDO PARTIDA...");
                
                // MODIFICADO: Iniciar animación de carga PERO NO TERMINAR AUTOMÁTICAMENTE
                // Ahora esperaremos mensajes del servidor para continuar
                ctrlLoading.startIndeterminateLoading();
                
                System.out.println("ViewLoading iniciada - Esperando mensajes del servidor...");
            } else {
                System.err.println("CtrlLoading no disponible");
            }
            
            // NUEVO: Guardar información para usar después
            pendingGameInfo = new GameInfo(opponent, role);
        });
    }

    private static class GameInfo {
        String opponent;
        String role;
        
        GameInfo(String opponent, String role) {
            this.opponent = opponent;
            this.role = role;
        }
    }

    private static GameInfo pendingGameInfo = null;

    private static void handleChoosingStarter() {
        Platform.runLater(() -> {
            CtrlGame ctrlGame = (CtrlGame) UtilsViews.getController("ViewGame");
            if (ctrlGame != null && "ViewGame".equals(UtilsViews.getActiveView())) {
                ctrlGame.showChoosingStarter();
            }
        });
    }

    private static void handleTextMessage(JSONObject json) {
        String textMessage = json.optString("message", "");
        long ttlMs = json.optLong("ttl_ms", 5000);
        
        if (textMessage.contains("Starts Player")) {
            Platform.runLater(() -> {
                // MEJORADO: Mostrar en ViewLoading y luego continuar al juego
                CtrlLoading ctrlLoading = (CtrlLoading) UtilsViews.getController("ViewLoading");
                if (ctrlLoading != null) {
                    ctrlLoading.showTemporaryMessage(textMessage, ttlMs);
                    System.out.println("🎯 Mensaje 'Starts Player' mostrado en ViewLoading: " + textMessage);
                    
                    // NUEVO: Esperar a que termine el mensaje y luego ir al juego
                    Main.pauseDuring(ttlMs + 500, () -> {
                        completeLoadingAndStartGame();
                    });
                } else {
                    System.err.println("CtrlLoading no disponible para mostrar mensaje");
                    // Fallback: ir directamente al juego
                    completeLoadingAndStartGame();
                }
            });
        } else if (!textMessage.isEmpty()) {
            Platform.runLater(() -> {
                AlertManager.showAlert("Mensaje del Servidor", textMessage, AlertType.INFORMATION, ttlMs);
            });
        }
    }

    // NUEVO: Método para completar la carga e iniciar el juego
    private static void completeLoadingAndStartGame() {
        Platform.runLater(() -> {
            CtrlLoading ctrlLoading = (CtrlLoading) UtilsViews.getController("ViewLoading");
            if (ctrlLoading != null && pendingGameInfo != null) {
                ctrlLoading.completeLoadingAndGoToGame(() -> {
                    // Ir a ViewGame después de completar la animación
                    UtilsViews.setViewAnimating("ViewGame");
                    CtrlGame ctrlGame = (CtrlGame) UtilsViews.getController("ViewGame");
                    if (ctrlGame != null) {
                        ctrlGame.setPlayerRole(pendingGameInfo.role, pendingGameInfo.opponent);
                        ctrlGame.startGameSequence();
                        System.out.println("🎮 Juego iniciado después de carga extendida");
                    }
                    pendingGameInfo = null;
                });
            } else {
                // Fallback: ir directamente al juego
                if (pendingGameInfo != null) {
                    UtilsViews.setViewAnimating("ViewGame");
                    CtrlGame ctrlGame = (CtrlGame) UtilsViews.getController("ViewGame");
                    if (ctrlGame != null) {
                        ctrlGame.setPlayerRole(pendingGameInfo.role, pendingGameInfo.opponent);
                        ctrlGame.startGameSequence();
                    }
                    pendingGameInfo = null;
                }
            }
        });
    }

    private static void handleCountdown(JSONObject json) {
        String countdownValue = json.optString("value", "3");
        
        // NUEVO: Si estamos en ViewLoading y llega el countdown, es hora de ir al juego
        if ("3".equals(countdownValue) && pendingGameInfo != null) {
            System.out.println("⏰ Countdown 3 recibido - Completando carga...");
            completeLoadingAndStartGame();
            return;
        }
        
        Platform.runLater(() -> {
            CtrlGame ctrlGame = (CtrlGame) UtilsViews.getController("ViewGame");
            if (ctrlGame != null && "ViewGame".equals(UtilsViews.getActiveView())) {
                ctrlGame.handleCountdown(countdownValue);
            }
        });
    }

    private static void handleGameState(JSONObject json) {
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
    }

    private static void handleGameOver(JSONObject json) {
        String winner = json.optString("winner", "");
        String reason = json.optString("reason", "");
        int finalScore1 = json.optInt("score1", 0);
        int finalScore2 = json.optInt("score2", 0);
        
        Platform.runLater(() -> {
            CtrlGame ctrlGame = (CtrlGame) UtilsViews.getController("ViewGame");
            if (ctrlGame != null) {
                if (!reason.isEmpty()) {
                    AlertManager.showAlert("Partida Terminada", reason, AlertType.INFORMATION);
                }
                ctrlGame.handleGameOver(winner, finalScore1, finalScore2);
            }
        });
    }

    private static void handlePlayerDisconnected(JSONObject json) {
        String disconnectedPlayer = json.optString("disconnected_player", "");
        String disconnectMessage = json.optString("message", "");
        
        Platform.runLater(() -> {
            System.out.println("🔌 Jugador desconectado: " + disconnectedPlayer);
            AlertManager.showAlert("Jugador Desconectado", 
                disconnectedPlayer + " se ha desconectado. Volviendo al lobby...", 
                AlertType.INFORMATION, 3000);
            UtilsViews.setViewAnimating("ViewOpponentSelection");
            if (ctrlOpponentSelection != null) {
                requestPlayersList();
            }
        });
    }

    private static void handleRematchRequest(JSONObject json) {
        String fromPlayer = json.optString("from", "");
        System.out.println("Solicitud de revancha recibida de: " + fromPlayer);
        
        Platform.runLater(() -> {
            AlertManager.showConfirmationDialog(
                "Solicitud de Revancha",
                "¿Aceptas la revancha contra " + fromPlayer + "?",
                () -> acceptRematch(fromPlayer),  // onConfirm
                () -> declineRematch(fromPlayer)  // onCancel
            );
        });
    }

    private static void handleRematchAccepted(JSONObject json) {
        String opponent = json.optString("opponent", "");
        System.out.println("Revancha aceptada por: " + opponent);
        
        Platform.runLater(() -> {
            CtrlGameOver ctrlGameOver = (CtrlGameOver) UtilsViews.getController("ViewGameOver");
            if (ctrlGameOver != null && "ViewGameOver".equals(UtilsViews.getActiveView())) {
                ctrlGameOver.handleRematchResponse(true);
            }
        });
    }

    private static void handleRematchDeclined(JSONObject json) {
        String opponent = json.optString("opponent", "");
        System.out.println("Revancha rechazada por: " + opponent);
        
        Platform.runLater(() -> {
            CtrlGameOver ctrlGameOver = (CtrlGameOver) UtilsViews.getController("ViewGameOver");
            if (ctrlGameOver != null && "ViewGameOver".equals(UtilsViews.getActiveView())) {
                ctrlGameOver.handleRematchResponse(false);
            }
            
            // Mostrar alerta informativa
            AlertManager.showAlert("Revancha Rechazada", 
                opponent + " ha rechazado la solicitud de revancha.", 
                AlertType.INFORMATION);
        });
    }

    private static void handleRematchStart(JSONObject json) {
        String opponent = json.optString("opponent", "");
        String role = json.optString("role", "");
        
        System.out.println("Iniciando revancha - Rol: " + role + ", Oponente: " + opponent);
        
        Platform.runLater(() -> {
            UtilsViews.setViewAnimating("ViewLoading");
            
            CtrlLoading ctrlLoading = (CtrlLoading) UtilsViews.getController("ViewLoading");
            if (ctrlLoading != null) {
                ctrlLoading.setLoadingMessage("CARGANDO REVANCHA...");
                ctrlLoading.startIndeterminateLoading();
            }
            
            // Guardar información para usar después
            pendingGameInfo = new GameInfo(opponent, role);
        });
    }

    private static void acceptRematch(String fromPlayer) {
        try {
            JSONObject response = new JSONObject();
            response.put("type", "rematch_response");
            response.put("to", fromPlayer);
            response.put("accepted", true);
            
            Main.wsClient.safeSend(response.toString());
            System.out.println("Revancha aceptada - Enviando respuesta al servidor");
            
        } catch (Exception e) {
            System.err.println("Error aceptando revancha: " + e.getMessage());
        }
    }

    private static void declineRematch(String fromPlayer) {
        try {
            JSONObject response = new JSONObject();
            response.put("type", "rematch_response");
            response.put("to", fromPlayer);
            response.put("accepted", false);
            
            Main.wsClient.safeSend(response.toString());
            System.out.println("Revancha rechazada - Enviando respuesta al servidor");
            
        } catch (Exception e) {
            System.err.println("Error rechazando revancha: " + e.getMessage());
        }
    }

    // ========== MÉTODOS DE INVITACIÓN ==========

    private static void acceptIncomingInvitation(String fromPlayer) {
        try {
            JSONObject response = new JSONObject();
            response.put("type", "challenge_response");
            response.put("to", fromPlayer); 
            response.put("accepted", true);
            
            System.out.println("Enviando respuesta de invitación: " + response.toString());
            
            if (wsClient != null && wsClient.isOpen()) {
                wsClient.safeSend(response.toString());
                System.out.println("Invitación aceptada - Enviando respuesta al servidor");
            }
        } catch (Exception e) {
            System.err.println("Error aceptando invitación: " + e.getMessage());
            AlertManager.showAlert("Error", "No se pudo aceptar la invitación: " + e.getMessage(), AlertType.ERROR);
        }
    }

    private static void rejectIncomingInvitation(String fromPlayer) {
        try {
            JSONObject response = new JSONObject();
            response.put("type", "challenge_response");
            response.put("to", fromPlayer);
            response.put("accepted", false);
            
            if (wsClient != null && wsClient.isOpen()) {
                wsClient.safeSend(response.toString());
                System.out.println("Invitación rechazada - Enviando respuesta al servidor");
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