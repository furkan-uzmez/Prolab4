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

    public Rota(PathFinder pathFinder,
                VehicleManager vehicleManager,
                TaxiData taxiData, Durak durak) {

        this.durak = durak;
        this.taxiData = taxiData;
        this.pathFinder = pathFinder;
        this.vehicleManager = vehicleManager;
    }

    public Map<String, Object> findRouteWithCoordinates(double startLat, double startLon,
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

        Map<String, Object> path_info = initializePathInfo();

        this.vehicleManager.setVehicle_type(startDistance);
        Vehicle start_vehicle = this.vehicleManager.getVehicle_type();


        start_vehicle.create_way(path_info,new Coordinate(String.valueOf(startLat),String.valueOf(startLon)),startDistance,"start_type");

        path_info = pathFinder.findBestPath(path_info,startId, endId, optimization);

        this.vehicleManager.setVehicle_type(endDistance);
        Vehicle end_vehicle = this.vehicleManager.getVehicle_type();

        end_vehicle.create_way(path_info,new Coordinate(String.valueOf(endLat),String.valueOf(endLon)),endDistance,"end_type");

        return path_info;
    }

    private Map<String, Object> initializePathInfo() {
        Map<String, Object> pathInfo = new HashMap<>();
        pathInfo.put("start_type", null);
        pathInfo.put("end_type", null);
        pathInfo.put("coordinates", new ArrayList<Coordinate>());
        pathInfo.put("toplam_mesafe_km", 0.0);
        pathInfo.put("toplam_ucret", 0.0);
        pathInfo.put("toplam_sure_dk", 0.0);
        return pathInfo;
    }
    private RotaInfo initializePathInfo2() {
        RotaInfo rotaInfo = new RotaInfo();
        rotaInfo.setStart_type(null);
        rotaInfo.setEnd_type(null);
        rotaInfo.setCoordinates(new ArrayList<Coordinate>());
        rotaInfo.setToplam_mesafe(0.0);
        rotaInfo.setToplam_ucret(0.0);
//        rotaInfo.se
        return rotaInfo;
    }

}