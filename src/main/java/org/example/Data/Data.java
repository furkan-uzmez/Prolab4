package org.example.Data;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
public class Data {

    private JSONObject veri;

    public Data() throws IOException {
        this.veri = set_data("veriseti.json");
    }

    public JSONObject get_data() {
        return this.veri;
    }

    public static JSONObject set_data(String filename) throws IOException {
        // Dosyanın içeriğini oku
        String jsonString = new String(Files.readAllBytes(Paths.get(filename)));

        return new JSONObject(jsonString);
    }
}