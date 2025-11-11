package com.client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    public static String clientName = "";
    public static CtrlConfig ctrlConfig;
    
    @Override
    public void start(Stage stage) throws Exception {

        final int windowWidth = 600;  // Ajustado al tamaño del FXML
        final int windowHeight = 500;

        UtilsViews.parentContainer.setStyle("-fx-font: 14 arial;");
        
        // CORREGIDO: Ruta correcta del FXML
        UtilsViews.addView(getClass(), "ViewConfig", "resources/assets/viewConfig.fxml");

        ctrlConfig = (CtrlConfig) UtilsViews.getController("ViewConfig");

        Scene scene = new Scene(UtilsViews.parentContainer, windowWidth, windowHeight);
        
        stage.setScene(scene);
        stage.setTitle("PONG - Configuración");
        stage.setMinWidth(windowWidth);
        stage.setMinHeight(windowHeight);
        
        // Add icon - corrección de la ruta
        try {
            Image icon = new Image(getClass().getResourceAsStream("/assets/icon.png"));
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.out.println("No se pudo cargar el icono: " + e.getMessage());
        }
        
        stage.show();
    }

    @Override
    public void stop() { 
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}