package com.client;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.animation.PauseTransition;
import javafx.scene.paint.Color;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Main extends Application {

    public static UtilsWS wsClient;

    public static String clientName = "";

    public static CtrlConfig ctrlConfig;

    public static void main(String[] args) {

        // Iniciar app JavaFX   
        launch(args);
    }
    
    @Override
    public void start(Stage stage) throws Exception {

        final int windowWidth = 400;
        final int windowHeight = 300;

        UtilsViews.parentContainer.setStyle("-fx-font: 14 arial;");
        UtilsViews.addView(getClass(), "ViewConfig", "/assets/viewConfig.fxml"); 

        ctrlConfig = (CtrlConfig) UtilsViews.getController("ViewConfig");

        Scene scene = new Scene(UtilsViews.parentContainer);
        
        stage.setScene(scene);
        stage.onCloseRequestProperty(); // Call close method when closing window
        stage.setTitle("JavaFX");
        stage.setMinWidth(windowWidth);
        stage.setMinHeight(windowHeight);
        stage.show();

        // Add icon only if not Mac
        if (!System.getProperty("os.name").contains("Mac")) {
            Image icon = new Image("file:/icons/icon.png");
            stage.getIcons().add(icon);
        }
    }

    @Override
    public void stop() { 
        if (wsClient != null) {
            wsClient.forceExit();
        }
        System.exit(1); // Kill all executor services
    }

    public static void pauseDuring(long milliseconds, Runnable action) {
        PauseTransition pause = new PauseTransition(Duration.millis(milliseconds));
        pause.setOnFinished(event -> Platform.runLater(action));
        pause.play();
    }

    public static <T> List<T> jsonArrayToList(JSONArray array, Class<T> clazz) {
        List<T> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            try {
                T value = clazz.cast(array.get(i));
                list.add(value);
            } catch (ClassCastException e) {
                System.err.println("Error casteando elemento del JSON array: " + e.getMessage());
            }
        }
        return list;
    }

    public static void connectToServer() {
        // Guardar el nombre del jugador
        clientName = ctrlConfig.txtPlayerName.getText().trim();
        
        // Validar que el nombre no esté vacío
        if (clientName.isEmpty()) {
            ctrlConfig.txtMessage.setTextFill(Color.RED);
            ctrlConfig.txtMessage.setText("Por favor, introduce tu nombre");
            return;
        }

        ctrlConfig.txtMessage.setTextFill(Color.BLACK);
        ctrlConfig.txtMessage.setText("Conectando...");

        pauseDuring(1500, () -> {
            String protocol = ctrlConfig.txtProtocol.getText();
            String host = ctrlConfig.txtHost.getText();
            String port = ctrlConfig.txtPort.getText();
            wsClient = UtilsWS.getSharedInstance(protocol + "://" + host + ":" + port);

            wsClient.onMessage((response) -> { Platform.runLater(() -> { wsMessage(response); }); });
            wsClient.onError((response) -> { Platform.runLater(() -> { wsError(response); }); });
            
            // Enviar nombre al servidor cuando se conecte
            wsClient.onOpen((response) -> {
                JSONObject loginMsg = new JSONObject();
                loginMsg.put("type", "login_request");
                loginMsg.put("username", clientName);
                wsClient.safeSend(loginMsg.toString());
            });
        });
    }
   
    private static void wsMessage(String response) {
    JSONObject msgObj = new JSONObject(response);
    switch (msgObj.getString("type")) {
        case "login_response":
            boolean success = msgObj.getBoolean("success");
            if (success) {
                // Login exitoso, ir a la vista de jugadores
                if (UtilsViews.getActiveView().equals("ViewConfig")) {
                    UtilsViews.setViewAnimating("ViewWait"); // O la vista que corresponda
                }
            } else {
                // Login fallido
                ctrlConfig.txtMessage.setTextFill(Color.RED);
                ctrlConfig.txtMessage.setText("Error de conexión: " + msgObj.optString("error", "Desconocido"));
            }
            break;
    }
}

    private static void wsError(String response) {
        String connectionRefused = "Connection refused";
        if (response.indexOf(connectionRefused) != -1) {
            ctrlConfig.txtMessage.setTextFill(Color.RED);
            ctrlConfig.txtMessage.setText(connectionRefused);
            pauseDuring(1500, () -> {
                ctrlConfig.txtMessage.setText("");
            });
        }
    }
}
