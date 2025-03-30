package org.example.AlternativeRota;

import org.example.Data.TaxiData;
import org.example.DijkstraAlghorithm.Coordinate;
import org.example.Rota.RotaInfo;

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
    public RotaInfo createAlternative(double startLat, double startLon,
                                      double endLat, double endLon,
                                      double distanceKm) {

        RotaInfo rotaInfo = new RotaInfo();
        double taxiFare = calculateTaxiFare(distanceKm);
        double taxiTime = distanceKm * 2; // Taxi time estimate: 2 min/km


        rotaInfo.setToplam_mesafe(distanceKm);
        rotaInfo.setToplam_ucret(taxiFare);
        rotaInfo.setToplam_sure(taxiTime);

        List<Coordinate> taxiWaypoints = createWaypoints(startLat, startLon, endLat, endLon);

        for (Coordinate coordinate:taxiWaypoints){
            rotaInfo.getCoordinates().add(coordinate);
        }

        rotaInfo.addPath_info();

        return rotaInfo;
    }


}