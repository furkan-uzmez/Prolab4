from dataclasses import dataclass
import folium
class Konum():
    enlem : float
    boylam : float
    
    def display_location(self,harita,kullanici_konumu):
        # Kullanıcı konumunu haritada işaretleme
        folium.Marker(
            location=kullanici_konumu,
            popup="Kullanıcı Konumu",
            icon=folium.Icon(color="red", icon="user")
        ).add_to(harita)
    
    def set_konum(self,enlem,boylam):
        self.enlem = enlem
        self.boylam = boylam

    def get_konum(self):
        return (self.enlem,self.boylam)
    