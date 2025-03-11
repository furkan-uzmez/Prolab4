package org.example;

import org.example.Data.Data;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {
    @Value("${google.maps.api.key:default-key}")
    private String googleMapsApiKey;

    private final Data data;

    public WebController(Data data) {
        this.data = data;
    }

    @GetMapping("/")
    public String home(Model model) {

        try {
            JSONObject jsonData = data.get_data();

            // Assuming veriseti.json contains the stops (duraklar)
            // Pass the JSON data directly to the model
            model.addAttribute("duraklar", jsonData.toString());

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
}