package org.example.Rota;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.IRota.PathFinder;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;

import java.util.*;

interface WeightStrategy {
    double calculateWeight(EdgeFeatures edge);
}

class TimeWeightStrategy implements WeightStrategy {
    @Override
    public double calculateWeight(EdgeFeatures edge) {
        return edge.getSure();
    }
}

class DistanceWeightStrategy implements WeightStrategy {
    @Override
    public double calculateWeight(EdgeFeatures edge) {
        return edge.getMesafe();
    }
}

class CostWeightStrategy implements WeightStrategy {
    @Override
    public double calculateWeight(EdgeFeatures edge) {
        return edge.getUcret();
    }
}

public class DijkstraPathFinder implements PathFinder {
    private final Graph<String, EdgeFeatures> graph;
    private final Map<String, WeightStrategy> strategies;
    private final Map<String, JsonNode> duraklar;

    public DijkstraPathFinder(Graph<String,EdgeFeatures> graph,Map<String, JsonNode> duraklar) {
        this.graph = graph;
        this.duraklar = duraklar;
        this.strategies = new HashMap<>();
        strategies.put("sure", new TimeWeightStrategy());
        strategies.put("mesafe", new DistanceWeightStrategy());
        strategies.put("ucret", new CostWeightStrategy());
    }

    @Override
    public List<Map<String, Object>> findBestPath(String startId, String stopId, String optimization) {
        WeightStrategy strategy = strategies.getOrDefault(optimization, new TimeWeightStrategy());
        for (EdgeFeatures edge : graph.edgeSet()) {
            graph.setEdgeWeight(edge, strategy.calculateWeight(edge));
        }

        System.out.println("Graph vertices: " + graph.vertexSet());
        System.out.println("Graph edges: " + graph.edgeSet());
        System.out.println("Start ID: " + startId + ", Stop ID: " + stopId);

        GraphPath<String, EdgeFeatures> path = DijkstraShortestPath.findPathBetween(graph, startId, stopId);
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
            EdgeFeatures edge = graph.getEdge(u, v);

            // Durak bilgilerini duraklar haritasından al
            JsonNode startStop = duraklar.get(u);
            JsonNode endStop = duraklar.get(v);

            Map<String, Object> startDurak = new HashMap<>();
            startDurak.put("id", u);
            if (startStop != null) {
                startDurak.put("lat", startStop.get("lat").asDouble());
                startDurak.put("lon", startStop.get("lon").asDouble());
                startDurak.put("name", startStop.get("name").asText());
                startDurak.put("type", startStop.get("type").asText());
            }

            Map<String, Object> endDurak = new HashMap<>();
            endDurak.put("id", v);
            if (endStop != null) {
                endDurak.put("lat", endStop.get("lat").asDouble());
                endDurak.put("lon", endStop.get("lon").asDouble());
                endDurak.put("name", endStop.get("name").asText());
                endDurak.put("type", endStop.get("type").asText());
            }

            Map<String, Object> segment = new HashMap<>();
            segment.put("baslangic_durak", startDurak);
            segment.put("bitis_durak", endDurak);
            segment.put("mesafe", edge.getMesafe());
            segment.put("sure", edge.getSure());
            segment.put("ucret", edge.getUcret());
            segment.put("baglanti_tipi", edge.getBaglanti_tipi());
            segments.add(segment);
        }
        /*
        Map<String, Object> yurume_segment = new HashMap<>();
        yurume_segment.put("baslangic_durak", startDurak);
        yurume_segment.put("bitis_durak", "hedef_nokta");
        yurume_segment.put("baglanti_tipi", edge.getBaglanti_tipi());
        segments.add(yurume_segment);
        */
        return segments;
    }
}