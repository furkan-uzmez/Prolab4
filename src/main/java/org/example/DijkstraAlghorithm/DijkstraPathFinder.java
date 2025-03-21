package org.example.DijkstraAlghorithm;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.Graph.Edge;
import org.example.Graph.Node;
import org.example.Graph.Graph;
import org.example.WeightStrategy.CostWeightStrategy;
import org.example.WeightStrategy.DistanceWeightStrategy;
import org.example.WeightStrategy.TimeWeightStrategy;
import org.example.WeightStrategy.WeightStrategy;

import java.util.*;

public class DijkstraPathFinder implements PathFinder {
    private final Graph graph;
    private final Map<String, WeightStrategy> strategies;
    //private final Map<String, JsonNode> duraklar;

    public DijkstraPathFinder(Graph graph) {
        this.graph = graph;
        //this.duraklar = duraklar;
        this.strategies = new HashMap<>();
        strategies.put("sure", new TimeWeightStrategy());
        strategies.put("mesafe", new DistanceWeightStrategy());
        strategies.put("ucret", new CostWeightStrategy());
    }

    @Override
    public HashMap<String,Object> findBestPath(String startId, String stopId, String optimization) {
        WeightStrategy strategy = strategies.getOrDefault(optimization, new TimeWeightStrategy());
        System.out.println(strategy);
        for (Edge edge : graph.edgeSet()) {
            edge.setWeight_strategy(strategy.calculateWeight(edge));
        }


        System.out.println("Start ID: " + startId + ", Stop ID: " + stopId);

        DijkstraA dijkstraA = new DijkstraA();
        ArrayList<Node> path = dijkstraA.shortest_path(graph,graph.getNode(startId), graph.getNode(stopId));


        double totalDistance = 0.0;
        double totalTime = 0.0;
        double totalCost = 0.0;

        HashMap<String,Object> path_info = new HashMap<>();
        List<Coordinate> vertices = new ArrayList<>();

        for (int i=0; i < path.size()-1;i++) {
            Node node = path.get(i);
            for(Edge edge : node.get_edges()){
                if(edge.getEnd().equals(path.get(i+1))){
                    totalDistance += edge.getMesafe();
                    totalTime += edge.getSure();
                    totalCost += edge.getUcret();
                }
            }
            vertices.add(node.getCoordinates());
        }
        vertices.add(path.get(path.size()-1).getCoordinates());

        path_info.put("toplam_mesafe_km", totalDistance);
        path_info.put("toplam_ucret", totalCost);
        path_info.put("toplam_sure_dk", totalTime);
        path_info.put("ara_koordinatlar",vertices);

        return path_info;
    }
}