package org.example.Rota;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.IRota.WaypointGenerator;

import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

public class DefaultWaypointGenerator implements WaypointGenerator {
    private final ObjectMapper objectMapper;
    private final Map<String, JsonNode> stops;

    public DefaultWaypointGenerator(ObjectMapper objectMapper, Map<String, JsonNode> stops) {
        this.objectMapper = objectMapper;
        this.stops = stops;
    }

    @Override
    public List<Map<String, Object>> generateWaypoints(List<Map<String, Object>> route,
                                                       double startLat, double startLon,
                                                       double endLat, double endLon,
                                                       double startDistance, double endDistance,
                                                       String endStopId) {
        List<Map<String, Object>> waypoints = new ArrayList<>();
        Set<String> visitedStops = new HashSet<>();

        Map<String, Object> startWaypoint = new HashMap<>();
        startWaypoint.put("id", "baslangic_nokta");
        startWaypoint.put("name", "Başlangıç Noktası");
        startWaypoint.put("lat", startLat);
        startWaypoint.put("lon", startLon);
        startWaypoint.put("type", "custom");
        startWaypoint.put("is_start", true);
        startWaypoint.put("is_end", false);
        startWaypoint.put("mesafe_to_stop", startDistance);
        waypoints.add(startWaypoint);
        visitedStops.add("baslangic_nokta");

        for (Map<String, Object> segment : route) {
            Map<String, Object> startStop = (Map<String, Object>) segment.get("baslangic_durak");
            String stopId = (String) startStop.get("id");

            if (!"baslangic_nokta".equals(stopId) && !"hedef_nokta".equals(stopId) && !visitedStops.contains(stopId)) {
                Map<String, Object> waypoint = new HashMap<>(startStop);
                waypoint.put("is_start", false);
                waypoint.put("is_end", false);
                waypoints.add(waypoint);
                visitedStops.add(stopId);
            }
        }

        if (!visitedStops.contains(endStopId)) {
            Map<String, Object> endStop = objectMapper.convertValue(stops.get(endStopId), Map.class);
            endStop.put("is_start", false);
            endStop.put("is_end", false);
            waypoints.add(endStop);
            visitedStops.add(endStopId);
        }

        Map<String, Object> endWaypoint = new HashMap<>();
        endWaypoint.put("id", "hedef_nokta");
        endWaypoint.put("name", "Hedef Noktası");
        endWaypoint.put("lat", endLat);
        endWaypoint.put("lon", endLon);
        endWaypoint.put("type", "custom");
        endWaypoint.put("is_start", false);
        endWaypoint.put("is_end", true);
        endWaypoint.put("mesafe_to_stop", endDistance);
        waypoints.add(endWaypoint);

        return waypoints;
    }
}