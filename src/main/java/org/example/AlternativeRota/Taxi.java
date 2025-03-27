package org.example.AlternativeRota;

import org.example.Data.TaxiData;
import org.example.DijkstraAlghorithm.Coordinate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Taxi extends AlternatifRota {
    private final double openingFee;
    private final double costPerKm;

    public Taxi(TaxiData taxiData) {
        this.openingFee = taxiData.getOpeningFee();
        this.costPerKm = taxiData.getCostPerKm();
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
        taxiAlternative.put("toplam_ucret", taxiFare);
        taxiAlternative.put("tahmini_sure_dk", taxiTime);

        List<Coordinate> taxiWaypoints = createWaypoints(startLat, startLon, endLat, endLon);
        taxiAlternative.put("coordinates", taxiWaypoints);

        return taxiAlternative;
    }


}