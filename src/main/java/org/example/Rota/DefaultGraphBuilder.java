package org.example.Rota;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.Rota.Rota;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.example.IRota.GraphBuilder;

public class DefaultGraphBuilder implements GraphBuilder {

    public Graph<String, EdgeFeatures> buildGraph(JsonNode data) {
        Graph<String, EdgeFeatures> graph = new DefaultDirectedWeightedGraph<>(EdgeFeatures.class);

        for (JsonNode stop : data.get("duraklar")) {
            graph.addVertex(stop.get("id").asText());
        }

        for (JsonNode stop : data.get("duraklar")) {
            String stopId = stop.get("id").asText();

            if (stop.has("nextStops")) {
                for (JsonNode nextStop : stop.get("nextStops")) {
                    String targetStopId = nextStop.get("stopId").asText();
                    EdgeFeatures edge = new EdgeFeatures(
                            nextStop.get("mesafe").asDouble(),
                            nextStop.get("sure").asDouble(),
                            nextStop.get("ucret").asDouble(),
                            "direct",
                            stop.get("type").asText()
                    );
                    graph.addEdge(stopId, targetStopId, edge);
                    graph.setEdgeWeight(edge, nextStop.get("sure").asDouble());
                }
            }

            if (stop.has("transfer") && stop.get("transfer").hasNonNull("transferStopId")){
                String transferStopId = stop.get("transfer").get("transferStopId").asText();
                EdgeFeatures edge = new EdgeFeatures(
                        0.1,
                        stop.get("transfer").get("transferSure").asDouble(),
                        stop.get("transfer").get("transferUcret").asDouble(),
                        "transfer",
                        "transfer"
                );
                graph.addEdge(stopId, transferStopId, edge);
                graph.setEdgeWeight(edge, stop.get("transfer").get("transferSure").asDouble());
            }
        }

        return graph;
    }
}