package org.example.Rota;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.Vehicle.Taxi;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.example.IRota.*;

import java.util.*;

public class Rota {
    public static class KenarOzellikleri extends DefaultWeightedEdge {
        private double mesafe;
        private double sure;
        private double ucret;
        private String type;
        private String baglanti_tipi;

        public KenarOzellikleri(double mesafe, double sure, double ucret, String type, String baglanti_tipi) {
            this.mesafe = mesafe;
            this.sure = sure;
            this.ucret = ucret;
            this.type = type;
            this.baglanti_tipi = baglanti_tipi;
        }

        public double getMesafe() { return mesafe; }
        public double getSure() { return sure; }
        public double getUcret() { return ucret; }
        public String getType() { return type; }
        public String getBaglanti_tipi() { return baglanti_tipi; }
    }

    private final GraphBuilder graphBuilder;
    private final PathFinder pathFinder;
    private final WaypointGenerator waypointGenerator;
    private final RoutePrinter routePrinter;
    private final DistanceCalculator distanceCalculator;
    private final Taxi taksi;
    private final ObjectMapper objectMapper;
    private final Map<String, JsonNode> duraklar;
    private final String city;
    private final Graph<String, KenarOzellikleri> graph;

    public Rota(String data, GraphBuilder graphBuilder, PathFinder pathFinder,
                WaypointGenerator waypointGenerator, RoutePrinter routePrinter,
                DistanceCalculator distanceCalculator, Taxi taksi) throws JsonProcessingException {
        this.objectMapper = new ObjectMapper();
        JsonNode jsonData = this.objectMapper.readTree(data);
        this.duraklar = new HashMap<>();

        for (JsonNode durak : jsonData.get("duraklar")) {
            duraklar.put(durak.get("id").asText(), durak);
        }

        this.city = jsonData.get("city").asText();
        this.taksi = taksi;
        this.graphBuilder = graphBuilder;
        this.pathFinder = pathFinder;
        this.waypointGenerator = waypointGenerator;
        this.routePrinter = routePrinter;
        this.distanceCalculator = distanceCalculator;
        this.graph = graphBuilder.buildGraph(jsonData);
    }

    public Map<String, Object> findRouteWithCoordinates(double startLat, double startLon,
                                                        double endLat, double endLon,
                                                        String optimization) {
        Map.Entry<String, Double> startStop = findNearestStop(startLat, startLon);
        Map.Entry<String, Double> endStop = findNearestStop(endLat, endLon);

        String startId = startStop.getKey();
        double startDistance = startStop.getValue();
        String endId = endStop.getKey();
        double endDistance = endStop.getValue();

        if (startId == null || endId == null) {
            throw new IllegalArgumentException("En yakın durak bulunamadı.");
        }

        List<Map<String, Object>> transitSegments = pathFinder.findBestPath(startId, endId, optimization);
        List<Map<String, Object>> route = new ArrayList<>();
        List<Map<String, Object>> waypoints;
        double totalDistance, totalTime, totalCost;
        boolean routeFound;

        // Eğer toplu taşıma rotası yoksa, sadece taksiyle gidilecek
        if (transitSegments.isEmpty() && !startId.equals(endId)) {
            // Toplu taşıma yok, sadece taksi alternatifi kullanılacak
            double taxiDistance = distanceCalculator.calculateDistance(startLat, startLon, endLat, endLon);
            Map<String, Object> taxiAlternative = taksi.createTaxiAlternative(startLat, startLon, endLat, endLon, taxiDistance);

            routeFound = false; // Toplu taşıma rotası bulunamadı
            totalDistance = taxiDistance;
            totalTime = (Double) taxiAlternative.get("tahmini_sure_dk");
            totalCost = (Double) taxiAlternative.get("ucret");

            // Waypoints sadece başlangıç ve hedef noktalarını içerir
            waypoints = new ArrayList<>();
            waypoints.add(createWaypoint("baslangic_nokta", "Başlangıç Noktası", startLat, startLon, true, false, 0.0));
            waypoints.add(createWaypoint("hedef_nokta", "Hedef Noktası", endLat, endLon, false, true, 0.0));
        } else {
            // Toplu taşıma rotası varsa, normal akış
            Map<String, Object> startSegment = createWalkingSegment(startLat, startLon, startId, startDistance, true);
            Map<String, Object> endSegment = createWalkingSegment(endLat, endLon, endId, endDistance, false);

            route.add(startSegment);
            route.addAll(transitSegments);
            route.add(endSegment);

            totalDistance = route.stream().mapToDouble(s -> (double) s.get("mesafe")).sum();
            totalTime = route.stream().mapToDouble(s -> (double) s.get("sure")).sum();
            totalCost = route.stream().mapToDouble(s -> (double) s.get("ucret")).sum();

            waypoints = waypointGenerator.generateWaypoints(route, startLat, startLon, endLat, endLon,
                    startDistance, endDistance, endId);
            routeFound = true;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("rota_bulundu", routeFound);
        result.put("baslangic_koordinat", Map.of("lat", startLat, "lon", startLon));
        result.put("hedef_koordinat", Map.of("lat", endLat, "lon", endLon));
        result.put("baslangic_durak", duraklar.get(startId));
        result.put("bitis_durak", duraklar.get(endId));
        result.put("toplam_mesafe_km", totalDistance);
        result.put("toplam_ucret", totalCost);
        result.put("toplam_sure_dk", totalTime);
        result.put("rota", route);
        result.put("waypoints", waypoints);

        double taxiDistance = distanceCalculator.calculateDistance(startLat, startLon, endLat, endLon);
        result.put("taksi_alternatifi", taksi.createTaxiAlternative(startLat, startLon, endLat, endLon, taxiDistance));

        return result;
    }

    private Map.Entry<String, Double> findNearestStop(double lat, double lon) {
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

    private Map<String, Object> createWalkingSegment(double lat, double lon, String stopId,
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

    private Map<String, Object> createWaypoint(String id, String name, double lat, double lon,
                                               boolean isStart, boolean isEnd, double distanceToStop) {
        Map<String, Object> waypoint = new HashMap<>();
        waypoint.put("id", id);
        waypoint.put("name", name);
        waypoint.put("lat", lat);
        waypoint.put("lon", lon);
        waypoint.put("type", "custom");
        waypoint.put("is_start", isStart);
        waypoint.put("is_end", isEnd);
        waypoint.put("mesafe_to_stop", distanceToStop);
        return waypoint;
    }

    public void printRouteDescription(Map<String, Object> result) {
        routePrinter.printRoute(result);
    }
}