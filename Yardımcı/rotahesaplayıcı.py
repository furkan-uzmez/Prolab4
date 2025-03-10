import json
import math
import networkx as nx
from typing import Dict, List, Tuple, Any

class RotaHesaplayici:
    def __init__(self, data: dict):
        self.data = data
        self.duraklar = {durak["id"]: durak for durak in self.data["duraklar"]}
        self.city = self.data["city"]
        self.taxi = self.data["taxi"]
        self.G = self._graf_olustur()
    
    def _graf_olustur(self) -> nx.DiGraph:
        """NetworkX kullanarak duraklar ve bağlantılarından yönlü bir graf oluşturur."""
        G = nx.DiGraph()
        for durak in self.data["duraklar"]:
            G.add_node(durak["id"], **durak)
        for durak in self.data["duraklar"]:
            durak_id = durak["id"]
            for next_stop in durak["nextStops"]:
                G.add_edge(
                    durak_id, 
                    next_stop["stopId"], 
                    mesafe=next_stop["mesafe"],
                    sure=next_stop["sure"],
                    ucret=next_stop["ucret"],
                    type="direct",
                    baglanti_tipi=durak["type"]  # Bağlantı tipini (bus, tram, vb.) ekle
                )
            if durak["transfer"] and durak["transfer"]["transferStopId"]:
                G.add_edge(
                    durak_id,
                    durak["transfer"]["transferStopId"],
                    mesafe=0.1,
                    sure=durak["transfer"]["transferSure"],
                    ucret=durak["transfer"]["transferUcret"],
                    type="transfer",
                    baglanti_tipi="transfer"
                )
        return G
    
    def _haversine_mesafe(self, lat1: float, lon1: float, lat2: float, lon2: float) -> float:
        """İki nokta arasındaki haversine mesafesini km cinsinden hesaplar."""
        R = 6371.0
        lat1_rad = math.radians(lat1)
        lon1_rad = math.radians(lon1)
        lat2_rad = math.radians(lat2)
        lon2_rad = math.radians(lon2)
        dlon = lon2_rad - lon1_rad
        dlat = lat2_rad - lat1_rad
        a = math.sin(dlat/2)**2 + math.cos(lat1_rad) * math.cos(lat2_rad) * math.sin(dlon/2)**2
        c = 2 * math.atan2(math.sqrt(a), math.sqrt(1-a))
        return R * c
    
    def _en_yakin_durak(self, lat: float, lon: float) -> str:
        """Verilen koordinatlara en yakın durağın ID'sini bulur."""
        min_mesafe = float('inf')
        en_yakin_durak_id = None
        
        for durak_id, durak in self.duraklar.items():
            mesafe = self._haversine_mesafe(lat, lon, durak["lat"], durak["lon"])
            if mesafe < min_mesafe:
                min_mesafe = mesafe
                en_yakin_durak_id = durak_id
        
        return en_yakin_durak_id, min_mesafe
    
    def _taksi_ucreti_hesapla(self, mesafe_km: float) -> float:
        """Taksi ücretini verilen mesafeye göre hesaplar."""
        return self.taxi["openingFee"] + (mesafe_km * self.taxi["costPerKm"])
    
    def _optimizasyon_agirlik_hesapla(self, edge_data: Dict, optimizasyon: str) -> float:
        """
        Optimizasyon kriterine göre kenar ağırlığını hesaplar.
        """
        if optimizasyon == "sure":
            return edge_data["sure"]
        elif optimizasyon == "mesafe":
            return edge_data["mesafe"]
        elif optimizasyon == "ucret":
            return edge_data["ucret"]
        else:
            # Varsayılan olarak süreyi kullan
            return edge_data["sure"]
    
    def _en_iyi_toplu_tasima_rotasi(self, 
                                   baslangic_id: str, 
                                   bitis_id: str, 
                                   optimizasyon: str = "sure") -> List[Dict[str, Any]]:
        """
        NetworkX kullanarak en iyi toplu taşıma rotasını bulur.
        
        Args:
            baslangic_id: Başlangıç durağı ID
            bitis_id: Bitiş durağı ID
            optimizasyon: Optimizasyon kriteri ("sure", "mesafe", "ucret")
            
        Returns:
            Rota segmentlerini içeren liste
        """
        try:
            # Ağırlık fonksiyonu tanımla
            def weight_function(u, v, edge_data):
                return self._optimizasyon_agirlik_hesapla(edge_data, optimizasyon)
            
            # En kısa yolu bul (Dijkstra algoritması)
            path = nx.dijkstra_path(self.G, baslangic_id, bitis_id, weight=weight_function)
            
            # Yol bulunamazsa boş liste döndür
            if not path or len(path) < 2:
                return []
            
            # Rotayı oluştur
            rota_segmentleri = []
            for i in range(len(path) - 1):
                u, v = path[i], path[i+1]
                edge_data = self.G.get_edge_data(u, v)
                
                # Durak bilgilerini al
                baslangic_durak = self.duraklar[u]
                bitis_durak = self.duraklar[v]
                
                # Bağlantı tipi (otobüs, tramvay, transfer)
                baglanti_tipi = edge_data["baglanti_tipi"]
                
                segment = {
                    "baslangic_durak": {
                        "id": u,
                        "name": baslangic_durak["name"],
                        "lat": baslangic_durak["lat"],
                        "lon": baslangic_durak["lon"],
                        "type": baslangic_durak["type"]
                    },
                    "bitis_durak": {
                        "id": v,
                        "name": bitis_durak["name"],
                        "lat": bitis_durak["lat"],
                        "lon": bitis_durak["lon"],
                        "type": bitis_durak["type"]
                    },
                    "mesafe": edge_data["mesafe"],
                    "sure": edge_data["sure"],
                    "ucret": edge_data["ucret"],
                    "baglanti_tipi": baglanti_tipi
                }
                rota_segmentleri.append(segment)
            
            return rota_segmentleri
        except nx.NetworkXNoPath:
            # Rota bulunamadı
            return []
    
    def rota_bul_koordinatlarla(self, baslangic_enlem: float, baslangic_boylam: float, 
                              hedef_enlem: float, hedef_boylam: float, optimizasyon: str = "sure") -> Dict[str, Any]:
        """
        Başlangıç ve hedef koordinatlarına göre tam rotayı bulur.
        Toplu taşıma, yürüme ve taksi alternatiflerini hesaplar.
        
        Args:
            baslangic_enlem: Başlangıç enlemi
            baslangic_boylam: Başlangıç boylamı
            hedef_enlem: Hedef enlemi
            hedef_boylam: Hedef boylamı
            optimizasyon: Optimizasyon kriteri ("sure", "mesafe", "ucret")
        
        Returns:
            Rota alternatiflerini içeren sözlük
        """
        # En yakın durakları bul
        baslangic_id, baslangic_mesafe = self._en_yakin_durak(baslangic_enlem, baslangic_boylam)
        bitis_id, bitis_mesafe = self._en_yakin_durak(hedef_enlem, hedef_boylam)
        
        if not baslangic_id or not bitis_id:
            raise ValueError("En yakın durak bulunamadı.")
            
        # Başlangıç ve bitiş yürüme segmentleri
        rota_baslangic = {
            "baslangic_durak": {
                "id": "baslangic_nokta",
                "name": "Başlangıç Noktası",
                "lat": baslangic_enlem,
                "lon": baslangic_boylam,
                "type": "custom"
            },
            "bitis_durak": self.duraklar[baslangic_id],
            "mesafe": baslangic_mesafe,
            "sure": baslangic_mesafe * 15,  # Yürüme süresi tahmini: 15 dk/km
            "ucret": 0,
            "baglanti_tipi": "walking"
        }
        
        rota_bitis = {
            "baslangic_durak": self.duraklar[bitis_id],
            "bitis_durak": {
                "id": "hedef_nokta",
                "name": "Hedef Noktası",
                "lat": hedef_enlem,
                "lon": hedef_boylam,
                "type": "custom"
            },
            "mesafe": bitis_mesafe,
            "sure": bitis_mesafe * 15,  # Yürüme süresi tahmini: 15 dk/km
            "ucret": 0,
            "baglanti_tipi": "walking"
        }
        
        # En iyi toplu taşıma rotasını bul
        toplu_tasima_segmentleri = self._en_iyi_toplu_tasima_rotasi(baslangic_id, bitis_id, optimizasyon)
        
        # Tam rotayı oluştur
        rota = [rota_baslangic] + toplu_tasima_segmentleri + [rota_bitis]
        
        # Toplam değerleri hesapla
        toplam_mesafe = sum(segment["mesafe"] for segment in rota)
        toplam_sure = sum(segment["sure"] for segment in rota)
        toplam_ucret = sum(segment["ucret"] for segment in rota)
        
        # Waypoints oluştur
        waypoints = []
        
        # Başlangıç noktası
        waypoints.append({
            "id": "baslangic_nokta",
            "name": "Başlangıç Noktası",
            "lat": baslangic_enlem,
            "lon": baslangic_boylam,
            "type": "custom",
            "is_start": True,
            "is_end": False,
            "mesafe_to_stop": baslangic_mesafe
        })
        
        # Rotadaki tüm duraklar
        visited_stops = set()
        for segment in rota:
            if segment["baslangic_durak"]["id"] != "baslangic_nokta" and segment["baslangic_durak"]["id"] not in visited_stops:
                durak = segment["baslangic_durak"]
                waypoints.append({
                    "id": durak["id"],
                    "name": durak["name"],
                    "lat": durak["lat"],
                    "lon": durak["lon"],
                    "type": durak["type"],
                    "is_start": False,
                    "is_end": False
                })
                visited_stops.add(durak["id"])
        
        # Hedef noktası
        waypoints.append({
            "id": "hedef_nokta",
            "name": "Hedef Noktası",
            "lat": hedef_enlem,
            "lon": hedef_boylam,
            "type": "custom",
            "is_start": False,
            "is_end": True,
            "mesafe_to_stop": bitis_mesafe
        })
        
        # Rota bulundu mu?
        rota_bulundu = True
        if not toplu_tasima_segmentleri:
            # Toplu taşıma rotası bulunamadıysa ve duraklar farklıysa false döndür
            if baslangic_id != bitis_id:
                rota_bulundu = False
        
        sonuc = {
            "rota_bulundu": rota_bulundu,
            "baslangic_koordinat": {"lat": baslangic_enlem, "lon": baslangic_boylam},
            "hedef_koordinat": {"lat": hedef_enlem, "lon": hedef_boylam},
            "baslangic_durak": self.duraklar[baslangic_id],
            "bitis_durak": self.duraklar[bitis_id],
            "toplam_mesafe_km": toplam_mesafe,
            "toplam_ucret": toplam_ucret,
            "toplam_sure_dk": toplam_sure,
            "rota": rota,
            "waypoints": waypoints
        }
        
        # Taksi alternatifi
        taksi_mesafe = self._haversine_mesafe(baslangic_enlem, baslangic_boylam, hedef_enlem, hedef_boylam)
        taksi_ucret = self._taksi_ucreti_hesapla(taksi_mesafe)
        taksi_sure = taksi_mesafe * 2  # Taksi süre tahmini: 2 dk/km
        
        sonuc["taksi_alternatifi"] = {
            "mesafe_km": taksi_mesafe,
            "ucret": taksi_ucret,
            "tahmini_sure_dk": taksi_sure,
            "waypoints": [
                {"id": "baslangic_nokta", "name": "Başlangıç Noktası", "lat": baslangic_enlem, "lon": baslangic_boylam, "type": "taksi", "is_start": True, "is_end": False},
                {"id": "hedef_nokta", "name": "Hedef Noktası", "lat": hedef_enlem, "lon": hedef_boylam, "type": "taksi", "is_start": False, "is_end": True}
            ]
        }
        
        return sonuc
    
    def rota_aciklama_goster(self, sonuc: Dict[str, Any]) -> None:
        """Bulunan rotayı insan dostu bir şekilde gösterir."""
        rota = sonuc["rota"]
        
        print(f"\n{self.city} şehrinde rota bulundu!")
        print(f"Başlangıç: ({sonuc['baslangic_koordinat']['lat']}, {sonuc['baslangic_koordinat']['lon']})")
        print(f"Hedef: ({sonuc['hedef_koordinat']['lat']}, {sonuc['hedef_koordinat']['lon']})")
        
        # Rota açıklaması
        if sonuc["rota_bulundu"]:
            print("\nÖnerilen Rota:")
            for i, segment in enumerate(rota):
                baslangic = segment["baslangic_durak"]["name"]
                bitis = segment["bitis_durak"]["name"]
                mesafe = segment["mesafe"]
                sure = segment["sure"]
                ucret = segment["ucret"]
                baglanti_tipi = segment["baglanti_tipi"]
                
                if baglanti_tipi == "walking":
                    print(f"  {i+1}. {baslangic}'dan {bitis}'a {mesafe:.1f} km yürüyün (yaklaşık {sure:.1f} dakika)")
                elif baglanti_tipi == "transfer":
                    print(f"  {i+1}. {baslangic}'dan {bitis}'a transfer yapın (yaklaşık {sure:.1f} dakika, {ucret:.2f} TL)")
                else:
                    print(f"  {i+1}. {baslangic}'dan {bitis}'a {baglanti_tipi} ile gidin (yaklaşık {sure:.1f} dakika, {ucret:.2f} TL)")
            
            print(f"\nToplam:")
            print(f"  Mesafe: {sonuc['toplam_mesafe_km']:.1f} km")
            print(f"  Süre: {sonuc['toplam_sure_dk']:.1f} dakika")
            print(f"  Ücret: {sonuc['toplam_ucret']:.2f} TL")
        else:
            print("\nToplu taşıma rotası bulunamadı.")
        
        # Taksi alternatifi
        taksi = sonuc["taksi_alternatifi"]
        print("\nTaksi alternatifi:")
        print(f"  Mesafe: {taksi['mesafe_km']:.1f} km")
        print(f"  Tahmini süre: {taksi['tahmini_sure_dk']:.1f} dakika")
        print(f"  Ücret: {taksi['ucret']:.2f} TL")

# Örnek Kullanım
if __name__ == "__main__":
    with open("veriseti.json", "r", encoding="utf-8") as f:
        data_json = f.read()
    
    rota_hesaplayici = RotaHesaplayici(data_json)
    
    # Koordinatlarla rota bul
    baslangic_enlem = 40.7669  # Örnek koordinatlar
    baslangic_boylam = 29.9169
    hedef_enlem = 40.8200
    hedef_boylam = 29.9300
    
    sonuc = rota_hesaplayici.rota_bul_koordinatlarla(
        baslangic_enlem, baslangic_boylam, hedef_enlem, hedef_boylam, optimizasyon="sure"
    )
    
    # Rota açıklamasını göster
    rota_hesaplayici.rota_aciklama_goster(sonuc)
    
    # JSON olarak kaydet (örnek)
    with open("rota_sonuc.json", "w", encoding="utf-8") as f:
        json.dump(sonuc, f, ensure_ascii=False, indent=2)
    print("Rota 'rota_sonuc.json' dosyasına kaydedildi.")