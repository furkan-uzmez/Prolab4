package org.example.Graph;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.Data.DurakD.StopData;


public interface IGraphBuilder {
    Graph buildGraph(StopData stopData);
    String get_name();
}
