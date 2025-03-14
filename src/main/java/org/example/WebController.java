package org.example;

import org.example.Data.DurakData;
import org.example.Rota.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

@Controller
public class WebController {
    @Value("${google.maps.api.key:default-key}")
    private String googleMapsApiKey;

    private final DurakData durak_data;
    private final Rota rota;

    @Autowired
    public WebController(DurakData durak_data, Rota rota) {
        this.durak_data = durak_data;
        this.rota = rota;
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

    @PostMapping(value = "/", produces = "application/json")
    @ResponseBody
    public Map<String, Object> handlePostRequest(
            @RequestParam("baslangic_enlem") double baslangicEnlem,
            @RequestParam("baslangic_boylam") double baslangicBoylam,
            @RequestParam("hedef_enlem") double hedefEnlem,
            @RequestParam("hedef_boylam") double hedefBoylam,
            Model model) {

        return rota.findRouteWithCoordinates(baslangicEnlem, baslangicBoylam, hedefEnlem, hedefBoylam, "sure");
    }
}
