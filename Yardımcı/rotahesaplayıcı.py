class RotaHesaplayici():
    def rota_hesapla(self,en_yakin):
        if en_yakin['distance_m'] < 3:
            print('Yürüyerek gidebilirsin')
        else : 
            print('Taksi tutmalısın')
        
import json
import heapq
import math
from typing import Dict, List, Tuple, Set, Optional, Union, Any

class IzmitRotaHesaplayici:
    def __init__(self, data_json: str):
        self.data = json.loads(data_json)
        self.duraklar = {durak["id"]: durak for durak in self.data["duraklar"]}
        self.city = self.data["city"]
        self.taxi = self.data["taxi"]
        self.graf = self._graf_olustur()
    
    def _graf_olustur(self) -> Dict[str, List[Dict[str, Any]]]:
        """Duraklar ve bağlantılarından bir graf oluşturur."""
        graf = {}
        
        # Her durağı grafa ekle
        for durak in self.data["duraklar"]:
            durak_id = durak["id"]
            if durak_id not in graf:
                graf[durak_id] = []
            
            # Duraktan sonraki durakları bağlantı olarak ekle
            for next_stop in durak["nextStops"]:
                graf[durak_id].append({
                    "hedef": next_stop["stopId"],
                    "mesafe": next_stop["mesafe"],
                    "sure": next_stop["sure"],
                    "ucret": next_stop["ucret"],
                    "type": "direct"
                })
            
            # Transfer durakları varsa ekle
            if durak["transfer"] and durak["transfer"]["transferStopId"]:
                graf[durak_id].append({
                    "hedef": durak["transfer"]["transferStopId"],
                    "mesafe": 0.1,  # Transfer için sembolik mesafe
                    "sure": durak["transfer"]["transferSure"],
                    "ucret": durak["transfer"]["transferUcret"],
                    "type": "transfer"
                })
        
        return graf
    
    def _haversine_mesafe(self, lat1: float, lon1: float, lat2: float, lon2: float) -> float:
        """İki nokta arasındaki haversine mesafesini km cinsinden hesaplar."""
        # Dünya yarıçapı (km)
        R = 6371.0
        
        # Radyana dönüştürme
        lat1_rad = math.radians(lat1)
        lon1_rad = math.radians(lon1)
        lat2_rad = math.radians(lat2)
        lon2_rad = math.radians(lon2)
        
        # Enlem ve boylam farkları
        dlon = lon2_rad - lon1_rad
        dlat = lat2_rad - lat1_rad
        
        # Haversine formülü
        a = math.sin(dlat/2)**2 + math.cos(lat1_rad) * math.cos(lat2_rad) * math.sin(dlon/2)**2
        c = 2 * math.atan2(math.sqrt(a), math.sqrt(1-a))
        
        # Mesafe (km)
        return R * c
    
    def _taksi_ucreti_hesapla(self, mesafe_km: float) -> float:
        """Taksi ücretini verilen mesafeye göre hesaplar."""
        return self.taxi["openingFee"] + (mesafe_km * self.taxi["costPerKm"])
    
    def dijkstra(self, baslangic_id: str, bitis_id: str, optimizasyon: str = "sure") -> Dict[str, Any]:
        """
        Belirtilen başlangıç ve bitiş durakları arasında Dijkstra algoritması ile en optimum rotayı bulur.
        
        Args:
            baslangic_id: Başlangıç durağının ID'si
            bitis_id: Bitiş durağının ID'si
            optimizasyon: Optimizasyon kriteri ('sure', 'mesafe', 'ucret')
        
        Returns:
            En iyi rotanın bilgilerini içeren sözlük
        """
        # Başlangıç ve bitiş durakları var mı kontrol et
        if baslangic_id not in self.duraklar or bitis_id not in self.duraklar:
            raise ValueError("Başlangıç veya bitiş durağı bulunamadı.")
        
        # Tüm duraklar için sonsuz maliyet başlat
        mesafeler = {durak_id: float('infinity') for durak_id in self.duraklar}
        mesafeler[baslangic_id] = 0
        
        # Her durak için toplam süre ve ücret takibi
        toplam_sure = {durak_id: float('infinity') for durak_id in self.duraklar}
        toplam_sure[baslangic_id] = 0
        
        toplam_ucret = {durak_id: float('infinity') for durak_id in self.duraklar}
        toplam_ucret[baslangic_id] = 0
        
        # Rota takibi için önceki durakları kaydet
        onceki = {durak_id: None for durak_id in self.duraklar}
        
        # İşlenmemiş duraklar kümesi
        islenmemis = set(self.duraklar.keys())
        
        # Öncelik kuyruğu (priority queue) kullanarak en düşük maliyetli durağı seç
        oncelik_kuyrugu = [(0, baslangic_id)]
        
        while islenmemis and oncelik_kuyrugu:
            # En düşük maliyetli durağı seç
            guncel_maliyet, guncel_durak = heapq.heappop(oncelik_kuyrugu)
            
            # Eğer hedef durağa ulaştıysak, sonucu döndür
            if guncel_durak == bitis_id:
                break
                
            # Durak zaten işlendiyse atla
            if guncel_durak not in islenmemis:
                continue
                
            # Durağı işlenmiş olarak işaretle
            islenmemis.remove(guncel_durak)
            
            # Komşu duraklara bak
            for komsu in self.graf.get(guncel_durak, []):
                komsu_durak = komsu["hedef"]
                
                # Komşu durak işlenmişse atla
                if komsu_durak not in islenmemis:
                    continue
                
                # Optimizasyon tipine göre maliyet hesapla
                if optimizasyon == "sure":
                    yeni_maliyet = toplam_sure[guncel_durak] + komsu["sure"]
                    eski_maliyet = toplam_sure[komsu_durak]
                elif optimizasyon == "mesafe":
                    yeni_maliyet = mesafeler[guncel_durak] + komsu["mesafe"]
                    eski_maliyet = mesafeler[komsu_durak]
                elif optimizasyon == "ucret":
                    yeni_maliyet = toplam_ucret[guncel_durak] + komsu["ucret"]
                    eski_maliyet = toplam_ucret[komsu_durak]
                else:
                    raise ValueError("Geçersiz optimizasyon kriteri. 'sure', 'mesafe' veya 'ucret' kullanın.")
                
                # Daha iyi bir rota bulduk mu?
                if yeni_maliyet < eski_maliyet:
                    # Maliyeti güncelle
                    if optimizasyon == "sure":
                        toplam_sure[komsu_durak] = yeni_maliyet
                    elif optimizasyon == "mesafe":
                        mesafeler[komsu_durak] = yeni_maliyet
                    elif optimizasyon == "ucret":
                        toplam_ucret[komsu_durak] = yeni_maliyet
                    
                    # Diğer metrikleri de güncelle
                    if optimizasyon != "sure":
                        toplam_sure[komsu_durak] = toplam_sure[guncel_durak] + komsu["sure"]
                    if optimizasyon != "mesafe":
                        mesafeler[komsu_durak] = mesafeler[guncel_durak] + komsu["mesafe"]
                    if optimizasyon != "ucret":
                        toplam_ucret[komsu_durak] = toplam_ucret[guncel_durak] + komsu["ucret"]
                    
                    # Önceki durağı güncelle
                    onceki[komsu_durak] = {"durak_id": guncel_durak, "baglanti": komsu}
                    
                    # Öncelik kuyruğuna ekle
                    heapq.heappush(oncelik_kuyrugu, (yeni_maliyet, komsu_durak))
        
        # Eğer bitiş durağına ulaşamadıysak
        if onceki[bitis_id] is None:
            # Taksi alternatifini hesapla
            baslangic_durak = self.duraklar[baslangic_id]
            bitis_durak = self.duraklar[bitis_id]
            
            taksi_mesafe = self._haversine_mesafe(
                baslangic_durak["lat"], baslangic_durak["lon"],
                bitis_durak["lat"], bitis_durak["lon"]
            )
            
            taksi_ucret = self._taksi_ucreti_hesapla(taksi_mesafe)
            taksi_sure = taksi_mesafe * 2  # Yaklaşık olarak 30 km/saat hızla
            
            return {
                "rota_bulundu": False,
                "taksi_alternatifi": {
                    "mesafe_km": taksi_mesafe,
                    "ucret": taksi_ucret,
                    "tahmini_sure_dk": taksi_sure
                }
            }
        
        # Rotayı geri izle
        rota = []
        guncel = bitis_id
        
        while guncel != baslangic_id:
            onceki_baglanti = onceki[guncel]
            onceki_durak_id = onceki_baglanti["durak_id"]
            baglanti_detay = onceki_baglanti["baglanti"]
            
            baglanti = {
                "baslangic_durak": self.duraklar[onceki_durak_id],
                "bitis_durak": self.duraklar[guncel],
                "mesafe": baglanti_detay["mesafe"],
                "sure": baglanti_detay["sure"],
                "ucret": baglanti_detay["ucret"],
                "baglanti_tipi": baglanti_detay["type"]
            }
            
            rota.insert(0, baglanti)
            guncel = onceki_durak_id
        
        # Sonuç
        return {
            "rota_bulundu": True,
            "baslangic_durak": self.duraklar[baslangic_id],
            "bitis_durak": self.duraklar[bitis_id],
            "toplam_mesafe_km": mesafeler[bitis_id],
            "toplam_ucret": toplam_ucret[bitis_id],
            "toplam_sure_dk": toplam_sure[bitis_id],
            "rota": rota
        }
    
    def rota_bul(self, baslangic_durak_id: str, bitis_durak_id: str, optimizasyon: str = "sure") -> Dict[str, Any]:
        """Kullanıcı arayüzü için Dijkstra algoritmasını çağırır ve insan dostu bir sonuç döndürür."""
        sonuc = self.dijkstra(baslangic_durak_id, bitis_durak_id, optimizasyon)
        
        # Taksi alternatifi hesaplama
        baslangic_durak = self.duraklar[baslangic_durak_id]
        bitis_durak = self.duraklar[bitis_durak_id]
        
        taksi_mesafe = self._haversine_mesafe(
            baslangic_durak["lat"], baslangic_durak["lon"],
            bitis_durak["lat"], bitis_durak["lon"]
        )
        
        taksi_ucret = self._taksi_ucreti_hesapla(taksi_mesafe)
        taksi_sure = taksi_mesafe * 2  # Yaklaşık olarak 30 km/saat hızla
        
        sonuc["taksi_alternatifi"] = {
            "mesafe_km": taksi_mesafe,
            "ucret": taksi_ucret,
            "tahmini_sure_dk": taksi_sure
        }
        
        return sonuc
    
    def rota_aciklama_goster(self, sonuc: Dict[str, Any]) -> None:
        """Rota sonucunu insan dostu bir şekilde gösterir."""
        if not sonuc["rota_bulundu"]:
            print(f"Toplu taşıma ile rota bulunamadı.")
            print("Taksi alternatifi:")
            taksi = sonuc["taksi_alternatifi"]
            print(f"  Mesafe: {taksi['mesafe_km']:.1f} km")
            print(f"  Tahmini süre: {taksi['tahmini_sure_dk']:.1f} dakika")
            print(f"  Ücret: {taksi['ucret']:.2f} TL")
            return
        
        print(f"\n{self.city} şehrinde rota bulundu!")
        print(f"Başlangıç: {sonuc['baslangic_durak']['name']}")
        print(f"Bitiş: {sonuc['bitis_durak']['name']}")
        print(f"Toplam mesafe: {sonuc['toplam_mesafe_km']:.1f} km")
        print(f"Toplam süre: {sonuc['toplam_sure_dk']} dakika")
        print(f"Toplam ücret: {sonuc['toplam_ucret']:.2f} TL")
        
        print("\nRota detayları:")
        for i, adim in enumerate(sonuc["rota"], 1):
            baglanti_tipi = "Transfer" if adim["baglanti_tipi"] == "transfer" else "Direkt"
            print(f"{i}. {baglanti_tipi}: {adim['baslangic_durak']['name']} → {adim['bitis_durak']['name']}")
            print(f"   Araç tipi: {adim['bitis_durak']['type']}")
            print(f"   Mesafe: {adim['mesafe']:.1f} km")
            print(f"   Süre: {adim['sure']} dakika")
            print(f"   Ücret: {adim['ucret']:.2f} TL")
        
        # Taksi karşılaştırması
        taksi = sonuc["taksi_alternatifi"]
        print("\nTaksi alternatifi:")
        print(f"  Mesafe: {taksi['mesafe_km']:.1f} km")
        print(f"  Tahmini süre: {taksi['tahmini_sure_dk']:.1f} dakika")
        print(f"  Ücret: {taksi['ucret']:.2f} TL")
        
        # Karşılaştırma
        tasarruf = taksi['ucret'] - sonuc['toplam_ucret']
        if tasarruf > 0:
            print(f"\nToplu taşıma kullanarak {tasarruf:.2f} TL tasarruf edebilirsiniz.")
        else:
            print(f"\nTaksi kullanmak {-tasarruf:.2f} TL daha ucuz olabilir.")
        
        sure_fark = taksi['tahmini_sure_dk'] - sonuc['toplam_sure_dk']
        if sure_fark > 0:
            print(f"Toplu taşıma kullanarak {sure_fark:.1f} dakika tasarruf edebilirsiniz.")
        else:
            print(f"Taksi kullanmak {-sure_fark:.1f} dakika daha hızlı olabilir.")

# Örnek Kullanım
if __name__ == "__main__":
    with open("veriseti.json", "r", encoding="utf-8") as f:
        data_json = f.read()
    
    # JSON verisi burada bir string olarak verilmiş olacak
    rota_hesaplayici = IzmitRotaHesaplayici(data_json)
    
    # Örnek: Otogardan Umuttepe'ye en hızlı rota
    sonuc_sure = rota_hesaplayici.rota_bul("bus_otogar", "bus_umuttepe", "sure")
    rota_hesaplayici.rota_aciklama_goster(sonuc_sure)
    
    # Örnek: Sekapark'tan Symbol AVM'ye en ucuz rota
    print("\n" + "="*50)
    sonuc_ucret = rota_hesaplayici.rota_bul("bus_sekapark", "bus_symbolavm", "ucret")
    rota_hesaplayici.rota_aciklama_goster(sonuc_ucret)
    
    # Örnek: Otogardan Halkevi'ye en kısa mesafe
    print("\n" + "="*50)
    sonuc_mesafe = rota_hesaplayici.rota_bul("tram_otogar", "tram_halkevi", "mesafe")
    rota_hesaplayici.rota_aciklama_goster(sonuc_mesafe)