package org.example.DijkstraAlghorithm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Yol bulucu arayüzü
public interface PathFinder {
    Map<String,Object> findBestPath(Map<String,Object> path_info, String startId, String stopId, String optimization);
}