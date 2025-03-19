package org.example.IRota;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.Graph.Graph;


public interface GraphBuilder {
    Graph buildGraph(JsonNode data);
}
