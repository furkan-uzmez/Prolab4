package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.Data.Data;
import org.example.IRota.DistanceCalculator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public class Durak {
    private final JSONObject veri;
    private ObjectMapper objectMapper;
    private HashMap<String, JsonNode> duraklar;
    private JSONArray durak_veri;
    private final DistanceCalculator distanceCalculator;

    public String getDurak_veri() {
        return durak_veri.toString();
    }

    public void setDurak_veri(JSONArray durak_veri) {
        this.durak_veri = durak_veri;
    }
    Durak(JSONObject veri, ObjectMapper objectMapper, DistanceCalculator distanceCalculator) throws JsonProcessingException {
        this.veri = veri;
        this.objectMapper = objectMapper;
        this.distanceCalculator = distanceCalculator;
        this.setDurak_veri(veri.getJSONArray("duraklar"));
        this.set_hashmap_duraklar();
    }

    public Map.Entry<String, Double> findNearestStop(double lat, double lon) {
        double minDistance = Double.POSITIVE_INFINITY;
        String nearestStopId = null;

        for (Map.Entry<String, JsonNode> entry : duraklar.entrySet()) {
            JsonNode stop = entry.getValue();
            double distance = distanceCalculator.calculateDistance(lat, lon,
                    stop.get("lat").asDouble(),
                    stop.get("lon").asDouble());
            if (distance < minDistance) {
                minDistance = distance;
                nearestStopId = entry.getKey();
            }
        }

        return new AbstractMap.SimpleEntry<>(nearestStopId, minDistance);
    }

    public void set_hashmap_duraklar() throws JsonProcessingException {
        JsonNode jsonData = this.objectMapper.readTree(String.valueOf(this.veri));
        this.duraklar =  new HashMap<>();
        for (JsonNode durak : jsonData.get("duraklar")) {
            duraklar.put(durak.get("id").asText(), durak);
        }
    }

    public HashMap get_hashmap_duraklar(){
        return this.duraklar;
    }

    public JsonNode get_node_data() throws JsonProcessingException {
        return this.objectMapper.readTree(String.valueOf(this.veri));
    }
}
