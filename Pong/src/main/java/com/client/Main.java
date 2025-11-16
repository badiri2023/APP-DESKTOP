package com.client;

import java.util.Arrays;

import org.json.JSONObject;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
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
            final int windowHeight = 650;

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
                System.out.println("✅ Vista Game cargada correctamente");
            } catch (Exception e) {
                System.err.println("❌ Error cargando ViewGame: " + e.getMessage());
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
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error Crítico");
        alert.setHeaderText(null);
        alert.setContentText(message + "\nLa aplicación se cerrará.");
        alert.showAndWait();
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
            wsClient = UtilsWS.getSharedInstance(ctrlLogin.getUrl());

            wsClient.onMessage((response) -> { 
                Platform.runLater(() -> { 
                    wsMessage(response); 
                }); 
            });
            
            pauseDuring(2000, () -> {
                if (wsClient != null && wsClient.isOpen()) {
                    JSONObject userInfo = new JSONObject();
                    userInfo.put("type", "userInfo");
                    userInfo.put("userName", ctrlLogin.getUserName().trim());
                    wsClient.safeSend(userInfo.toString());
                    System.out.println("Enviando nombre de usuario: " + ctrlLogin.getUserName().trim());
                    
                    // Cambiar DIRECTAMENTE a vista de selección de oponente
                    pauseDuring(1000, () -> {
                        if (ctrlOpponentSelection != null) {
                            UtilsViews.setViewAnimating("ViewOpponentSelection");
                            requestPlayersList();
                        } else {
                            System.out.println("Vista de selección de oponente no disponible");
                        }
                    });
                } 
            });
        });
    }
    
    public static void requestPlayersList() {
        try {
            JSONObject request = new JSONObject();
            request.put("type", "getPlayers");
            if (wsClient != null && wsClient.isOpen()) {
                wsClient.safeSend(request.toString());
                System.out.println("Solicitando lista de jugadores...");
            }
        } catch (Exception e) {
            System.err.println("Error solicitando lista de jugadores: " + e.getMessage());
        }
    }
    
    private static void wsMessage(String response) {
        try {
            System.out.println("Mensaje recibido del servidor: " + response);
            JSONObject json = new JSONObject(response);
            String type = json.optString("type", "");
            
            switch (type) {
                case "welcome":
                    // ✅ MOSTRAR ALERT DE BIENVENIDA
                    String welcomeMsg = json.optString("message", "¡Bienvenido al servidor PONG!");
                    Platform.runLater(() -> {
                        showAlert("Bienvenida", welcomeMsg);
                    });
                    break;
                    
                case "userRegistered":
                    // ✅ MOSTRAR ALERT DE REGISTRO EXITOSO
                    String regMsg = json.optString("message", "Registro exitoso");
                    Platform.runLater(() -> {
                        showAlert("Registro Exitoso", regMsg);
                    });
                    break;
                    
                case "playersList":
                    if (ctrlOpponentSelection != null && json.has("players")) {
                        java.util.List<Object> playersList = json.getJSONArray("players").toList();
                        String[] players = playersList.toArray(new String[0]);
                        ctrlOpponentSelection.updatePlayersList(players);
                        
                        // ✅ MOSTRAR ALERT CON LISTA ACTUALIZADA
                        String listMsg = json.optString("message", "Lista de jugadores actualizada");
                        Platform.runLater(() -> {
                            showAlert("Jugadores Conectados", listMsg + "\nJugadores: " + Arrays.toString(players));
                        });
                    }
                    break;
                    
                case "clientInvite":
                    String fromPlayer = json.optString("from", "");
                    String inviteMsg = json.optString("message", "Invitación recibida");
                    if (!fromPlayer.isEmpty() && ctrlOpponentSelection != null) {
                        ctrlOpponentSelection.handleIncomingInvitation(fromPlayer);
                        
                        // ✅ EL ALERT DE INVITACIÓN YA SE MANEJA EN handleIncomingInvitation
                    }
                    break;
                    
                case "invitationResponse":
                    boolean accepted = json.optBoolean("accepted", false);
                    String responder = json.optString("from", "");
                    String responseMsg = json.optString("message", "Respuesta a invitación");
                    
                    if (accepted) {
                        Platform.runLater(() -> {
                            showAlert("Invitación Aceptada", responseMsg);
                        });
                    } else {
                        Platform.runLater(() -> {
                            showAlert("Invitación Rechazada", responseMsg);
                        });
                    }
                    break;
                    
                case "gameStart":
                    String gameMsg = json.optString("message", "Partida iniciada");
                    Platform.runLater(() -> {
                        showAlert("¡Partida Iniciada!", gameMsg);
                    });
                    break;
                    
                case "error":
                    String errorMsg = json.optString("message", "Error del servidor");
                    Platform.runLater(() -> {
                        showAlert("Error", errorMsg);
                    });
                    break;
                    
                default:
                    System.out.println("Mensaje no manejado - Tipo: " + type);
            }
            
        } catch (Exception e) {
            System.err.println("Error procesando mensaje: " + e.getMessage());
        }
    }

    // Método auxiliar para mostrar alerts
    private static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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