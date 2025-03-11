package org.example.Data;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
        // Load the file as a classpath resource
        try (InputStream inputStream = Data.class.getClassLoader().getResourceAsStream(filename)) {
            if (inputStream == null) {
                throw new IOException("File not found: " + filename);
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            // Convert the string to a JSONObject
            return new JSONObject(stringBuilder.toString());
        }
    }
}