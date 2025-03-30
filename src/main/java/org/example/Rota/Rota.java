package org.example.Rota;

import org.example.Data.TaxiData;
import org.example.DijkstraAlghorithm.Coordinate;
import org.example.DijkstraAlghorithm.PathFinder;
import org.example.Durak;

import java.io.IOException;
import java.util.*;

public class Rota {
    private final PathFinder pathFinder;
    private final VehicleManager vehicleManager;
    private final TaxiData taxiData;
    private final Durak durak;
    private final RotaInfoManager rotaInfoManager;

    public Rota(PathFinder pathFinder,
                VehicleManager vehicleManager,
                TaxiData taxiData, Durak durak,RotaInfoManager rotaInfoManager) {

        this.durak = durak;
        this.taxiData = taxiData;
        this.pathFinder = pathFinder;
        this.vehicleManager = vehicleManager;
        this.rotaInfoManager = rotaInfoManager;
    }

    public RotaInfo findRouteWithCoordinates(double startLat, double startLon,
                                                        double endLat, double endLon,
                                                        String optimization) throws IOException {
        Map.Entry<String, Double> startStop = durak.findNearestStop(startLat, startLon);
        Map.Entry<String, Double> endStop = durak.findNearestStop(endLat, endLon);
        String startId = startStop.getKey();
        double startDistance = startStop.getValue();
        String endId = endStop.getKey();
        double endDistance = endStop.getValue();

        if (startId == null || endId == null) {
            throw new IllegalArgumentException("En yakın durak bulunamadı.");
        }

        RotaInfo rotaInfo = rotaInfoManager.initializePathInfo();

        this.vehicleManager.setVehicle_type(startDistance);
        Vehicle start_vehicle = this.vehicleManager.getVehicle_type();

        start_vehicle.create_way(rotaInfo,new Coordinate(String.valueOf(startLat),String.valueOf(startLon)),startDistance,"start_type");

        pathFinder.findBestPath(rotaInfo,startId, endId, optimization);

        this.vehicleManager.setVehicle_type(endDistance);
        Vehicle end_vehicle = this.vehicleManager.getVehicle_type();

        end_vehicle.create_way(rotaInfo,new Coordinate(String.valueOf(endLat),String.valueOf(endLon)),endDistance,"end_type");

        return rotaInfo;
    }



}