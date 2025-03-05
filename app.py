from flask import Flask, render_template, request
import folium
from geopy.distance import geodesic

app = Flask(__name__)

# Duraklar listesi
duraklar = [
    (40.7651, 29.9406, "Sekapark"),
    (40.7751, 29.9500, "Halkevi"),
    (40.7568, 29.9212, "Otogar"),
    (40.7410, 29.9255, "Yahya Kaptan"),
    (40.7510, 29.9300, "Santral"),
]


# En yakın durağı hesaplayan fonksiyon
def en_yakin_durak(kullanici_konumu):
    min_mesafe = float("inf")
    en_yakin = None
    for durak in duraklar:
        mesafe = geodesic(kullanici_konumu, (durak[0], durak[1])).km
        if mesafe < min_mesafe:
            min_mesafe = mesafe
            en_yakin = durak
    return en_yakin, min_mesafe


@app.route("/", methods=["GET", "POST"])
def home():
    print("Index.html çalıştırılıyor...")  # Debugging için

    # Harita oluştur
    harita = folium.Map(location=[40.7651, 29.9406], zoom_start=13)

    # Durakları haritaya ekleyin
    for enlem, boylam, isim in duraklar:
        folium.Marker(
            location=[enlem, boylam],
            popup=isim,
            icon=folium.Icon(color="blue", icon="cloud")
        ).add_to(harita)

    en_yakin = None
    mesafe = None
    hata = None

    if request.method == "POST":
        try:
            enlem = float(request.form["enlem"])
            boylam = float(request.form["boylam"])
            kullanici_konumu = (enlem, boylam)

            # En yakın durağı bulma
            en_yakin, mesafe = en_yakin_durak(kullanici_konumu)

            # Kullanıcı konumunu haritada işaretleme
            folium.Marker(
                location=kullanici_konumu,
                popup="Kullanıcı Konumu",
                icon=folium.Icon(color="red", icon="user")
            ).add_to(harita)

            # En yakın durağı işaretleme
            folium.Marker(
                location=[en_yakin[0], en_yakin[1]],
                popup=f"{en_yakin[2]} (En Yakın Durak - {mesafe:.2f} km)",
                icon=folium.Icon(color="green", icon="info-sign")
            ).add_to(harita)
        except ValueError:
            hata = "Lütfen geçerli bir enlem ve boylam girin!"

    # Haritayı statik klasörüne kaydedin
    harita.save("static/harita.html")

    return render_template("index.html", en_yakin=en_yakin, mesafe=mesafe, hata=hata)


if __name__ == "__main__":
    app.run(debug=True)