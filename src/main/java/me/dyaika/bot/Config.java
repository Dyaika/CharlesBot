package me.dyaika.bot;


import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Config {
    private static final JsonObject CONFIG;
    private static final String JSON_CONFIG_PATH = "config.json";

    static {
        String jsonString;
        try {
            jsonString = new String(Files.readAllBytes(Paths.get(JSON_CONFIG_PATH)));
        } catch (IOException e) {
            throw new ExceptionInInitializerError("Error reading JSON file: " + e.getMessage());
        }
        CONFIG = new Gson().fromJson(jsonString, JsonObject.class);
    }

    public static String get(String key){
        return CONFIG.get(key).getAsString();
    }
}
