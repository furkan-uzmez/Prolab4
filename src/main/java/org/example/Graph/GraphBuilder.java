package org.example.Graph;

import com.fasterxml.jackson.databind.JsonNode;

public class GraphBuilder implements IGraphBuilder {

    public Graph buildGraph(JsonNode data) {
        Graph graph = new Graph(false,true);

        for (JsonNode stop : data.get("duraklar")) {
            graph.addNode(stop.get("id").asText(),stop.get("lat").asDouble(),stop.get("lon").asDouble());
        }

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
                    graph.addEdge(stopNode,targetStopNode, mesafe,sure,ucret,type);
                }
            }

            if (stop.has("transfer") && stop.get("transfer").hasNonNull("transferStopId")){
                String transferStopId = stop.get("transfer").get("transferStopId").asText();
                Node transferStopNode = graph.getNode(transferStopId);
                        Double mesafe = 0.0;
                        Double sure = stop.get("transfer").get("transferSure").asDouble();
                        Double ucret = stop.get("transfer").get("transferUcret").asDouble();
                        String type = "transfer";
                graph.addEdge(stopNode,transferStopNode, mesafe,sure,ucret,type);
            }
        }

        return graph;
    }
}