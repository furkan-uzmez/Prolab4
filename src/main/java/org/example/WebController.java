package org.example;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {
    @GetMapping("/")  // http://localhost:8080/ çağrıldığında index.html açılır
    public String home(Model model) {
        model.addAttribute("mesaj", "Spring Boot HTML bağlantısı başarılı!");
        return "index"; // templates/index.html çağrılır
    }
}
