package org.example.DijkstraAlghorithm;

import java.util.HashMap;
import java.util.List;

// Yol bulucu arayüzü
public interface PathFinder {
    HashMap<String,Object> findBestPath(String startId, String stopId, String optimization);
}