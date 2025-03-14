package org.example.Rota;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.Data.GraphDurakData;
import org.example.Durak;
import org.example.Mesafe.DistanceCalculator;
import org.example.Vehicle.Taxi;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.example.IRota.*;

import java.util.*;

public class Rota {


    /*public static class KenarOzellikleri extends DefaultWeightedEdge {
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
        public String getBaglanti_tipi() { return baglanti_tipi; }
    }*/

    private final GraphBuilder graphBuilder;
    private final PathFinder pathFinder;
    private final WaypointGenerator waypointGenerator;
    private final RoutePrinter routePrinter;
    private final DistanceCalculator distanceCalculator;
    private final Taxi taksi;
    private final ObjectMapper objectMapper;
    private final Map<String, JsonNode> duraklar;
    private final Graph<String, EdgeFeatures> graph;
    private final Durak durak;

    public Rota(GraphBuilder graphBuilder, PathFinder pathFinder,
                WaypointGenerator waypointGenerator, RoutePrinter routePrinter,
                DistanceCalculator distanceCalculator, Taxi taksi, Durak durak, GraphDurakData graphDurakData) throws JsonProcessingException {

        this.objectMapper = new ObjectMapper();
        this.durak = durak;
        this.duraklar = graphDurakData.get_hashmap_duraklar();
        this.taksi = taksi;
        this.graphBuilder = graphBuilder;
        this.pathFinder = pathFinder;
        this.waypointGenerator = waypointGenerator;
        this.routePrinter = routePrinter;
        this.distanceCalculator = distanceCalculator;
        this.graph = graphBuilder.buildGraph(graphDurakData.get_node_data());
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

        // Toplu taşıma rotası
        List<Map<String, Object>> transitSegments = pathFinder.findBestPath(startId, endId, optimization);
        List<Map<String, Object>> route = new ArrayList<>();
        List<Map<String, Object>> waypoints;
        double totalDistance = 0.0;
        double totalTime = 0.0;
        double totalCost = 0.0;
        boolean routeFound = !transitSegments.isEmpty();

        //System.out.println(transitSegments);
        // Toplu taşıma rotası bulunduysa
        Map<String, Object> startSegment = createWalkingSegment(startLat, startLon, startId, startDistance, true);
        Map<String, Object> endSegment = createWalkingSegment(endLat, endLon, endId, endDistance, false);

         //System.out.println("endSegment:"+endSegment);

        route.add(startSegment);
        route.addAll(transitSegments);
        route.add(endSegment);

        totalDistance = route.stream().mapToDouble(s -> (double) s.get("mesafe")).sum();
        totalTime = route.stream().mapToDouble(s -> (double) s.get("sure")).sum();
        totalCost = route.stream().mapToDouble(s -> (double) s.get("ucret")).sum();

        waypoints = waypointGenerator.generateWaypoints(route, startLat, startLon, endLat, endLon,
                    startDistance, endDistance, endId);


        // Taksi alternatifi (her zaman oluşturuluyor)
        double taxiDistance = distanceCalculator.calculateDistance(startLat, startLon, endLat, endLon);
        Map<String, Object> taxiAlternative = taksi.createAlternative(startLat, startLon, endLat, endLon, taxiDistance);

        // Sonuç objesi
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
        result.put("taksi_alternatifi", taxiAlternative);

        return result;
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


}