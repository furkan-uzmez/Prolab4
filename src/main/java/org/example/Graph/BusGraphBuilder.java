package org.example.Graph;

import org.example.Data.DurakD.Stop;
import org.example.Data.DurakD.StopData;
import org.example.DijkstraAlghorithm.Coordinate;

public class BusGraphBuilder implements IGraphBuilder{
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
                for (Stop nextStop : stop.getNextStops()) {
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

        return graph;
    }

    public String get_name(){
        return "bus";
    }
}
