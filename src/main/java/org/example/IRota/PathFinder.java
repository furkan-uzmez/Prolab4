package org.example.IRota;

import java.util.List;
import java.util.Map;

// Yol bulucu arayüzü
public interface PathFinder {
    List<Map<String, Object>> findBestPath(String startId, String stopId, String optimization);
}