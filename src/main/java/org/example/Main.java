package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.Data.Data;
import org.example.Data.DurakData;
import org.example.Data.GraphDurakData;
import org.example.Data.TaxiData;
import org.example.DijkstraAlghorithm.DijkstraPathFinder;
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
        Data data = new Data();
        GraphDurakData graphDurakData = new GraphDurakData(data, new ObjectMapper());
        DistanceCalculator distanceCalculator = new HaversineDistanceCalculator();
        Durak durak = new Durak(graphDurakData, distanceCalculator);
        IGraphBuilder graphBuilder = new GraphBuilder();
        Graph graph = graphBuilder.buildGraph(new ObjectMapper().readTree(data.get_data()));
        PathFinder pathFinder = new DijkstraPathFinder(graph);
        VehicleManager vehicleManager = new VehicleManager();
        TaxiData taxiData = new TaxiData(new ObjectMapper().readTree(data.get_data()).get("taxi"));

        return new Rota(pathFinder, vehicleManager, taxiData, durak);
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

    @Bean
    public DistanceCalculator distanceCalculator(){
        return new HaversineDistanceCalculator();
    }

    @Bean
    public Data durak_data() throws IOException {
        return new Data();
    }

    @Bean
    public OdemeKontrol odemeKontrol(){
        return new OdemeKontrol();
    }
    @Bean
    public Data data() throws IOException {
        return new Data();
    }
    @Bean
    public TaxiData taxiData(Data data) throws IOException {
        return new TaxiData(new ObjectMapper().readTree(data.get_data()).get("taxi"));
    }
    @Bean
    public Taxi taxi(TaxiData taxiData) {
        return new Taxi(taxiData);
    }

}

