package org.example.IRota;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.Rota.Rota;
import org.jgrapht.Graph;

public interface GraphBuilder {
    Graph<String, Rota.KenarOzellikleri> buildGraph(JsonNode data);
}
