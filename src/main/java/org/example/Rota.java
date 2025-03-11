package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.*;
import java.util.stream.Collectors;

public class Rota {
    record Coordinate(double lat, double lon) {
        public double lat() { return lat; }
        public double lon() { return lon; }
    }
    private JsonNode data;
    private Map<String, JsonNode> duraklar;
    private String city;
    private JsonNode taxi;
    private Graph<String, KenarOzellikleri> G;
    private final ObjectMapper objectMapper; // Add ObjectMapper as a field


    public static class KenarOzellikleri extends DefaultWeightedEdge {
        private double mesafe;
        private double sure;
        private double ucret;
        private String type;
        private String baglanti_tipi;

        public KenarOzellikleri(double mesafe, double sure, double ucret, String type, String baglanti_tipi) {
            this.mesafe = mesafe;
            this.sure = sure;
            this.ucret = ucret;
            this.type = type;
            this.baglanti_tipi = baglanti_tipi;
        }

        public double getMesafe() {
            return mesafe;
        }

        public double getSure() {
            return sure;
        }

        public double getUcret() {
            return ucret;
        }

        public String getType() {
            return type;
        }

        public String getBaglanti_tipi() {
            return baglanti_tipi;
        }
    }

    public Rota(String data) throws JsonProcessingException {
        this.objectMapper = new ObjectMapper();
        this.data = this.objectMapper.readTree(data);

        this.duraklar = new HashMap<>();

        // Durakları işle
        for (JsonNode durak : this.data.get("duraklar")) {
            duraklar.put(durak.get("id").asText(), durak);
        }

        this.city = this.data.get("city").asText();
        this.taxi = this.data.get("taxi");
        this.G = grafOlustur();
    }

    private Graph<String, KenarOzellikleri> grafOlustur() {
        Graph<String, KenarOzellikleri> G = new DefaultDirectedWeightedGraph<>(KenarOzellikleri.class);

        // Düğümleri ekle
        for (JsonNode durak : data.get("duraklar")) {
            G.addVertex(durak.get("id").asText());
        }

        // Kenarları ekle
        for (JsonNode durak : data.get("duraklar")) {
            String durakId = durak.get("id").asText();

            // Sonraki durakları ekle
            if (durak.has("nextStops")) {
                for (JsonNode nextStop : durak.get("nextStops")) {
                    String hedefDurakId = nextStop.get("stopId").asText();
                    KenarOzellikleri kenar = new KenarOzellikleri(
                            nextStop.get("mesafe").asDouble(),
                            nextStop.get("sure").asDouble(),
                            nextStop.get("ucret").asDouble(),
                            "direct",
                            durak.get("type").asText()
                    );
                    G.addEdge(durakId, hedefDurakId, kenar);

                    // Ağırlığı ayarla (varsayılan olarak süre kullanılır)
                    G.setEdgeWeight(kenar, nextStop.get("sure").asDouble());
                }
            }

            // Transfer durakları ekle
            if (durak.has("transfer") && durak.get("transfer").has("transferStopId") &&
                    !durak.get("transfer").get("transferStopId").isNull()) {

                String transferDurakId = durak.get("transfer").get("transferStopId").asText();
                KenarOzellikleri kenar = new KenarOzellikleri(
                        0.1,
                        durak.get("transfer").get("transferSure").asDouble(),
                        durak.get("transfer").get("transferUcret").asDouble(),
                        "transfer",
                        "transfer"
                );
                G.addEdge(durakId, transferDurakId, kenar);
                G.setEdgeWeight(kenar, durak.get("transfer").get("transferSure").asDouble());
            }
        }

        return G;
    }

    private Map.Entry<String, Double> enYakinDurak(double lat, double lon) {
        double minMesafe = Double.POSITIVE_INFINITY;
        String enYakinDurakId = null;

        for (Map.Entry<String, JsonNode> entry : duraklar.entrySet()) {
            JsonNode durak = entry.getValue();
            double mesafe = haversineMesafe(lat, lon, durak.get("lat").asDouble(), durak.get("lon").asDouble());
            if (mesafe < minMesafe) {
                minMesafe = mesafe;
                enYakinDurakId = entry.getKey();
            }
        }

        return new AbstractMap.SimpleEntry<>(enYakinDurakId, minMesafe);
    }

    private double taksiUcretiHesapla(double mesafeKm) {
        return taxi.get("openingFee").asDouble() + (mesafeKm * taxi.get("costPerKm").asDouble());
    }

    private double optimizasyonAgirlikHesapla(KenarOzellikleri kenarVerisi, String optimizasyon) {
        if ("sure".equals(optimizasyon)) {
            return kenarVerisi.getSure();
        } else if ("mesafe".equals(optimizasyon)) {
            return kenarVerisi.getMesafe();
        } else if ("ucret".equals(optimizasyon)) {
            return kenarVerisi.getUcret();
        } else {
            // Varsayılan olarak süreyi kullan
            return kenarVerisi.getSure();
        }
    }

    private List<Map<String, Object>> enIyiTopluTasimaRotasi(String baslangicId, String bitisId, String optimizasyon) {
        try {
            // Ağırlık fonksiyonu için grafiği yeniden ağırlıklandır
            for (KenarOzellikleri kenar : G.edgeSet()) {
                G.setEdgeWeight(kenar, optimizasyonAgirlikHesapla(kenar, optimizasyon));
            }

            // En kısa yolu bul
            GraphPath<String, KenarOzellikleri> path = DijkstraShortestPath.findPathBetween(G, baslangicId, bitisId);

            if (path == null || path.getVertexList().size() < 2) {
                return new ArrayList<>();
            }

            List<Map<String, Object>> rotaSegmentleri = new ArrayList<>();
            List<String> vertices = path.getVertexList();

            for (int i = 0; i < vertices.size() - 1; i++) {
                String u = vertices.get(i);
                String v = vertices.get(i + 1);
                KenarOzellikleri kenarVerisi = G.getEdge(u, v);

                JsonNode baslangicDurak = duraklar.get(u);
                JsonNode bitisDurak = duraklar.get(v);

                Map<String, Object> segment = new HashMap<>();

                Map<String, Object> baslangicDurakMap = new HashMap<>();
                baslangicDurakMap.put("id", u);
                baslangicDurakMap.put("name", baslangicDurak.get("name").asText());
                baslangicDurakMap.put("lat", baslangicDurak.get("lat").asDouble());
                baslangicDurakMap.put("lon", baslangicDurak.get("lon").asDouble());
                baslangicDurakMap.put("type", baslangicDurak.get("type").asText());

                Map<String, Object> bitisDurakMap = new HashMap<>();
                bitisDurakMap.put("id", v);
                bitisDurakMap.put("name", bitisDurak.get("name").asText());
                bitisDurakMap.put("lat", bitisDurak.get("lat").asDouble());
                bitisDurakMap.put("lon", bitisDurak.get("lon").asDouble());
                bitisDurakMap.put("type", bitisDurak.get("type").asText());

                segment.put("baslangic_durak", baslangicDurakMap);
                segment.put("bitis_durak", bitisDurakMap);
                segment.put("mesafe", kenarVerisi.getMesafe());
                segment.put("sure", kenarVerisi.getSure());
                segment.put("ucret", kenarVerisi.getUcret());
                segment.put("baglanti_tipi", kenarVerisi.getBaglanti_tipi());

                rotaSegmentleri.add(segment);
            }

            return rotaSegmentleri;
        } catch (Exception e) {
            // Rota bulunamadı
            return new ArrayList<>();
        }
    }

    public Map<String, Object> rotaBulKoordinatlarla(double baslangicEnlem, double baslangicBoylam,
                                                     double hedefEnlem, double hedefBoylam, String optimizasyon) {
        // En yakın durakları bul
        Map.Entry<String, Double> baslangicDurak = enYakinDurak(baslangicEnlem, baslangicBoylam);
        Map.Entry<String, Double> bitisDurak = enYakinDurak(hedefEnlem, hedefBoylam);

        String baslangicId = baslangicDurak.getKey();
        double baslangicMesafe = baslangicDurak.getValue();
        String bitisId = bitisDurak.getKey();
        double bitisMesafe = bitisDurak.getValue();

        if (baslangicId == null || bitisId == null) {
            throw new IllegalArgumentException("En yakın durak bulunamadı.");
        }

        // Başlangıç ve bitiş yürüme segmentleri
        Map<String, Object> rotaBaslangic = new HashMap<>();
        Map<String, Object> baslangicDurakMap = new HashMap<>();
        ArrayList<Coordinate> durak_enlem_boylam_list = new ArrayList<>();
        durak_enlem_boylam_list.add(new Coordinate(baslangicEnlem, baslangicBoylam));
        baslangicDurakMap.put("id", "baslangic_nokta");
        baslangicDurakMap.put("name", "Başlangıç Noktası");
        baslangicDurakMap.put("lat", baslangicEnlem);
        baslangicDurakMap.put("lon", baslangicBoylam);
        baslangicDurakMap.put("type", "custom");

        rotaBaslangic.put("baslangic_durak", baslangicDurakMap);

        // Convert JsonNode to Map
        Map<String, Object> bitisDurakConverted = objectMapper.convertValue(duraklar.get(baslangicId), Map.class);
        rotaBaslangic.put("bitis_durak", bitisDurakConverted);
        rotaBaslangic.put("mesafe", baslangicMesafe);
        rotaBaslangic.put("sure", baslangicMesafe * 15); // Yürüme süresi tahmini: 15 dk/km
        rotaBaslangic.put("ucret", 0.0);
        rotaBaslangic.put("baglanti_tipi", "walking");

        Map<String, Object> rotaBitis = new HashMap<>();
        Map<String, Object> bitisDurakMap = new HashMap<>();
        //durak_enlem_boylam_list.add(new Tuple(hedefEnlem, hedefBoylam));
        bitisDurakMap.put("id", "hedef_nokta");
        bitisDurakMap.put("name", "Hedef Noktası");
        bitisDurakMap.put("lat", hedefEnlem);
        bitisDurakMap.put("lon", hedefBoylam);
        bitisDurakMap.put("type", "custom");

        // Convert JsonNode to Map
        Map<String, Object> baslangicDurakConverted = objectMapper.convertValue(duraklar.get(bitisId), Map.class);
        rotaBitis.put("baslangic_durak", baslangicDurakConverted);
        rotaBitis.put("bitis_durak", bitisDurakMap);
        rotaBitis.put("mesafe", bitisMesafe);
        rotaBitis.put("sure", bitisMesafe * 15); // Yürüme süresi tahmini: 15 dk/km
        rotaBitis.put("ucret", 0.0);
        rotaBitis.put("baglanti_tipi", "walking");

        // En iyi toplu taşıma rotasını bul
        List<Map<String, Object>> topluTasimaSegmentleri = enIyiTopluTasimaRotasi(baslangicId, bitisId, optimizasyon);

        // Tam rotayı oluştur
        List<Map<String, Object>> rota = new ArrayList<>();
        rota.add(rotaBaslangic);
        rota.addAll(topluTasimaSegmentleri);
        rota.add(rotaBitis);

        // Toplam değerleri hesapla
        double toplamMesafe = 0;
        double toplamSure = 0;
        double toplamUcret = 0;

        for (Map<String, Object> segment : rota) {
            toplamMesafe += (double) segment.get("mesafe");
            toplamSure += (double) segment.get("sure");
            toplamUcret += (double) segment.get("ucret");
        }

        // Waypoints oluştur
        List<Map<String, Object>> waypoints = new ArrayList<>();

        // Başlangıç noktası
        Map<String, Object> baslangicWaypoint = new HashMap<>();
        baslangicWaypoint.put("id", "baslangic_nokta");
        baslangicWaypoint.put("name", "Başlangıç Noktası");
        baslangicWaypoint.put("lat", baslangicEnlem);
        baslangicWaypoint.put("lon", baslangicBoylam);
        baslangicWaypoint.put("type", "custom");
        baslangicWaypoint.put("is_start", true);
        baslangicWaypoint.put("is_end", false);
        baslangicWaypoint.put("mesafe_to_stop", baslangicMesafe);
        waypoints.add(baslangicWaypoint);

        // Rotadaki tüm duraklar
        Set<String> visitedStops = new HashSet<>();
        for (Map<String, Object> segment : rota) {
            Map<String, Object> baslangicDurakSegment = (Map<String, Object>) segment.get("baslangic_durak");
            String durakId = (String) baslangicDurakSegment.get("id");

            if (!"baslangic_nokta".equals(durakId) && !visitedStops.contains(durakId)) {
                Map<String, Object> waypointDurak = new HashMap<>();
                waypointDurak.put("id", durakId);
                waypointDurak.put("name", baslangicDurakSegment.get("name"));
                durak_enlem_boylam_list.add(new Coordinate((double) baslangicDurakSegment.get("lat"),(double) baslangicDurakSegment.get("lon")));
                waypointDurak.put("lat", baslangicDurakSegment.get("lat"));
                waypointDurak.put("lon", baslangicDurakSegment.get("lon"));
                waypointDurak.put("type", baslangicDurakSegment.get("type"));
                waypointDurak.put("is_start", false);
                waypointDurak.put("is_end", false);

                waypoints.add(waypointDurak);
                visitedStops.add(durakId);
            }
        }

        // Hedef noktası
        Map<String, Object> hedefWaypoint = new HashMap<>();
        hedefWaypoint.put("id", "hedef_nokta");
        hedefWaypoint.put("name", "Hedef Noktası");
        hedefWaypoint.put("lat", hedefEnlem);
        hedefWaypoint.put("lon", hedefBoylam);
        hedefWaypoint.put("type", "custom");
        hedefWaypoint.put("is_start", false);
        hedefWaypoint.put("is_end", true);
        hedefWaypoint.put("mesafe_to_stop", bitisMesafe);
        waypoints.add(hedefWaypoint);

        // Rota bulundu mu?
        boolean rotaBulundu = true;
        if (topluTasimaSegmentleri.isEmpty() && !baslangicId.equals(bitisId)) {
            rotaBulundu = false;
        }

        Map<String, Object> sonuc = new HashMap<>();
        sonuc.put("rota_bulundu", rotaBulundu);

        Map<String, Double> baslangicKoordinat = new HashMap<>();
        baslangicKoordinat.put("lat", baslangicEnlem);
        baslangicKoordinat.put("lon", baslangicBoylam);
        sonuc.put("baslangic_koordinat", baslangicKoordinat);

        Map<String, Double> hedefKoordinat = new HashMap<>();
        hedefKoordinat.put("lat", hedefEnlem);
        hedefKoordinat.put("lon", hedefBoylam);
        sonuc.put("hedef_koordinat", hedefKoordinat);

        sonuc.put("baslangic_durak", duraklar.get(baslangicId));
        sonuc.put("bitis_durak", duraklar.get(bitisId));
        sonuc.put("toplam_mesafe_km", toplamMesafe);
        sonuc.put("toplam_ucret", toplamUcret);
        sonuc.put("toplam_sure_dk", toplamSure);
        sonuc.put("rota", rota);
        sonuc.put("waypoints", waypoints);

        // Taksi alternatifi
        double taksiMesafe = haversineMesafe(baslangicEnlem, baslangicBoylam, hedefEnlem, hedefBoylam);
        double taksiUcret = taksiUcretiHesapla(taksiMesafe);
        double taksiSure = taksiMesafe * 2; // Taksi süre tahmini: 2 dk/km

        Map<String, Object> taksiAlternatifi = new HashMap<>();
        taksiAlternatifi.put("mesafe_km", taksiMesafe);
        taksiAlternatifi.put("ucret", taksiUcret);
        taksiAlternatifi.put("tahmini_sure_dk", taksiSure);

        List<Map<String, Object>> taksiWaypoints = new ArrayList<>();

        Map<String, Object> taksiBaslangic = new HashMap<>();
        taksiBaslangic.put("id", "baslangic_nokta");
        taksiBaslangic.put("name", "Başlangıç Noktası");
        taksiBaslangic.put("lat", baslangicEnlem);
        taksiBaslangic.put("lon", baslangicBoylam);
        taksiBaslangic.put("type", "taksi");
        taksiBaslangic.put("is_start", true);
        taksiBaslangic.put("is_end", false);
        taksiWaypoints.add(taksiBaslangic);

        Map<String, Object> taksiHedef = new HashMap<>();
        taksiHedef.put("id", "hedef_nokta");
        taksiHedef.put("name", "Hedef Noktası");
        taksiHedef.put("lat", hedefEnlem);
        taksiHedef.put("lon", hedefBoylam);
        taksiHedef.put("type", "taksi");
        taksiHedef.put("is_start", false);
        taksiHedef.put("is_end", true);
        taksiWaypoints.add(taksiHedef);

        taksiAlternatifi.put("waypoints", taksiWaypoints);
        sonuc.put("taksi_alternatifi", taksiAlternatifi);
        durak_enlem_boylam_list.add(new Coordinate(hedefEnlem, hedefBoylam));
        //System.out.println(durak_enlem_boylam_list);

        //List<Map<String, Double>> way_points = new ArrayList<>();

        Map<String, Object> response = new HashMap<>();
        response.put("baslangic_koordinat", Map.of("lat", baslangicEnlem, "lon", baslangicBoylam));
        response.put("hedef_koordinat", Map.of("lat", hedefEnlem, "lon", hedefBoylam));
        // Check if waypoints is already in the correct format
        /*
        Object waypointsObj = sonuc.get("waypoints");
        List<Map<String, Double>> way_points;
        if (waypointsObj instanceof List<?> waypointsList) {
            way_points = (List<Map<String, Double>>) waypointsList; // Safe cast if already correct
        } else {
            throw new IllegalStateException("waypoints is not a List: " + waypointsObj.getClass().getName());
        }
        response.put("waypoints", way_points);*/
        // Safely handle waypoints
        //Object waypointsObj = sonuc.get("waypoints");
        List<Map<String, Double>> way_points;

        way_points = durak_enlem_boylam_list.stream()
                .filter(Coordinate.class::isInstance) // Ensure each item is a Coordinate
                .map(coord -> Map.of("lat", ((Coordinate) coord).lat(), "lon", ((Coordinate) coord).lon()))
                .collect(Collectors.toList());

        response.put("waypoints", way_points);

        return response;
    }

    public void rotaAciklamaGoster(Map<String, Object> sonuc) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rota = (List<Map<String, Object>>) sonuc.get("rota");

        System.out.println("\n" + city + " şehrinde rota bulundu!");

        Map<String, Double> baslangicKoordinat = (Map<String, Double>) sonuc.get("baslangic_koordinat");
        Map<String, Double> hedefKoordinat = (Map<String, Double>) sonuc.get("hedef_koordinat");

        System.out.println("Başlangıç: (" + baslangicKoordinat.get("lat") + ", " + baslangicKoordinat.get("lon") + ")");
        System.out.println("Hedef: (" + hedefKoordinat.get("lat") + ", " + hedefKoordinat.get("lon") + ")");

        // Rota açıklaması
        if ((boolean) sonuc.get("rota_bulundu")) {
            System.out.println("\nÖnerilen Rota:");
            for (int i = 0; i < rota.size(); i++) {
                Map<String, Object> segment = rota.get(i);

                Map<String, Object> baslangicDurak = (Map<String, Object>) segment.get("baslangic_durak");
                Map<String, Object> bitisDurak = (Map<String, Object>) segment.get("bitis_durak");

                String baslangic = baslangicDurak.get("name").toString();
                String bitis = bitisDurak.get("name").toString();
                double mesafe = (double) segment.get("mesafe");
                double sure = (double) segment.get("sure");
                double ucret = (double) segment.get("ucret");
                String baglantiTipi = (String) segment.get("baglanti_tipi");

                if ("walking".equals(baglantiTipi)) {
                    System.out.printf("  %d. %s'dan %s'a %.1f km yürüyün (yaklaşık %.1f dakika)%n",
                            i+1, baslangic, bitis, mesafe, sure);
                } else if ("transfer".equals(baglantiTipi)) {
                    System.out.printf("  %d. %s'dan %s'a transfer yapın (yaklaşık %.1f dakika, %.2f TL)%n",
                            i+1, baslangic, bitis, sure, ucret);
                } else {
                    System.out.printf("  %d. %s'dan %s'a %s ile gidin (yaklaşık %.1f dakika, %.2f TL)%n",
                            i+1, baslangic, bitis, baglantiTipi, sure, ucret);
                }
            }

            System.out.println("\nToplam:");
            System.out.printf("  Mesafe: %.1f km%n", (double) sonuc.get("toplam_mesafe_km"));
            System.out.printf("  Süre: %.1f dakika%n", (double) sonuc.get("toplam_sure_dk"));
            System.out.printf("  Ücret: %.2f TL%n", (double) sonuc.get("toplam_ucret"));
        } else {
            System.out.println("\nToplu taşıma rotası bulunamadı.");
        }

        // Taksi alternatifi
        Map<String, Object> taksi = (Map<String, Object>) sonuc.get("taksi_alternatifi");
        System.out.println("\nTaksi alternatifi:");
        System.out.printf("  Mesafe: %.1f km%n", (double) taksi.get("mesafe_km"));
        System.out.printf("  Tahmini süre: %.1f dakika%n", (double) taksi.get("tahmini_sure_dk"));
        System.out.printf("  Ücret: %.2f TL%n", (double) taksi.get("ucret"));
    }

    private double haversineMesafe(double lat1, double lon1, double lat2, double lon2) {
        // Dünya yarıçapı (km)
        double R = 6371.0;

        // Radyana dönüştür
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);

        // Haversine formülü
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1Rad) * Math.cos(lat2Rad);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }


}