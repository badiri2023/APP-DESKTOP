package com.client;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import java.net.URL;
import java.util.ResourceBundle;
import org.json.JSONObject;

public class CtrlOpponentSelection implements Initializable {

    @FXML
    private ListView<String> listPlayers;
    
    @FXML
    private Label lblStatus;

    // Variables para manejar invitaciones
    public static boolean invitationPending = false;
    public static String pendingOpponent = "";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Configurar lista de jugadores
        listPlayers.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selectedPlayer = listPlayers.getSelectionModel().getSelectedItem();
                if (selectedPlayer != null && !selectedPlayer.equals(Main.ctrlLogin.getUserName())) {
                    sendInvitation(selectedPlayer);
                }
            }
        });
        
        // Inicializar estado
        lblStatus.setText("Conectando al servidor...");
        
        // Solicitar lista de jugadores al servidor
        requestPlayersList();
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
    
    public void updatePlayersList(String[] players) {
        System.out.println("Actualizando lista de jugadores...");
        
        if (players == null) {
            lblStatus.setText("Error: datos no disponibles");
            listPlayers.getItems().clear();
            return;
        }
        
        // Limpiar la lista actual
        listPlayers.getItems().clear();
        
        int availablePlayers = 0;
        String currentPlayer = Main.ctrlLogin.getUserName();
        
        for (String player : players) {
            // Solo mostrar jugadores que no sean yo mismo
            if (player != null && !player.equals(currentPlayer) && !player.isEmpty()) {
                listPlayers.getItems().add(player);
                availablePlayers++;
                System.out.println("Añadido jugador: " + player);
            }
        }
        
        // Actualizar el mensaje de estado
        if (availablePlayers == 0) {
            lblStatus.setText("No hay otros jugadores conectados");
        } else {
            lblStatus.setText(availablePlayers + " jugador(es) disponible(s). Haz doble click para invitar.");
        }
        
        System.out.println("Lista actualizada. Jugadores disponibles: " + availablePlayers);
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
            
            // Mostrar confirmación
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Invitación Enviada");
            alert.setHeaderText(null);
            alert.setContentText("Invitación enviada a " + opponentName + ". Esperando respuesta...");
            alert.showAndWait();
            
        } catch (Exception e) {
            System.err.println("Error enviando invitación: " + e.getMessage());
            lblStatus.setText("Error al enviar invitación");
            
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("No se pudo enviar la invitación: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    private void startInvitationTimeout(String opponentName) {
        new Thread(() -> {
            try {
                Thread.sleep(30000); // 30 segundos timeout
                
                javafx.application.Platform.runLater(() -> {
                    if (invitationPending && pendingOpponent.equals(opponentName)) {
                        invitationPending = false;
                        pendingOpponent = "";
                        
                        Alert alert = new Alert(AlertType.WARNING);
                        alert.setTitle("Tiempo Agotado");
                        alert.setHeaderText(null);
                        alert.setContentText("La invitación a " + opponentName + " ha expirado.");
                        alert.showAndWait();
                        
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
        lblStatus.setText(message);
    }
    
    // Método para limpiar estado de invitación
    public static void clearInvitation() {
        invitationPending = false;
        pendingOpponent = "";
    }
}