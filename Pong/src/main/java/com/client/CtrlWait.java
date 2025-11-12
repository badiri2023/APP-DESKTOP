package com.client;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

public class CtrlWait implements Initializable{
    
    // Ya no necesitamos los labels de jugadores
    // Solo mantenemos el initialize básico

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Vista simplificada, solo muestra "Coming Soon"
        System.out.println("Vista de espera - Coming Soon");
    }

    // Eliminamos todos los métodos anteriores ya que no son necesarios
    // para la vista "Coming Soon"
}