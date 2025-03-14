package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.Data.GraphDurakData;
import org.example.Mesafe.DistanceCalculator;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public class Durak {
    private final DistanceCalculator distanceCalculator;
    private final GraphDurakData graphDurakData;

    Durak(GraphDurakData graphDurakData, DistanceCalculator distanceCalculator)  {
        this.graphDurakData = graphDurakData;
        this.distanceCalculator = distanceCalculator;
    }

    public Map.Entry<String, Double> findNearestStop(double lat, double lon) {
        double minDistance = Double.POSITIVE_INFINITY;
        String nearestStopId = null;

        for (HashMap.Entry<String, JsonNode> entry : graphDurakData.get_hashmap_duraklar().entrySet()) {
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



}
