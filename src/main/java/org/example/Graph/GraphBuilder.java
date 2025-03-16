package org.example.Graph;

import com.fasterxml.jackson.databind.JsonNode;

public class GraphBuilder implements org.example.IRota.GraphBuilder {

    public Graph buildGraph(JsonNode data) {
        Graph graph = new Graph(true,true);

        for (JsonNode stop : data.get("duraklar")) {
            //System.out.println("Node eklendi");
            graph.addNode(stop.get("id").asText());
        }
        //System.out.println(graph.getNodes().size());

        for (JsonNode stop : data.get("duraklar")) {
            String stopId = stop.get("id").asText();
            Node stopNode = graph.getNode(stopId);
            if (stop.has("nextStops")) {
                for (JsonNode nextStop : stop.get("nextStops")) {
                    String targetStopId = nextStop.get("stopId").asText();
                    Node targetStopNode = graph.getNode(targetStopId);
                    Double mesafe = nextStop.get("mesafe").asDouble();
                    Double sure = nextStop.get("sure").asDouble();
                    Double ucret = nextStop.get("ucret").asDouble();
                    String type = stop.get("type").asText();
                    //System.out.println("Edge eklendi");
                    graph.addEdge(stopNode,targetStopNode, mesafe,sure,ucret,type);}
                    //System.out.println(stopNode.get_edges());
            }

            if (stop.has("transfer") && stop.get("transfer").hasNonNull("transferStopId")){
                //System.out.println("Edge eklendi");
                String transferStopId = stop.get("transfer").get("transferStopId").asText();
                Node transferStopNode = graph.getNode(transferStopId);
                        Double mesafe = 0.0;
                        Double sure = stop.get("transfer").get("transferSure").asDouble();
                        Double ucret = stop.get("transfer").get("transferUcret").asDouble();
                        String type = "transfer";
                graph.addEdge(stopNode,transferStopNode, mesafe,sure,ucret,type);
                //System.out.println(stopNode.get_edges());
            }
        }
        //graph.printGraph();
        return graph;
    }
}