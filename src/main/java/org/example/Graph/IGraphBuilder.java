package org.example.Graph;

import com.fasterxml.jackson.databind.JsonNode;


public interface IGraphBuilder {
    Graph buildGraph(JsonNode data);
}
