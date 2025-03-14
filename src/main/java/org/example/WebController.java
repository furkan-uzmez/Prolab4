package org.example;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.Data.GraphDurakData;
import org.example.IRota.*;
import org.example.Data.Data;
import org.example.Data.DurakData;
import org.example.Mesafe.DistanceCalculator;
import org.example.Mesafe.HaversineDistanceCalculator;
import org.example.Rota.*;
import org.example.Vehicle.Taxi;
import org.jgrapht.Graph;
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

@Controller
public class WebController {
    @Value("${google.maps.api.key:default-key}")
    private String googleMapsApiKey;

    private final DurakData durak_data;
    private final Rota rota;

    public WebController() throws IOException {
        Data data = new Data();
        this.durak_data = new DurakData(data);
        GraphDurakData graph_durak_data = new GraphDurakData(data,new ObjectMapper());

        DistanceCalculator distanceCalculator = new HaversineDistanceCalculator();
        Durak durak = new Durak(graph_durak_data,distanceCalculator);

        GraphBuilder graphBuilder = new DefaultGraphBuilder();
        Graph<String, Rota.KenarOzellikleri> graph = graphBuilder.buildGraph(new ObjectMapper().readTree(data.get_data()));
        PathFinder pathFinder = new DijkstraPathFinder(graph,graph_durak_data.get_hashmap_duraklar());
        WaypointGenerator waypointGenerator = new DefaultWaypointGenerator(new ObjectMapper(), new HashMap<>());
        RoutePrinter routePrinter = new ConsoleRoutePrinter("Izmit");
        Taxi taksi = new Taxi(new ObjectMapper().readTree(data.get_data()).get("taxi"));

        this.rota = new Rota( graphBuilder, pathFinder, waypointGenerator,
                routePrinter, distanceCalculator, taksi,durak,graph_durak_data);
    }

    @GetMapping("/")
    public String home(Model model) {
        try {
            // Pass the JSON data directly to the model
            model.addAttribute("duraklar", durak_data.getDuraklar().toString());

            // Default coordinates (replace with actual values if needed)
            model.addAttribute("baslangic_enlem", 40.7669);
            model.addAttribute("baslangic_boylam", 29.9169);
            model.addAttribute("hedef_enlem", 40.7669);
            model.addAttribute("hedef_boylam", 29.9169);
            model.addAttribute("nearest_stop", null); // Replace with actual nearest stop if calculated
            model.addAttribute("googleMapsApiKey", googleMapsApiKey);

            model.addAttribute("mesaj", "Spring Boot HTML bağlantısı başarılı!");
        } catch (Exception e) {
            model.addAttribute("mesaj", "Error loading JSON: " + e.getMessage());
        }
        return "index";
    }

    // POST isteğini destekler
    @PostMapping(value = "/", produces = "application/json")
    @ResponseBody
    public Map<String, Object> handlePostRequest(
            @RequestParam("baslangic_enlem") double baslangicEnlem,
            @RequestParam("baslangic_boylam") double baslangicBoylam,
            @RequestParam("hedef_enlem") double hedefEnlem,
            @RequestParam("hedef_boylam") double hedefBoylam,
            Model model) {

        Map<String, Object> rota_koordinatlar = rota.findRouteWithCoordinates(baslangicEnlem,baslangicBoylam,hedefEnlem,hedefBoylam,"sure");
        System.out.println("Tam rota: " + rota_koordinatlar);
        System.out.println("Waypoints: " + rota_koordinatlar.get("waypoints"));
        System.out.println("Bitiş durağı: " + rota_koordinatlar.get("bitis_durak"));

        return rota_koordinatlar;
    }
}