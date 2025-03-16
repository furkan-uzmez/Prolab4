package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.Data.Data;
import org.example.Data.DurakData;
import org.example.Data.GraphDurakData;
import org.example.DijkstraAlghorithm.DijkstraPathFinder;
import org.example.Graph.GraphBuilder;
import org.example.IRota.PathFinder;
import org.example.IRota.RoutePrinter;
import org.example.IRota.WaypointGenerator;
import org.example.Mesafe.DistanceCalculator;
import org.example.Mesafe.HaversineDistanceCalculator;
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
        //DurakData durakData = new DurakData(data);
        GraphDurakData graphDurakData = new GraphDurakData(data, new ObjectMapper());
        DistanceCalculator distanceCalculator = new HaversineDistanceCalculator();
        Durak durak = new Durak(graphDurakData, distanceCalculator);
        org.example.IRota.GraphBuilder graphBuilder = new GraphBuilder();
        Graph graph = graphBuilder.buildGraph(new ObjectMapper().readTree(data.get_data()));
        PathFinder pathFinder = new DijkstraPathFinder(graph, graphDurakData.get_hashmap_duraklar());
        WaypointGenerator waypointGenerator = new DefaultWaypointGenerator(new ObjectMapper(), new HashMap<>());
        RoutePrinter routePrinter = new ConsoleRoutePrinter("Izmit");
        Taxi taksi = new Taxi(new ObjectMapper().readTree(data.get_data()).get("taxi"));

        return new Rota(graph, pathFinder, waypointGenerator, routePrinter, distanceCalculator, taksi, durak, graphDurakData);
    }

    @Bean
    public DurakData durakData() throws IOException {
        return new DurakData(new Data());
    }
}

