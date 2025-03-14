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

    private String veri;

    //private Map<String, JsonNode> duraklar;

    public Data() throws IOException {
        this.veri = set_data("veriseti.json");
    }

    public String get_data() {
        return this.veri;
    }

    public static String set_data(String filename) throws IOException {
        // Dosyanın içeriğini oku
        return new String(Files.readAllBytes(Paths.get(filename)));
    }


}