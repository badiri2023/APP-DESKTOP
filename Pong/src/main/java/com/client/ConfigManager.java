package com.client;

import org.json.JSONObject;
import java.io.*;
import java.nio.file.*;

public class ConfigManager {
    private static final String CONFIG_FILE = "pong_config.json";
    
    public static class ConfigData {
        public String playerName;
        public String protocol;
        public String host;
        public String port;
        
        public ConfigData() {
            this.playerName = "";
            this.protocol = "ws";
            this.host = "localhost";
            this.port = "3000";
        }
        
        public JSONObject toJSON() {
            JSONObject obj = new JSONObject();
            obj.put("playerName", playerName);
            obj.put("protocol", protocol);
            obj.put("host", host);
            obj.put("port", port);
            return obj;
        }
        
        public static ConfigData fromJSON(JSONObject obj) {
            ConfigData config = new ConfigData();
            config.playerName = obj.optString("playerName", "");
            config.protocol = obj.optString("protocol", "ws");
            config.host = obj.optString("host", "localhost");
            config.port = obj.optString("port", "3000");
            return config;
        }
    }
    
    public static void saveConfig(ConfigData config) {
        try (FileWriter file = new FileWriter(CONFIG_FILE)) {
            JSONObject jsonConfig = config.toJSON();
            file.write(jsonConfig.toString());
            System.out.println("Configuración guardada en: " + CONFIG_FILE);
        } catch (IOException e) {
            System.err.println("Error guardando configuración: " + e.getMessage());
        }
    }
    
    public static ConfigData loadConfig() {
        try {
            File file = new File(CONFIG_FILE);
            if (!file.exists()) {
                System.out.println("No se encontró archivo de configuración, usando valores por defecto");
                return new ConfigData();
            }
            
            // Leer archivo de forma compatible
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line);
                }
            }
            
            JSONObject jsonConfig = new JSONObject(content.toString());
            return ConfigData.fromJSON(jsonConfig);
        } catch (IOException e) {
            System.out.println("Error leyendo configuración, usando valores por defecto");
            return new ConfigData();
        }
    }
}