package org.example.Rota;

import org.example.DijkstraAlghorithm.Coordinate;

import java.util.ArrayList;
import java.util.HashMap;

public class RotaInfoManager {
    private HashMap<String, Object> path_info;

    public RotaInfoManager(){
        path_info = new HashMap<>();
    }

    public void add_path_info(RotaInfo new_path_info,String rota_ad) {
        this.path_info.put(rota_ad,new_path_info.get_path_info());
    }

    public HashMap<String, Object> getAllPath_info() {
        return path_info;
    }

    public RotaInfo initializePathInfo() {
        RotaInfo rotaInfo = new RotaInfo();
//        rotaInfo.setStart_type(null);
//        rotaInfo.setEnd_type(null);
//        rotaInfo.setCoordinates(new ArrayList<Coordinate>());
//        rotaInfo.setToplam_mesafe(0.0);
//        rotaInfo.setToplam_ucret(0.0);
        return rotaInfo;
    }

}
