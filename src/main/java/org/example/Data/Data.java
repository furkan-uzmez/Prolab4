package org.example.Data;

import com.fasterxml.jackson.databind.JsonNode;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

@Service
public class Data {

    private JSONObject veri;

    private Map<String, JsonNode> duraklar;

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