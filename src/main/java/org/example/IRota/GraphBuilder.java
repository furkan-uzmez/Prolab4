package org.example.IRota;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.Rota.EdgeFeatures;
import org.example.Rota.Rota;
import org.jgrapht.Graph;

public interface GraphBuilder {
    Graph<String, EdgeFeatures> buildGraph(JsonNode data);
}
