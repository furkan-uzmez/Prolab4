package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.io.IOException;

@SpringBootApplication
public class Main {
    public static void main(String[] args) throws IOException {
        //Data data = new Data();
        //JSONObject jsonData = data.get_data();

        SpringApplication.run(Main.class, args);
    }
}
