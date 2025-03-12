package org.example.Rota;

import java.util.List;
import java.util.Map;
import org.example.IRota.RoutePrinter;


public class ConsoleRoutePrinter implements RoutePrinter {
    private final String city;

    public ConsoleRoutePrinter(String city) {
        this.city = city;
    }

    @Override
    public void printRoute(Map<String, Object> result) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> route = (List<Map<String, Object>>) result.get("rota");

        System.out.println("\n" + city + " şehrinde rota bulundu!");

        Map<String, Double> startCoord = (Map<String, Double>) result.get("baslangic_koordinat");
        Map<String, Double> endCoord = (Map<String, Double>) result.get("hedef_koordinat");

        System.out.println("Başlangıç: (" + startCoord.get("lat") + ", " + startCoord.get("lon") + ")");
        System.out.println("Hedef: (" + endCoord.get("lat") + ", " + endCoord.get("lon") + ")");

        if ((boolean) result.get("rota_bulundu")) {
            System.out.println("\nÖnerilen Rota:");
            for (int i = 0; i < route.size(); i++) {
                Map<String, Object> segment = route.get(i);
                Map<String, Object> startStop = (Map<String, Object>) segment.get("baslangic_durak");
                Map<String, Object> endStop = (Map<String, Object>) segment.get("bitis_durak");

                String start = startStop.get("name").toString();
                String end = endStop.get("name").toString();
                double distance = (double) segment.get("mesafe");
                double time = (double) segment.get("sure");
                double cost = (double) segment.get("ucret");
                String connectionType = (String) segment.get("baglanti_tipi");

                if ("walking".equals(connectionType)) {
                    System.out.printf("  %d. %s'dan %s'a %.1f km yürüyün (yaklaşık %.1f dakika)%n",
                            i+1, start, end, distance, time);
                } else if ("transfer".equals(connectionType)) {
                    System.out.printf("  %d. %s'dan %s'a transfer yapın (yaklaşık %.1f dakika, %.2f TL)%n",
                            i+1, start, end, time, cost);
                } else {
                    System.out.printf("  %d. %s'dan %s'a %s ile gidin (yaklaşık %.1f dakika, %.2f TL)%n",
                            i+1, start, end, connectionType, time, cost);
                }
            }

            System.out.println("\nToplam:");
            System.out.printf("  Mesafe: %.1f km%n", (double) result.get("toplam_mesafe_km"));
            System.out.printf("  Süre: %.1f dakika%n", (double) result.get("toplam_sure_dk"));
            System.out.printf("  Ücret: %.2f TL%n", (double) result.get("toplam_ucret"));
        } else {
            System.out.println("\nToplu taşıma rotası bulunamadı.");
        }

        Map<String, Object> taxi = (Map<String, Object>) result.get("taksi_alternatifi");
        System.out.println("\nTaksi alternatifi:");
        System.out.printf("  Mesafe: %.1f km%n", (double) taxi.get("mesafe_km"));
        System.out.printf("  Tahmini süre: %.1f dakika%n", (double) taxi.get("tahmini_sure_dk"));
        System.out.printf("  Ücret: %.2f TL%n", (double) taxi.get("ucret"));
    }
}