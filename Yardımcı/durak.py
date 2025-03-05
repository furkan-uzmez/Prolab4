from geopy.distance import geodesic
class Durak():
    def __init__(self):
        self.durak_verisi = {}
        self.duraklar = []
    
    def set_durak_verisi(self,veri):
        self.durak_verisi = veri 

    def get_durak_verisi(self):
        return self.durak_verisi
    
    def 

    # En yakın durağı hesaplayan fonksiyon
    def en_yakin_durak(self,kullanici_konumu):
        duraklar = []
        for durak in self.durak_verisi:
            duraklar.append((durak['lat'] , durak['lon'] , durak['name']))
        print(duraklar)
        min_mesafe = float("inf")
        en_yakin = None
        for durak in duraklar:
            mesafe = geodesic(kullanici_konumu, (durak[0], durak[1])).km
            if mesafe < min_mesafe:
                min_mesafe = mesafe
                en_yakin = durak
        return en_yakin, min_mesafe
    
    def display_stop(self):
        pass