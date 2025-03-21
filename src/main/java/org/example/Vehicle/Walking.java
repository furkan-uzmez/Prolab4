package org.example.Vehicle;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class Walking {
    private Map<String, JsonNode> duraklar;
    private ObjectMapper objectMapper;

    public Walking(Map<String, JsonNode> duraklar){
        this.objectMapper = new ObjectMapper();
        this.duraklar = duraklar;
    }

    public Map<String, Object> createWalkingSegment(double lat, double lon, double distance) {
        Map<String, Object> segment = new HashMap<>();
        segment.put("lat", lat);
        segment.put("lon", lon);
        segment.put("mesafe", distance);
        segment.put("sure", distance * 15);
        segment.put("ucret", 0.0);
        segment.put("baglanti_tipi", "walking");
        return segment;
    }
}
