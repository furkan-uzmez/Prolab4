from flask import Flask, render_template, request
import folium
from Yardımcı.durak import Durak
from Yardımcı.konum import Konum
from Yardımcı.displayer import Displayer
from Arac.arac import Taksi
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
displayer = Displayer()

taksi.opening_fee = taksi_veri['openingFee'] 
taksi.cost_per_km = taksi_veri['costPerKm']

durak.durak_verisi = veri['duraklar']

durak.set_duraklar()

duraklar = durak.duraklar

@app.route("/", methods=["GET", "POST"])
def home():
    print("Index.html çalıştırılıyor...")  # Debugging için

    # Harita oluştur
    harita = folium.Map(location=[40.7651, 29.9406], zoom_start=13)

    # Durakları haritaya ekleyin
    displayer.display_duraklar(harita,duraklar)

    en_yakin = None
    mesafe = None
    hata = None

    if request.method == "POST":
        try:
            enlem = float(request.form["enlem"])
            boylam = float(request.form["boylam"])
            konum.baslangic_enlem , konum.baslangic_boylam = enlem , boylam
            #konum.hedef_enlem , konum.hedef_boylam = hedef_enlem , hedef_boylam
            kullanici_konumu = (konum.baslangic_enlem , konum.baslangic_boylam)

            # En yakın durağı bulma
            en_yakin, mesafe = durak.en_yakin_durak(kullanici_konumu)
            
            print(en_yakin,mesafe)

            # Kullanıcı konumunu haritada işaretleme
            displayer.display_user_location(harita,kullanici_konumu)
            
            # En yakın durağı işaretleme
            displayer.display_nearest_durak(harita,en_yakin,mesafe)
        except ValueError:
            hata = "Lütfen geçerli bir enlem ve boylam girin!"

    # Haritayı statik klasörüne kaydedin
    harita.save("static/harita.html")

    return render_template("index.html", en_yakin=en_yakin, mesafe=mesafe, hata=hata)


if __name__ == "__main__":
    app.run(debug=True)