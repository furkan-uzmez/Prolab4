package org.example.Vehicle;

import org.example.DijkstraAlghorithm.Coordinate;

import java.util.List;
import java.util.Map;

// Waypoint oluşturucu arayüzü
public interface WaypointGenerator {
    List<Map<String, Object>> generateWaypoints(List<Map<String, Object>> route,
                                                double startLat, double startLon,
                                                double endLat, double endLon,
                                                double startDistance, double endDistance,
                                                String endStopId);

}
