package org.example.Rota;

import org.example.IRota.PathFinder;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;

import java.util.*;

interface WeightStrategy {
    double calculateWeight(Rota.KenarOzellikleri edge);
}

class TimeWeightStrategy implements WeightStrategy {
    @Override
    public double calculateWeight(Rota.KenarOzellikleri edge) {
        return edge.getSure();
    }
}

class DistanceWeightStrategy implements WeightStrategy {
    @Override
    public double calculateWeight(Rota.KenarOzellikleri edge) {
        return edge.getMesafe();
    }
}

class CostWeightStrategy implements WeightStrategy {
    @Override
    public double calculateWeight(Rota.KenarOzellikleri edge) {
        return edge.getUcret();
    }
}

public class DijkstraPathFinder implements PathFinder {
    private final Graph<String, Rota.KenarOzellikleri> graph;
    private final Map<String, WeightStrategy> strategies;

    public DijkstraPathFinder(Graph<String, Rota.KenarOzellikleri> graph) {
        this.graph = graph;
        this.strategies = new HashMap<>();
        strategies.put("sure", new TimeWeightStrategy());
        strategies.put("mesafe", new DistanceWeightStrategy());
        strategies.put("ucret", new CostWeightStrategy());
    }

    @Override
    public List<Map<String, Object>> findBestPath(String startId, String stopId, String optimization) {
        WeightStrategy strategy = strategies.getOrDefault(optimization, new TimeWeightStrategy());
        for (Rota.KenarOzellikleri edge : graph.edgeSet()) {
            graph.setEdgeWeight(edge, strategy.calculateWeight(edge));
        }

        GraphPath<String, Rota.KenarOzellikleri> path = DijkstraShortestPath.findPathBetween(graph, startId, stopId);
        if (path == null || path.getVertexList().size() < 2) {
            return new ArrayList<>();
        }

        // Burada duraklar haritasına erişim eksik, bu yüzden bağımlılığı enjekte etmek gerekebilir
        // Şimdilik basit bir dönüş yapıyorum, gerçek uygulamada duraklar haritası gerekecek
        List<Map<String, Object>> segments = new ArrayList<>();
        List<String> vertices = path.getVertexList();

        for (int i = 0; i < vertices.size() - 1; i++) {
            String u = vertices.get(i);
            String v = vertices.get(i + 1);
            Rota.KenarOzellikleri edge = graph.getEdge(u, v);

            Map<String, Object> segment = new HashMap<>();
            segment.put("baslangic_durak", Map.of("id", u));
            segment.put("bitis_durak", Map.of("id", v));
            segment.put("mesafe", edge.getMesafe());
            segment.put("sure", edge.getSure());
            segment.put("ucret", edge.getUcret());
            segment.put("baglanti_tipi", edge.getBaglanti_tipi());
            segments.add(segment);
        }

        return segments;
    }
}