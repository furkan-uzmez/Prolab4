package org.example.Data.DurakD;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.example.Data.JsonNodeData;
import org.example.DijkstraAlghorithm.Coordinate;

import java.util.ArrayList;
import java.util.HashMap;


public class StopData {
    private HashMap<String,Stop> stops;
    private ArrayList<Transfer> transfers;

    public void set_stops(JsonNodeData jsonNodeData) throws JsonProcessingException {
        JsonNode jsonData = jsonNodeData.get_node_data();
        stops = new HashMap<>();
        transfers = new ArrayList<>();

        for(JsonNode durak : jsonData.get("duraklar")){
            if(durak.get("type").asText().equals("bus")){
                Stop bus_stop = new BusStop();
                set_stop_info(bus_stop,durak);
                stops.put(bus_stop.getId(),bus_stop);
            }
            else if(durak.get("type").asText().equals("tram")){
                Stop tram_stop = new TramStop();
                set_stop_info(tram_stop,durak);
                stops.put(tram_stop.getId(),tram_stop);
            }
        }

        System.out.println(stops);
        for(JsonNode durak : jsonData.get("duraklar")){
            Stop stop = stops.get(durak.get("id").asText());
            System.out.println(stop.getId());
            System.out.println(durak.get("nextStops"));
            for (JsonNode next_stop_json : durak.get("nextStops")){
                Stop next_stop = stops.get(next_stop_json.get("stopId").asText());
                System.out.println(next_stop);
                stop.getNextStops().add(next_stop);
                stop.add_mesafe(next_stop,next_stop_json.get("mesafe").asDouble());
                stop.add_sure(next_stop,next_stop_json.get("sure").asDouble());
                stop.add_ucret(next_stop,next_stop_json.get("ucret").asDouble());
            }
        }

        for(JsonNode durak : jsonData.get("duraklar")){
            if(!durak.get("transfer").isNull()){
                JsonNode transfer_stop = durak.get("transfer");
                Transfer transfer = new BusTramTransfer();
                transfers.add(transfer);
                transfer.setStation1(stops.get(durak.get("id").asText()));
                transfer.setStation2(stops.get(transfer_stop.get("transferStopId").asText()));
                transfer.setTransfer_sure(transfer_stop.get("transferSure").asDouble());
                transfer.setTransfer_ucret(transfer_stop.get("transferUcret").asDouble());

            }
        }


    }

    private void set_stop_info(Stop stop,JsonNode durak){
        stop.setId(durak.get("id").asText());
        stop.setType(durak.get("type").asText());
        stop.setCoordinate(new Coordinate(durak.get("lat").asText(),durak.get("lon").asText()));
    }

    public HashMap<String,Stop> getStops() {
        return stops;
    }

    public ArrayList<Transfer> getTransfers() {
        return transfers;
    }

}
