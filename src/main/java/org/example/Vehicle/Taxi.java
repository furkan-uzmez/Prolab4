package org.example.Vehicle;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.DijkstraAlghorithm.Coordinate;

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

        List<Coordinate> taxiWaypoints = createTaxiWaypoints(startLat, startLon, endLat, endLon);
        taxiAlternative.put("coordinates", taxiWaypoints);

        return taxiAlternative;
    }

    private List<Coordinate>createTaxiWaypoints(double startLat, double startLon,
                                                          double endLat, double endLon) {

        List<Coordinate> vertices = new ArrayList<>();
        vertices.add(new Coordinate(String.valueOf(startLat),String.valueOf(startLon)));
        vertices.add(new Coordinate(String.valueOf(endLat),String.valueOf(endLon)));

        return vertices;
    }
}