from geopy.distance import geodesic
class Durak():
    def __init__(self):
        self.durak_verisi = {}
        self.duraklar = []
    
    def set_duraklar(self):
        for durak_ in self.durak_verisi:
            self.duraklar.append((durak_['lat'] , durak_['lon'] , durak_['name']))

    # En yakın durağı hesaplayan fonksiyon
    def en_yakin_durak(self,kullanici_konumu):
        print(self.duraklar)
        min_mesafe = float("inf")
        en_yakin = None
        for durak in self.duraklar:
            mesafe = geodesic(kullanici_konumu, (durak[0], durak[1])).km
            if mesafe < min_mesafe:
                min_mesafe = mesafe
                en_yakin = durak
        return en_yakin, min_mesafe     

    