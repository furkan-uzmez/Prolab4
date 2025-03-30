package org.example.DijkstraAlghorithm;

import org.example.Graph.Edge;
import org.example.Graph.Node;
import org.example.Graph.Graph;
import org.example.Rota.RotaInfo;
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
    public void findBestPath(RotaInfo path_info, String startId, String stopId, String optimization) {
        weight_turu.setWeight_turu(optimization);
        WeightStrategy strategy = weight_turu.getWeight_turu();
        System.out.println(strategy);
        for (Edge edge : graph.edgeSet()) {
            edge.setWeight_strategy(strategy.calculateWeight(edge));
        }

        System.out.println("Start ID: " + startId + ", Stop ID: " + stopId);
        DijkstraA dijkstraA = new DijkstraA();
        ArrayList<Node> path = dijkstraA.shortest_path(graph,graph.getNode(startId), graph.getNode(stopId));

        add_info(path_info,path);
    }
    private void add_info(RotaInfo path_info,ArrayList<Node> path) {
        double totalDistance = path_info.getToplam_mesafe();
        double totalTime = path_info.getToplam_sure();
        double totalCost = path_info.getToplam_ucret();

        List<Coordinate> coordinates = path_info.getCoordinates();

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

        path_info.setToplam_mesafe(totalDistance);
        path_info.setToplam_ucret(totalCost);
        path_info.setToplam_sure(totalTime);

        path_info.addPath_info();
    }
}