import folium
class Displayer():
    def display_user_location(self,harita,kullanici_konumu):
        # Kullanıcı konumunu haritada işaretleme
        folium.Marker(
            location=kullanici_konumu,
            popup="Kullanıcı Konumu",
            icon=folium.Icon(color="red", icon="user")
        ).add_to(harita)
    
    def display_duraklar(self,harita,duraklar):
        # Durakları haritaya ekleyin
        for enlem, boylam, isim in duraklar:
            folium.Marker(
                location=[enlem, boylam],
                popup=isim,
                icon=folium.Icon(color="blue", icon="cloud")
            ).add_to(harita)

    def display_nearest_durak(self,harita,en_yakin,mesafe):
        # En yakın durağı işaretleme
        folium.Marker(
            location=[en_yakin[0], en_yakin[1]],
            popup=f"{en_yakin[2]} (En Yakın Durak - {mesafe:.2f} km)",
            icon=folium.Icon(color="green", icon="info-sign")
        ).add_to(harita)