package org.example.Data;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Data {

    private JSONObject veri;

    public Data() throws IOException {
        veri = set_data("veriseti.json");
    }

    public JSONObject get_data(){
        return this.veri;
    }

    public static JSONObject set_data(String filename) throws IOException {
        // Dosyayı oku
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);  //
            }
            // JSON verisini objeye dönüştür
            return new JSONObject(stringBuilder.toString());
        }
    }
}
