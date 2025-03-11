package org.example;

import org.example.Data.Data;
import org.json.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.io.IOException;

@SpringBootApplication
public class Main {
    public static void main(String[] args) throws IOException {
        Data data = new Data();
        JSONObject jsonData = data.get_data();

        /*Durak durak = new Durak();
        durak.setDurak_veri(jsonData.getJSONArray("duraklar"));*/

        SpringApplication.run(Main.class, args);
    }

    /*@Bean
    public static Data data() throws IOException {
        System.out.println("1");
        return new Data(); // Burada kendi nesneni oluşturabilirsin
    }

    public static Data getData() {
        return Main.data;
    }*/
}
