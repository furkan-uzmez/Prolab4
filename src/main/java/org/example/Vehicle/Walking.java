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

    public Map<String, Object> createWalkingSegment(double lat, double lon, String stopId,
                                                     double distance, boolean isStart) {
        Map<String, Object> segment = new HashMap<>();
        Map<String, Object> point = new HashMap<>();
        point.put("id", isStart ? "baslangic_nokta" : "hedef_nokta");
        point.put("name", isStart ? "Başlangıç Noktası" : "Hedef Noktası");
        point.put("lat", lat);
        point.put("lon", lon);
        point.put("type", "custom");

        segment.put(isStart ? "baslangic_durak" : "bitis_durak", point);
        segment.put(isStart ? "bitis_durak" : "baslangic_durak",
                objectMapper.convertValue(duraklar.get(stopId), Map.class));
        segment.put("mesafe", distance);
        segment.put("sure", distance * 15);
        segment.put("ucret", 0.0);
        segment.put("baglanti_tipi", "walking");
        return segment;
    }
}
