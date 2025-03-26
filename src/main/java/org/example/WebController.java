package org.example;

import org.example.Data.DefaultData;
import org.example.Data.DurakData;
import org.example.Data.TaxiData;
import org.example.Mesafe.DistanceCalculator;
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
    private DistanceCalculator distanceCalculator;
    private DefaultData data;
    private OdemeKontrol odemeKontrol;
    private final TaxiData taxiData;
    private final Taxi taxi;

    @Autowired
    public WebController(DurakData durak_data, Rota rota, PassengerManager passengerManager,
                         PaymentManager paymentManager, DistanceCalculator distanceCalculator,
                         DefaultData data, OdemeKontrol odemeKontrol, TaxiData taxiData,
                         Taxi taxi) {
        this.durak_data = durak_data;
        this.rota = rota;
        this.passengerManager = passengerManager;
        this.paymentManager = paymentManager;
        this.distanceCalculator = distanceCalculator;
        this.data = data;
        this.odemeKontrol = odemeKontrol;
        this.taxiData = taxiData;
        this.taxi = taxi;
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
        Yolcu yolcu = passengerManager.get_yolcu();
        Odeme odeme = paymentManager.getOdeme_turu();
        System.out.println("Yolcu nesnesi : " + yolcu);
        System.out.println("Odeme nesnesi : " + odeme);

        double taxiDistance = distanceCalculator.calculateDistance(baslangicEnlem, baslangicBoylam, hedefEnlem, hedefBoylam);
        Map<String, Object> taxiAlternative = taxi.createAlternative(baslangicEnlem, baslangicBoylam,
                hedefEnlem, hedefBoylam, taxiDistance);

        Map rotalar = new HashMap<>();

        rotalar.put("1",rota.findRouteWithCoordinates(baslangicEnlem, baslangicBoylam, hedefEnlem, hedefBoylam, "ucret"));
        rotalar.put("2",rota.findRouteWithCoordinates(baslangicEnlem, baslangicBoylam, hedefEnlem, hedefBoylam, "sure"));
        rotalar.put("3",rota.findRouteWithCoordinates(baslangicEnlem, baslangicBoylam, hedefEnlem, hedefBoylam, "mesafe"));
        rotalar.put("taxi",taxiAlternative);

        odemeKontrol.kontrol(rotalar,odeme,yolcu,bakiye);

        System.out.println(rotalar);

        return rotalar;
    }


}
