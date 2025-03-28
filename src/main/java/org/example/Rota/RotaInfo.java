package org.example.Rota;

import org.example.DijkstraAlghorithm.Coordinate;

import java.util.ArrayList;
import java.util.HashMap;

public class RotaInfo implements IRotaInfo{
    private String start_type;
    private String end_type;
    private ArrayList<Coordinate> coordinates;
    private Double toplam_mesafe;
    private Double toplam_sure;
    private Double toplam_ucret;
    private HashMap<String,Object> path_info;

    public void setStart_type(String start_type) {
        this.start_type = start_type;
    }

    public void setEnd_type(String end_type) {
        this.end_type = end_type;
    }

    public void setCoordinates(ArrayList<Coordinate> coordinates) {
        this.coordinates = coordinates;
    }

    public void setToplam_mesafe(Double toplam_mesafe) {
        this.toplam_mesafe = toplam_mesafe;
    }

    public void setToplam_sure(Double toplam_sure) {
        this.toplam_sure = toplam_sure;
    }

    public void setToplam_ucret(Double toplam_ucret) {
        this.toplam_ucret = toplam_ucret;
    }


    public void addPath_info() {
        path_info = new HashMap<>();
        path_info.put("start_type", start_type);
        path_info.put("end_type", end_type);
        path_info.put("coordinates", coordinates);
        path_info.put("toplam_mesafe", toplam_mesafe);
        path_info.put("toplam_sure", toplam_sure);
        path_info.put("toplam_ucret", toplam_ucret);
    }

    public HashMap<String,Object> get_path_info() {
        return path_info;
    }


}
