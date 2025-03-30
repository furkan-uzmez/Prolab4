package org.example.AlternativeRota;

import org.example.DijkstraAlghorithm.Coordinate;
import org.example.Konum;
import org.example.Rota.RotaInfo;

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

    abstract RotaInfo createAlternative(Konum konum,
                                        double distance);
}
