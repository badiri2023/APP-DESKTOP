package com.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.net.URL;
import java.util.ResourceBundle;

import org.json.JSONArray;
import org.json.JSONObject;

public class CtrlOpponentSelection implements Initializable {

    @FXML
    private AnchorPane anchorPane;
    
    @FXML
    private VBox selectionPanel;
    
    @FXML
    private Label titleLabel;
    
    @FXML
    private Label lblStatus;
    
    @FXML
    private Label instructionLabel;

    @FXML
    private ListView<String> listPlayers;

    private Font retroFont;
    
    // Variables para manejar invitaciones
    public static boolean invitationPending = false;
    public static String pendingOpponent = "";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            // Cargar fuente retro
            try {
                retroFont = Font.loadFont(getClass().getResourceAsStream("/assets/fonts/8bitOperatorPlus8-Regular.ttf"), 14);
                System.out.println("✅ Fuente cargada en OpponentSelection: " + retroFont.getFamily());
            } catch (Exception e) {
                System.out.println("❌ No se pudo cargar fuente 8bitOperatorPlus8: " + e.getMessage());
                retroFont = Font.font("Consolas", 14);
            }
            
            // Aplicar estilos retro
            applyRetroStyles();
            
            // Configurar lista de jugadores
            setupPlayersList();
            
            // Inicializar estado
            lblStatus.setText("Conectando al servidor...");
            
            // Solicitar lista de jugadores al servidor
            requestPlayersList();
            
        } catch (Exception e) {
            System.err.println("Error en initialize de OpponentSelection: " + e.getMessage());
        }
    }
    
    private void applyRetroStyles() {
        // Fondo principal
        if (anchorPane != null) {
            anchorPane.setStyle("-fx-background-color: #000000;");
        }
        
        // Título
        if (titleLabel != null) {
            titleLabel.setFont(Font.font(retroFont.getFamily(), FontWeight.BOLD, 48));
            titleLabel.setTextFill(Color.WHITE);
            titleLabel.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(255,255,255,0.8), 10, 0, 0, 0);");
        }
        
        // Panel de selección
        if (selectionPanel != null) {
            selectionPanel.setStyle(
                "-fx-background-color: #1a1a1a; " +
                "-fx-background-radius: 8; " +
                "-fx-border-color: #ffffff; " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 8; " +
                "-fx-padding: 20;"
            );
        }
        
        // Labels
        if (lblStatus != null) {
            lblStatus.setFont(Font.font(retroFont.getFamily(), FontWeight.BOLD, 14));
            lblStatus.setTextFill(Color.WHITE);
        }
        
        if (instructionLabel != null) {
            instructionLabel.setFont(Font.font(retroFont.getFamily(), 12));
            instructionLabel.setTextFill(Color.rgb(102, 102, 102)); // #666666
        }
        
        // Estilo de la lista de jugadores
        if (listPlayers != null) {
            listPlayers.setStyle(
                "-fx-background-color: #000000; " +
                "-fx-text-fill: #ffffff; " +
                "-fx-border-color: #ffffff; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 3; " +
                "-fx-background-radius: 3; " +
                "-fx-control-inner-background: #000000; " +
                "-fx-selection-bar: #333333; " +
                "-fx-selection-bar-non-focused: #222222;"
            );
            
            // Aplicar estilo a las celdas
            listPlayers.setCellFactory(lv -> new javafx.scene.control.ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("-fx-background-color: #000000; -fx-text-fill: #ffffff;");
                    } else {
                        setText(item);
                        setFont(Font.font(retroFont.getFamily(), 14));
                        setTextFill(Color.WHITE);
                        setStyle("-fx-background-color: #000000; -fx-border-color: #333333; -fx-border-width: 0 0 1 0;");
                        
                        // Efecto hover
                        setOnMouseEntered(e -> {
                            if (!isEmpty()) {
                                setStyle("-fx-background-color: #333333; -fx-text-fill: #ffffff; -fx-border-color: #333333; -fx-border-width: 0 0 1 0;");
                            }
                        });
                        setOnMouseExited(e -> {
                            if (!isEmpty()) {
                                setStyle("-fx-background-color: #000000; -fx-text-fill: #ffffff; -fx-border-color: #333333; -fx-border-width: 0 0 1 0;");
                            }
                        });
                    }
                }
            });
        }
    }
    
    private void setupPlayersList() {
        listPlayers.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selectedPlayer = listPlayers.getSelectionModel().getSelectedItem();
                if (selectedPlayer != null && !selectedPlayer.equals(Main.ctrlLogin.getUserName())) {
                    sendInvitation(selectedPlayer);
                }
            }
        });
    }
    
    private void requestPlayersList() {
        try {
            JSONObject request = new JSONObject();
            request.put("type", "getPlayers");
            Main.wsClient.safeSend(request.toString());
        } catch (Exception e) {
            System.err.println("Error solicitando lista de jugadores: " + e.getMessage());
        }
    }
    
    // ✅ NUEVO MÉTODO para manejar JSONArray (como la Raspberry Pi)
    public void updatePlayersList(JSONArray playersArray) {
        Platform.runLater(() -> {
            System.out.println("Actualizando lista de jugadores desde JSONArray...");
            
            if (playersArray == null) {
                lblStatus.setText("Error: datos no disponibles");
                listPlayers.getItems().clear();
                return;
            }
            
            // Limpiar la lista actual
            listPlayers.getItems().clear();
            
            int availablePlayers = 0;
            String currentPlayer = Main.ctrlLogin.getUserName();
            
            // Procesar el JSONArray igual que en la Raspberry Pi
            for (int i = 0; i < playersArray.length(); i++) {
                try {
                    String player = playersArray.getString(i);
                    
                    // Solo mostrar jugadores que no sean yo mismo
                    if (player != null && !player.equals(currentPlayer) && !player.isEmpty()) {
                        listPlayers.getItems().add(player);
                        availablePlayers++;
                        System.out.println("Añadido jugador: " + player);
                    }
                } catch (Exception e) {
                    System.err.println("Error procesando jugador en índice " + i + ": " + e.getMessage());
                }
            }
            
            // Actualizar el mensaje de estado
            if (availablePlayers == 0) {
                lblStatus.setText("No hay otros jugadores conectados");
                lblStatus.setTextFill(Color.rgb(255, 100, 100));
            } else {
                lblStatus.setText(availablePlayers + " jugador(es) disponible(s). Haz doble click para invitar.");
                lblStatus.setTextFill(Color.WHITE);
            }
            
            System.out.println("Lista actualizada. Jugadores disponibles: " + availablePlayers);
        });
    }
    
    private void sendInvitation(String opponentName) {
        try {
            JSONObject invitation = new JSONObject();
            invitation.put("type", "clientInvite");
            invitation.put("opponent", opponentName);
            invitation.put("from", Main.ctrlLogin.getUserName());
            
            Main.wsClient.safeSend(invitation.toString());
            
            // Marcar invitación pendiente
            invitationPending = true;
            pendingOpponent = opponentName;
            
            // Iniciar timeout (30 segundos)
            startInvitationTimeout(opponentName);
            
            System.out.println("Invitación enviada a " + opponentName);
            
            // Mostrar confirmación con estilo retro
            showRetroAlert("Invitación Enviada", "Invitación enviada a " + opponentName + ". Esperando respuesta...", AlertType.INFORMATION);
            
        } catch (Exception e) {
            System.err.println("Error enviando invitación: " + e.getMessage());
            lblStatus.setText("Error al enviar invitación");
            lblStatus.setTextFill(Color.RED);
            
            showRetroAlert("Error", "No se pudo enviar la invitación: " + e.getMessage(), AlertType.ERROR);
        }
    }
    
    private void startInvitationTimeout(String opponentName) {
        new Thread(() -> {
            try {
                Thread.sleep(30000); // 30 segundos timeout
                
                Platform.runLater(() -> {
                    if (invitationPending && pendingOpponent.equals(opponentName)) {
                        invitationPending = false;
                        pendingOpponent = "";
                        
                        showRetroAlert("Tiempo Agotado", "La invitación a " + opponentName + " ha expirado.", AlertType.WARNING);
                        
                        // Actualizar lista
                        requestPlayersList();
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
    
    public void handleIncomingInvitation(String fromPlayer) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Invitación de Partida");
        alert.setHeaderText("¡Invitación recibida!");
        alert.setContentText("¿Aceptas jugar contra " + fromPlayer + "?");
        
        // Aplicar estilo retro al alert
        applyAlertStyle(alert);
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                acceptInvitation(fromPlayer);
            } else {
                rejectInvitation(fromPlayer);
                updateStatus("Rechazada invitación de " + fromPlayer);
            }
        });
    }
    
    private void acceptInvitation(String fromPlayer) {
        try {
            JSONObject response = new JSONObject();
            response.put("type", "invitationResponse");
            response.put("to", fromPlayer);
            response.put("from", Main.ctrlLogin.getUserName());
            response.put("accepted", true);
            
            Main.wsClient.safeSend(response.toString());
            updateStatus("Aceptada invitación de " + fromPlayer + ". Iniciando partida...");
            
            // Cambiar a vista de loading con animación controlada
            Main.pauseDuring(1000, () -> {
                UtilsViews.setViewAnimating("ViewLoading");
                CtrlLoading ctrlLoading = (CtrlLoading) UtilsViews.getController("ViewLoading");
                if (ctrlLoading != null) {
                    ctrlLoading.startLoadingAnimation(() -> {
                        // Esperar a que el servidor confirme el inicio del juego
                        // El cambio a ViewGame lo hará el servidor con "gameStart"
                    });
                }
            });
            
        } catch (Exception e) {
            System.err.println("Error aceptando invitación: " + e.getMessage());
        }
    }
    
    private void rejectInvitation(String fromPlayer) {
        try {
            JSONObject response = new JSONObject();
            response.put("type", "invitationResponse");
            response.put("to", fromPlayer);
            response.put("from", Main.ctrlLogin.getUserName());
            response.put("accepted", false);
            
            Main.wsClient.safeSend(response.toString());
            
        } catch (Exception e) {
            System.err.println("Error rechazando invitación: " + e.getMessage());
        }
    }
    
    public void updateStatus(String message) {
        Platform.runLater(() -> {
            lblStatus.setText(message);
        });
    }
    
    // Método para mostrar alerts con estilo retro
    private void showRetroAlert(String title, String message, AlertType type) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            applyAlertStyle(alert);
            alert.showAndWait();
        });
    }
    
    // Aplicar estilo retro a los alerts
    private void applyAlertStyle(Alert alert) {
        alert.getDialogPane().setStyle(
            "-fx-background-color: #000000; " +
            "-fx-border-color: #ffffff; " +
            "-fx-border-width: 3; " +
            "-fx-border-radius: 5; " +
            "-fx-background-radius: 5;"
        );
        
        // Aplicar estilo a los botones
        alert.getDialogPane().getButtonTypes().forEach(buttonType -> {
            Button button = (Button) alert.getDialogPane().lookupButton(buttonType);
            if (button != null && retroFont != null) {
                button.setFont(Font.font(retroFont.getFamily(), FontWeight.BOLD, 12));
                button.setStyle(
                    "-fx-background-color: #ffffff; " +
                    "-fx-text-fill: #000000; " +
                    "-fx-background-radius: 3; " +
                    "-fx-border-radius: 3; " +
                    "-fx-border-color: #000000; " +
                    "-fx-border-width: 1;"
                );
            }
        });
    }
    
    // Método para limpiar estado de invitación
    public static void clearInvitation() {
        invitationPending = false;
        pendingOpponent = "";
    }
}