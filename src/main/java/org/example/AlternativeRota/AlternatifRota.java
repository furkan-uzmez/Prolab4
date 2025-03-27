package org.example.AlternativeRota;

import org.example.DijkstraAlghorithm.Coordinate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AlternatifRota {
    protected List<Coordinate> createWaypoints(double startLat, double startLon,
                                                 double endLat, double endLon) {
        List<Coordinate> vertices = new ArrayList<>();
        vertices.add(new Coordinate(String.valueOf(startLat),String.valueOf(startLon)));
        vertices.add(new Coordinate(String.valueOf(endLat),String.valueOf(endLon)));

        return vertices;
    }

    abstract Map<String, Object> createAlternative(double startLat, double startLon,
                                          double endLat, double endLon,
                                          double distance);
}
