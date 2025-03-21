package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.Data.Data;
import org.example.Data.DurakData;
import org.example.Mesafe.DistanceCalculator;
import org.example.Mesafe.HaversineDistanceCalculator;
import org.example.Passenger.PassengerManager;
import org.example.Passenger.Yolcu;
import org.example.Payment.PaymentManager;
import org.example.Rota.*;
import org.example.Vehicle.Arac;
import org.example.Vehicle.Taxi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

@Controller
public class WebController {
    @Value("${google.maps.api.key:default-key}")
    private String googleMapsApiKey;

    private final DurakData durak_data;
    private final Rota rota;
    private PassengerManager passengerManager;
    private PaymentManager paymentManager;
    private double baslangicEnlem;
    private double baslangicBoylam;
    private double hedefEnlem;
    private double hedefBoylam;
    private String odemeYontemi;
    private String yolcuTuru;
    private double bakiye;


    @Autowired
    public WebController(DurakData durak_data, Rota rota,PassengerManager passengerManager,PaymentManager paymentManager) {
        this.durak_data = durak_data;
        this.rota = rota;
        this.passengerManager = passengerManager;
        this.paymentManager = paymentManager;
    }

    @GetMapping("/")
    public String home(Model model) {
        try {
            model.addAttribute("duraklar", durak_data.getDuraklar().toString());
            model.addAttribute("googleMapsApiKey", googleMapsApiKey);
            model.addAttribute("mesaj", "Spring Boot HTML bağlantısı başarılı!");
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

        System.out.println("Yolcu nesnesi : " + passengerManager.get_yolcu());
        System.out.println("Odeme nesnesi : " + paymentManager.getOdeme_turu());

        // Taksi alternatifi (her zaman oluşturuluyor)
        DistanceCalculator distanceCalculator = new HaversineDistanceCalculator();
        double taxiDistance = distanceCalculator.calculateDistance(baslangicEnlem, baslangicBoylam, hedefEnlem, hedefBoylam);
        Data data = new Data();
        Taxi taksi = new Taxi(new ObjectMapper().readTree(data.get_data()).get("taxi"));
        Map<String, Object> taxiAlternative = taksi.createAlternative(baslangicEnlem, baslangicBoylam, hedefEnlem, hedefBoylam, taxiDistance);

        Map rotalar = new HashMap<>();

        rotalar.put("1",rota.findRouteWithCoordinates(baslangicEnlem, baslangicBoylam, hedefEnlem, hedefBoylam, "ucret"));
        rotalar.put("2",rota.findRouteWithCoordinates(baslangicEnlem, baslangicBoylam, hedefEnlem, hedefBoylam, "sure"));
        rotalar.put("3",rota.findRouteWithCoordinates(baslangicEnlem, baslangicBoylam, hedefEnlem, hedefBoylam, "mesafe"));
        rotalar.put("taxi",taxiAlternative);

        System.out.println(rotalar);

        return rotalar;
    }


}
