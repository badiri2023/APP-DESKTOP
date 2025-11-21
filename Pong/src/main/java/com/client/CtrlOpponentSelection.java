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

    public String selectedPlayer;
    public String currentPlayer;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            // Cargar fuente retro
            try {
                retroFont = Font.loadFont(getClass().getResourceAsStream("/assets/fonts/8bitOperatorPlus8-Regular.ttf"), 14);
                System.out.println("Fuente cargada en OpponentSelection: " + retroFont.getFamily());
            } catch (Exception e) {
                System.out.println("No se pudo cargar fuente 8bitOperatorPlus8: " + e.getMessage());
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
                selectedPlayer = listPlayers.getSelectionModel().getSelectedItem();
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
    
    // NUEVO MÉTODO para manejar JSONArray (como la Raspberry Pi)
    // En el método updatePlayersList, filtrar la Raspberry Pi:

    public void updatePlayersList(JSONArray playersArray) {
        Platform.runLater(() -> {
            System.out.println("ACTUALIZANDO LISTA DE JUGADORES");
            System.out.println("Datos recibidos: " + playersArray.toString());
            
            listPlayers.getItems().clear();
            
            int availablePlayers = 0;
            currentPlayer = Main.ctrlLogin.getUserName();
            System.out.println("Mi nombre: " + currentPlayer);
            
            for (int i = 0; i < playersArray.length(); i++) {
                try {
                    String player = playersArray.getString(i);
                    System.out.println("Jugador en lista: " + player);
                    
                    if (player.equals(currentPlayer)) {
                        System.out.println("Filtrado: soy yo mismo");
                        continue;
                    }
                    
                    if (isRaspberryPi(player)) {
                        System.out.println("Filtrado: es Raspberry Pi");
                        continue;
                    }
                    
                    listPlayers.getItems().add(player);
                    availablePlayers++;
                    System.out.println("Añadido a lista: " + player);
                    
                } catch (Exception e) {
                    System.err.println("Error procesando jugador: " + e.getMessage());
                }
            }
            
            System.out.println("Total jugadores disponibles: " + availablePlayers);
        });
    }

    private boolean isRaspberryPi(String playerName) {
        if (playerName == null) return false;
        
        String lowerName = playerName.toLowerCase();
        return lowerName.contains("pantalla") || 
            lowerName.contains("raspberry") || 
            lowerName.contains("pi") ||
            lowerName.contains("matrix") ||
            lowerName.equals("screen") ||
            lowerName.equals("display");
    }
    
    private void sendInvitation(String opponentName) {
        try {
            String myName = Main.ctrlLogin.getUserName();
            System.out.println("ENVIANDO INVITACIÓN: De " + myName + " para " + opponentName);
            
            JSONObject invitation = new JSONObject();
            invitation.put("type", "challenge");
            invitation.put("to", opponentName);
            invitation.put("from", myName);  
            
            String invitationStr = invitation.toString();
            System.out.println("Mensaje JSON enviado: " + invitationStr);
            
            Main.wsClient.safeSend(invitationStr);
            
            invitationPending = true;
            pendingOpponent = opponentName;
            startInvitationTimeout(opponentName);
            
            System.out.println("Invitación enviada correctamente");
            
        } catch (Exception e) {
            System.err.println("Error enviando invitación: " + e.getMessage());
            e.printStackTrace();
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

                        Main.showAlert("Tiempo Agotado", 
                                    "La invitación a " + opponentName + " ha expirado.", 
                                    AlertType.WARNING);
                        
                        // Actualizar lista
                        requestPlayersList();
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void rejectInvitation(String fromPlayer) {
        try {
            JSONObject response = new JSONObject();
            response.put("type", "challenge_response"); 
            response.put("to", fromPlayer);
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
    
    // Método para limpiar estado de invitación
    public static void clearInvitation() {
        invitationPending = false;
        pendingOpponent = "";
    }
}