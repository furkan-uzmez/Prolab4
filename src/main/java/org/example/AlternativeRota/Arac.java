package org.example.AlternativeRota;

import java.util.Map;

public interface Arac {
    Map<String, Object> createAlternative(double startLat, double startLon,
                                          double endLat, double endLon,
                                          double distance);
}
