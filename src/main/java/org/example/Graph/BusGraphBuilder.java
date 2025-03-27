package org.example.Graph;

import org.example.Data.DurakD.Stop;
import org.example.Data.DurakD.StopData;
import org.example.DijkstraAlghorithm.Coordinate;

public class BusGraphBuilder{
    public Graph buildGraph(StopData stopData) {
        Graph graph = new Graph(false,true);

        for (Stop stop : stopData.getStops().values()) {

            if(stop.getType().equals("bus")) {
                Coordinate coordinate = stop.getCoordinate();
                graph.addNode(stop.getId(), Double.parseDouble(coordinate.lat()), Double.parseDouble(coordinate.lon()));
            }
        }


        for (Stop stop : stopData.getStops().values()) {
            String stopId = stop.getId();
            Node stopNode = graph.getNode(stopId);

            if (stop.getType().equals("bus") && !stop.getNextStops().isEmpty()) {
                System.out.println();
                for (Stop nextStop : stop.getNextStops()) {
                    System.out.println("b");
                    System.out.println(nextStop.getMesafe());
                    System.out.println(stop.getId());
                    System.out.println(nextStop.getId());
                    String targetStopId = nextStop.getId();
                    Node targetStopNode = graph.getNode(targetStopId);
                    Double mesafe = stop.getMesafe().get(nextStop);
                    Double sure = stop.getSure().get(nextStop);
                    Double ucret = stop.getUcret().get(nextStop);
                    String type = stop.getType();
                    graph.addEdge(stopNode,targetStopNode, mesafe,sure,ucret,type);
                }
            }

        }
        System.out.println("bus_graph" + graph.getNodes());
        for (Node node: graph.getNodes()){
            System.out.println("1");
            System.out.println(node.get_name());
        }

        for (Edge edge: graph.edgeSet()){
            System.out.println("1");
            System.out.println(edge.getStart().get_name() + "  --  " + edge.getEnd().get_name());
        }

        return graph;
    }
}
