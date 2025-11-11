package com.client;

import java.io.*;
import java.util.Properties;

public class ConfigManager {
    
    public static class ConfigData {
        public String playerName = "";
        public String serverUrl = "matrixplay5.ieti.site";
    }
    
    private static final String CONFIG_FILE = "pong_config.properties";
    
    public static ConfigData loadConfig() {
        ConfigData config = new ConfigData();
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            Properties prop = new Properties();
            prop.load(input);
            config.playerName = prop.getProperty("playerName", "");
            config.serverUrl = prop.getProperty("serverUrl", "matrixplay5.ieti.site");
        } catch (IOException e) {
            System.out.println("No se encontró configuración previa, usando valores por defecto");
        }
        return config;
    }
    
    public static void saveConfig(ConfigData config) {
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            Properties prop = new Properties();
            prop.setProperty("playerName", config.playerName);
            prop.setProperty("serverUrl", config.serverUrl);
            prop.store(output, "Pong Game Configuration");
        } catch (IOException e) {
            System.err.println("Error guardando configuración: " + e.getMessage());
        }
    }
}