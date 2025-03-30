package org.example.DijkstraAlghorithm;

import org.example.Rota.RotaInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Yol bulucu arayüzü
public interface PathFinder {
    void findBestPath(RotaInfo path_info, String startId, String stopId, String optimization);
}