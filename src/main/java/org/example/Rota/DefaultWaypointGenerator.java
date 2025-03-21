package org.example.Rota;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.DijkstraAlghorithm.Coordinate;
import org.example.Vehicle.WaypointGenerator;

import java.util.*;

public class DefaultWaypointGenerator implements WaypointGenerator {
    private final Map<Coordinate, JsonNode> stops;

    public DefaultWaypointGenerator(Map<Coordinate, JsonNode> stops) {
        this.stops = stops;
    }

    @Override
    public List<Map<String, Object>> generateWaypoints(List<Map<String, Object>> route, double startLat, double startLon, double endLat, double endLon, double startDistance, double endDistance, String endStopId) {
        return List.of();
    }


//    @Override
//    public HashMap generateWaypoints(List<Coordinate> route,
//                                                       double startLat, double startLon,
//                                                       double endLat, double endLon
//    ) {
//        HashMap waypoints = new HashMap();
//
//        for (int i = 0; i < route.size() - 1; i++) {
//            List next_stops = (List) stops.get(route.get(i)).get("nextStops");
//            for ()
//        }
//
//        return waypoints;
//    }
}