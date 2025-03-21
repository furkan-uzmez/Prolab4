package org.example.Rota;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.Data.GraphDurakData;
import org.example.DijkstraAlghorithm.Coordinate;
import org.example.DijkstraAlghorithm.PathFinder;
import org.example.Durak;
import org.example.Mesafe.DistanceCalculator;
import org.example.Vehicle.Taxi;
import org.example.Graph.Graph;
import org.example.Vehicle.WaypointGenerator;

import java.util.*;

public class Rota {
    private final Graph graph;
    private final PathFinder pathFinder;
    private final WaypointGenerator waypointGenerator;
    private final DistanceCalculator distanceCalculator;
    private final Taxi taksi;
    private final ObjectMapper objectMapper;
    private final HashMap<Coordinate, JsonNode> duraklar;
    private final Durak durak;

    public Rota(Graph graph, PathFinder pathFinder,
                WaypointGenerator waypointGenerator,
                DistanceCalculator distanceCalculator, Taxi taksi, Durak durak, GraphDurakData graphDurakData) {

        this.objectMapper = new ObjectMapper();
        this.durak = durak;
        this.duraklar = graphDurakData.get_hashmap_duraklar();
        this.taksi = taksi;
        this.graph = graph;
        this.pathFinder = pathFinder;
        this.waypointGenerator = waypointGenerator;
        this.distanceCalculator = distanceCalculator;
    }

    public Map<String, Object> findRouteWithCoordinates(double startLat, double startLon,
                                                        double endLat, double endLon,
                                                        String optimization) {
        Map.Entry<String, Double> startStop = durak.findNearestStop(startLat, startLon);
        Map.Entry<String, Double> endStop = durak.findNearestStop(endLat, endLon);
        String startId = startStop.getKey();
        double startDistance = startStop.getValue();
        String endId = endStop.getKey();
        double endDistance = endStop.getValue();

        if (startId == null || endId == null) {
            throw new IllegalArgumentException("En yakın durak bulunamadı.");
        }

        List<Coordinate> coordinates = new ArrayList<>();
        coordinates.add(new Coordinate(String.valueOf(startLat),String.valueOf(startLon)));

        // Toplu taşıma rotası
        Map<String, Object> yol_bilgileri = pathFinder.findBestPath(startId, endId, optimization);

        ArrayList<Coordinate> ara_koordinatlar = (ArrayList<Coordinate>) yol_bilgileri.get("ara_koordinatlar");

        coordinates.addAll(ara_koordinatlar);

        coordinates.add(new Coordinate(String.valueOf(endLat),String.valueOf(endLon)));

        yol_bilgileri.remove("ara_koordinatlar");

        yol_bilgileri.put("coordinates",coordinates);

        return yol_bilgileri;
    }

}