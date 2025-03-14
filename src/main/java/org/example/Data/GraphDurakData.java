package org.example.Data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;

public class GraphDurakData {
    private String veri;
    private ObjectMapper objectMapper;
    private HashMap<String, JsonNode> duraklar;


    public GraphDurakData(Data data, ObjectMapper objectMapper) throws JsonProcessingException {
        this.veri = data.get_data();
        this.objectMapper = objectMapper;
        this.set_hashmap_duraklar();
    }

    public void set_hashmap_duraklar() throws JsonProcessingException {
        JsonNode jsonData = this.get_node_data();
        this.duraklar =  new HashMap<>();
        for (JsonNode durak : jsonData.get("duraklar")) {
            duraklar.put(durak.get("id").asText(), durak);
        }
    }

    public HashMap<String,JsonNode> get_hashmap_duraklar(){
        return this.duraklar;
    }

    public JsonNode get_node_data() throws JsonProcessingException {
        return this.objectMapper.readTree(this.veri);
    }


}
