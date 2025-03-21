package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.Data.Data;
import org.example.Data.DurakData;
import org.example.Data.GraphDurakData;
import org.example.DijkstraAlghorithm.DijkstraPathFinder;
import org.example.Graph.GraphBuilder;
import org.example.DijkstraAlghorithm.PathFinder;
import org.example.Vehicle.WaypointGenerator;
import org.example.Mesafe.DistanceCalculator;
import org.example.Mesafe.HaversineDistanceCalculator;
import org.example.Passenger.PassengerManager;
import org.example.Payment.PaymentManager;
import org.example.Rota.*;
import org.example.Vehicle.Taxi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.example.Graph.Graph;

import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.util.HashMap;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public Rota rota() throws IOException {
        Data data = new Data();
        GraphDurakData graphDurakData = new GraphDurakData(data, new ObjectMapper());
        DistanceCalculator distanceCalculator = new HaversineDistanceCalculator();
        Durak durak = new Durak(graphDurakData, distanceCalculator);
        GraphBuilder graphBuilder = new GraphBuilder();
        Graph graph = graphBuilder.buildGraph(new ObjectMapper().readTree(data.get_data()));
        PathFinder pathFinder = new DijkstraPathFinder(graph);
        WaypointGenerator waypointGenerator = new DefaultWaypointGenerator(new HashMap<>());
        Taxi taksi = new Taxi(new ObjectMapper().readTree(data.get_data()).get("taxi"));

        return new Rota(graph, pathFinder, waypointGenerator, distanceCalculator, taksi, durak, graphDurakData);
    }

    @Bean
    public DurakData durakData() throws IOException {
        return new DurakData(new Data());
    }

    @Bean
    public PassengerManager passengerManager(){
        return new PassengerManager();
    }

    @Bean
    public PaymentManager paymentManager(){
        return new PaymentManager();
    }


}

