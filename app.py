from flask import Flask, render_template, request
import folium
from geopy.distance import geodesic
from Yardımcı.durak import Durak
from Yardımcı.konum import Konum
from Arac.taksi import Taksi
import json
import os

app = Flask(__name__)

file_path = "veriseti.json"
if os.path.exists(file_path):
    with open(file_path, "r", encoding="utf-8") as dosya:
        veri = json.load(dosya)
    print('Veri yüklendi')
else:
    print(f"File {file_path} does not exist.")

taksi_veri = veri['taxi']

durak = Durak()
konum = Konum()
taksi = Taksi()

taksi.opening_fee = taksi_veri['openingFee']
taksi.cost_per_km = taksi_veri['costPerKm']

durak.set_durak_verisi(veri['duraklar'])

duraklar = []
for durak_ in veri['duraklar']:
    duraklar.append((durak_['lat'] , durak_['lon'] , durak_['name']))


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
            konum.set_konum(enlem,boylam)
            kullanici_konumu = konum.get_konum()

            # En yakın durağı bulma
            en_yakin, mesafe = durak.en_yakin_durak(kullanici_konumu)
            
            print(en_yakin,mesafe)

            konum.display_location(harita,kullanici_konumu)
            
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