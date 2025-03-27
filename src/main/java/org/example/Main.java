package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.Data.*;
import org.example.Data.DurakD.StopData;
import org.example.DijkstraAlghorithm.DijkstraPathFinder;
import org.example.Graph.BusGraphBuilder;
import org.example.Graph.GraphBuilder;
import org.example.DijkstraAlghorithm.PathFinder;
import org.example.Graph.IGraphBuilder;
import org.example.Mesafe.DistanceCalculator;
import org.example.Mesafe.HaversineDistanceCalculator;
import org.example.Passenger.PassengerManager;
import org.example.Payment.OdemeKontrol;
import org.example.Payment.PaymentManager;
import org.example.Rota.*;
import org.example.AlternativeRota.Taxi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.example.Graph.Graph;

import org.springframework.context.annotation.Bean;

import java.io.IOException;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public Rota rota() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNodeData jsonNodeData = new JsonNodeData(objectMapper);
        GraphDurakData graphDurakData = new GraphDurakData(objectMapper,jsonNodeData);

        StopData stopData = new StopData();
        stopData.set_stops(jsonNodeData);
        DistanceCalculator distanceCalculator = new HaversineDistanceCalculator();
        Durak durak = new Durak(graphDurakData, distanceCalculator);
        BusGraphBuilder busGraphBuilder = new BusGraphBuilder();
        IGraphBuilder graphBuilder = new GraphBuilder();
        Graph graph = graphBuilder.buildGraph(jsonNodeData.get_node_data());
        PathFinder pathFinder = new DijkstraPathFinder(graph);
        PathFinder pathFinder1 = new DijkstraPathFinder(busGraphBuilder.buildGraph(stopData));
        VehicleManager vehicleManager = new VehicleManager();
        TaxiData taxiData = new TaxiData(objectMapper);

        return new Rota(pathFinder1, vehicleManager, taxiData, durak);
    }

    @Bean
    public PassengerManager passengerManager(){
        return new PassengerManager();
    }

    @Bean
    public PaymentManager paymentManager(){
        return new PaymentManager();
    }

    @Bean
    public DistanceCalculator distanceCalculator(){
        return new HaversineDistanceCalculator();
    }

    @Bean
    public DurakData durak_data() throws IOException {
        return new DurakData();
    }

    @Bean
    public OdemeKontrol odemeKontrol(){
        return new OdemeKontrol();
    }
    @Bean
    public DefaultData data() throws IOException {
        return new DefaultData();
    }
    @Bean
    public TaxiData taxiData() throws IOException {
        return new TaxiData(new ObjectMapper());
    }
    @Bean
    public Taxi taxi(TaxiData taxiData) {
        return new Taxi(taxiData);
    }

}

