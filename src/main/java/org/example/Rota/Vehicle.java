package org.example.Rota;

import org.example.DijkstraAlghorithm.Coordinate;

import java.util.List;
import java.util.Map;

public abstract class Vehicle {
    public Map<String, Object> create_way(Map<String,Object> path_info, Coordinate coordinate, double distance , String start_or_end) {
        path_info.put(start_or_end,this.get_name());
        ((List<Coordinate>)path_info.get("coordinates")).add(coordinate);


        double total_distance = (double) path_info.get("toplam_mesafe_km");
        double totalCost = (double) path_info.get("toplam_ucret");
        double totalTime = (double) path_info.get("toplam_sure_dk");

        total_distance += distance;
        totalTime += this.get_sure(distance);
        totalCost += this.get_ucret(distance);


        path_info.replace("toplam_mesafe_km",total_distance);
        path_info.replace("toplam_ucret",totalCost);
        path_info.replace("toplam_sure_dk",totalTime);

        return path_info;
    }

    abstract String get_name();
    abstract double get_sure(double distance);
    abstract double get_ucret(double distance);
}
