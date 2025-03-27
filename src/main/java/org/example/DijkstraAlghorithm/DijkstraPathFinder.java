package org.example.DijkstraAlghorithm;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.Graph.Edge;
import org.example.Graph.Node;
import org.example.Graph.Graph;
import org.example.WeightStrategy.*;

import java.util.*;

public class DijkstraPathFinder implements PathFinder {
    private final Graph graph;
    private WeightStrategyManager weight_turu;

    public DijkstraPathFinder(Graph graph) {
        this.graph = graph;
        this.weight_turu = new WeightStrategyManager();
    }

    @Override
    public Map<String,Object> findBestPath(Map<String,Object> path_info,String startId, String stopId, String optimization) {
        weight_turu.setWeight_turu(optimization);
        WeightStrategy strategy = weight_turu.getWeight_turu();
        System.out.println(strategy);
        for (Edge edge : graph.edgeSet()) {
            edge.setWeight_strategy(strategy.calculateWeight(edge));
        }

        System.out.println("Start ID: " + startId + ", Stop ID: " + stopId);
        DijkstraA dijkstraA = new DijkstraA();
        ArrayList<Node> path = dijkstraA.shortest_path(graph,graph.getNode(startId), graph.getNode(stopId));

        path_info = add_info(path_info,path);

        return path_info;
    }
    private Map<String,Object> add_info(Map<String,Object> path_info,ArrayList<Node> path) {
        double totalDistance = (double) path_info.get("toplam_mesafe_km");
        double totalTime = (double) path_info.get("toplam_sure_dk");
        double totalCost = (double) path_info.get("toplam_ucret");

        List<Coordinate> coordinates = (List<Coordinate>) path_info.get("coordinates");

        for (int i=0; i < path.size()-1;i++) {
            Node node = path.get(i);
            for(Edge edge : node.get_edges()){
                if(edge.getEnd().equals(path.get(i+1))){
                    totalDistance += edge.getMesafe();
                    totalTime += edge.getSure();
                    totalCost += edge.getUcret();
                }
            }
            coordinates.add(node.getCoordinates());
        }
        coordinates.add(path.get(path.size()-1).getCoordinates());

        path_info.put("toplam_mesafe_km", totalDistance);
        path_info.put("toplam_ucret", totalCost);
        path_info.put("toplam_sure_dk", totalTime);

        return path_info;
    }
}