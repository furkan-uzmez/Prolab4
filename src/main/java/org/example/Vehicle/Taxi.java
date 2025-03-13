package org.example.Vehicle;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Taxi implements Arac{
    private final JsonNode taxiData;
    private final double openingFee;
    private final double costPerKm;

    public Taxi(JsonNode taxiData) {
        this.taxiData = taxiData;
        this.openingFee = taxiData.get("openingFee").asDouble();
        this.costPerKm = taxiData.get("costPerKm").asDouble();
    }

    public double calculateTaxiFare(double distanceKm) {
        return openingFee + (distanceKm * costPerKm);
    }

    @Override
    public Map<String, Object> createAlternative(double startLat, double startLon,
                                                 double endLat, double endLon,
                                                 double distanceKm) {
        Map<String, Object> taxiAlternative = new HashMap<>();

        double taxiFare = calculateTaxiFare(distanceKm);
        double taxiTime = distanceKm * 2; // Taxi time estimate: 2 min/km

        taxiAlternative.put("mesafe_km", distanceKm);
        taxiAlternative.put("ucret", taxiFare);
        taxiAlternative.put("tahmini_sure_dk", taxiTime);

        List<Map<String, Object>> taxiWaypoints = createTaxiWaypoints(startLat, startLon, endLat, endLon);
        taxiAlternative.put("waypoints", taxiWaypoints);

        return taxiAlternative;
    }

    private List<Map<String, Object>> createTaxiWaypoints(double startLat, double startLon,
                                                          double endLat, double endLon) {
        List<Map<String, Object>> taxiWaypoints = new ArrayList<>();

        Map<String, Object> taxiStart = new HashMap<>();
        taxiStart.put("id", "baslangic_nokta");
        taxiStart.put("name", "Başlangıç Noktası");
        taxiStart.put("lat", startLat);
        taxiStart.put("lon", startLon);
        taxiStart.put("type", "taksi");
        taxiStart.put("is_start", true);
        taxiStart.put("is_end", false);
        taxiWaypoints.add(taxiStart);

        Map<String, Object> taxiEnd = new HashMap<>();
        taxiEnd.put("id", "hedef_nokta");
        taxiEnd.put("name", "Hedef Noktası");
        taxiEnd.put("lat", endLat);
        taxiEnd.put("lon", endLon);
        taxiEnd.put("type", "taksi");
        taxiEnd.put("is_start", false);
        taxiEnd.put("is_end", true);
        taxiWaypoints.add(taxiEnd);

        return taxiWaypoints;
    }
}