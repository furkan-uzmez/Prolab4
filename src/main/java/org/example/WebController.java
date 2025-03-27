package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.Data.*;
import org.example.Data.DurakD.StopData;
import org.example.DijkstraAlghorithm.DijkstraPathFinder;
import org.example.DijkstraAlghorithm.PathFinder;
import org.example.Graph.BusGraphBuilder;
import org.example.Graph.BusTramGraphBuilder;
import org.example.Graph.IGraphBuilder;
import org.example.Graph.TramGraphBuilder;
import org.example.Mesafe.DistanceCalculator;
import org.example.Mesafe.HaversineDistanceCalculator;
import org.example.Passenger.PassengerManager;
import org.example.Passenger.Yolcu;
import org.example.Payment.Odeme;
import org.example.Payment.OdemeKontrol;
import org.example.Payment.PaymentManager;
import org.example.Rota.*;
import org.example.AlternativeRota.Taxi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

@Controller
public class WebController {
    @Value("${google.maps.api.key:default-key}")
    private String googleMapsApiKey;

    private final DurakData durak_data;
    private final Rota rota1;
    private final Rota rota2;
    private final Rota rota3;
    private PassengerManager passengerManager;
    private PaymentManager paymentManager;
    private double baslangicEnlem;
    private double baslangicBoylam;
    private double hedefEnlem;
    private double hedefBoylam;
    private String odemeYontemi;
    private String yolcuTuru;
    private double bakiye;
    private DistanceCalculator distanceCalculator;
    private DefaultData data;
    private OdemeKontrol odemeKontrol;
    private final TaxiData taxiData;
    private final Taxi taxi;
    private final ObjectMapper objectMapper;
    private final JsonNodeData jsonNodeData;
    private final GraphDurakData graphDurakData;
    private final StopData stopData;
    private final Durak durak;

    @Autowired
    public WebController(DurakData durak_data, Rota rota, PassengerManager passengerManager,
                         PaymentManager paymentManager, DistanceCalculator distanceCalculator,
                         DefaultData data, OdemeKontrol odemeKontrol, TaxiData taxiData,
                         Taxi taxi, ObjectMapper objectMapper, JsonNodeData jsonNodeData,
                         GraphDurakData graphDurakData,StopData stopData,Durak durak
                         ) throws JsonProcessingException {
        System.out.println("1");
        this.objectMapper = objectMapper;
        this.jsonNodeData = jsonNodeData;
        this.graphDurakData =  graphDurakData;
        System.out.println("2");

        this.stopData = stopData;
        stopData.set_stops(jsonNodeData);
        this.distanceCalculator = distanceCalculator;
        this.durak = durak;
        System.out.println("3");

        IGraphBuilder busGraphBuilder = new BusGraphBuilder();
        IGraphBuilder tramGraphBuilder = new TramGraphBuilder();
        IGraphBuilder busTramGraphBuilder = new BusTramGraphBuilder();
        System.out.println("4");


        PathFinder pathFinder1 = new DijkstraPathFinder(busGraphBuilder.buildGraph(stopData));
        PathFinder pathFinder2 = new DijkstraPathFinder(tramGraphBuilder.buildGraph(stopData));
        PathFinder pathFinder3 = new DijkstraPathFinder(busTramGraphBuilder.buildGraph(stopData));
        System.out.println("5");

        VehicleManager vehicleManager = new VehicleManager();

        this.durak_data = durak_data;
        //this.rota = rota;
        this.passengerManager = passengerManager;
        this.paymentManager = paymentManager;
        System.out.println("6");

        this.data = data;
        this.odemeKontrol = odemeKontrol;
        this.taxiData = taxiData;
        this.taxi = taxi;
        System.out.println("7");

        this.rota1 = new Rota(pathFinder1,vehicleManager,taxiData,new Durak(graphDurakData,distanceCalculator,busGraphBuilder.get_name()));
        this.rota2 = new Rota(pathFinder2,vehicleManager,taxiData,new Durak(graphDurakData,distanceCalculator,tramGraphBuilder.get_name()));
        this.rota3 = new Rota(pathFinder3,vehicleManager,taxiData,new Durak(graphDurakData,distanceCalculator,busTramGraphBuilder.get_name()));
        System.out.println("8");
    }

    @GetMapping("/")
    public String home(Model model) {
        try {
            System.out.println("9");
            model.addAttribute("duraklar", durak_data.getDuraklar().toString());
            model.addAttribute("googleMapsApiKey", googleMapsApiKey);
            model.addAttribute("mesaj", "Spring Boot HTML bağlantısı başarılı!");
            System.out.println("10");
        } catch (Exception e) {
            model.addAttribute("mesaj", "Error loading JSON: " + e.getMessage());
        }
        return "index";
    }


    @PostMapping(value = "/")
    @ResponseBody
    public Map handlePostRequest(
            @RequestParam("baslangic_enlem") double baslangicEnlem,
            @RequestParam("baslangic_boylam") double baslangicBoylam,
            @RequestParam("hedef_enlem") double hedefEnlem,
            @RequestParam("hedef_boylam") double hedefBoylam,
            @RequestParam("odeme_yontemi") String odemeYontemi,
            @RequestParam("yolcu_turu") String yolcuTuru,
            @RequestParam("bakiye") double bakiye
            ) throws IOException {
        System.out.println("odemeYontemi:"+odemeYontemi);
        System.out.println("yolcuTuru:"+yolcuTuru);
        System.out.println("bakiye:"+bakiye);


        this.baslangicEnlem = baslangicEnlem;
        this.baslangicBoylam = baslangicBoylam;
        this.hedefEnlem = hedefEnlem;
        this.hedefBoylam = hedefBoylam;
        this.odemeYontemi = odemeYontemi;
        this.yolcuTuru = yolcuTuru;
        this.bakiye = bakiye;

        return rotaCiz();
    }

    public Map rotaCiz() throws IOException {
        passengerManager.setYolcu(yolcuTuru);
        paymentManager.setOdeme_turu(odemeYontemi);
        Yolcu yolcu = passengerManager.get_yolcu();
        Odeme odeme = paymentManager.getOdeme_turu();
        System.out.println("Yolcu nesnesi : " + yolcu);
        System.out.println("Odeme nesnesi : " + odeme);

        double taxiDistance = distanceCalculator.calculateDistance(baslangicEnlem, baslangicBoylam, hedefEnlem, hedefBoylam);
        Map<String, Object> taxiAlternative = taxi.createAlternative(baslangicEnlem, baslangicBoylam,
                hedefEnlem, hedefBoylam, taxiDistance);

        Map rotalar = new HashMap<>();

        rotalar.put("1",rota1.findRouteWithCoordinates(baslangicEnlem, baslangicBoylam, hedefEnlem, hedefBoylam, "ucret"));
        rotalar.put("2",rota1.findRouteWithCoordinates(baslangicEnlem, baslangicBoylam, hedefEnlem, hedefBoylam, "sure"));
        rotalar.put("3",rota1.findRouteWithCoordinates(baslangicEnlem, baslangicBoylam, hedefEnlem, hedefBoylam, "mesafe"));
        rotalar.put("taxi",taxiAlternative);

        odemeKontrol.kontrol(rotalar,odeme,yolcu,bakiye);

        System.out.println(rotalar);

        return rotalar;
    }


}
