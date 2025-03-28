package org.example.Rota;

import java.util.HashMap;

public class RotaInfoManager {
    private HashMap<String, Object> path_info;

    public void add_path_info(RotaInfo new_path_info,String rota_ad) {
        this.path_info.put(rota_ad,new_path_info.get_path_info());
    }

    public HashMap<String, Object> getPath_info() {
        return path_info;
    }
}
