from flask import Flask, render_template, request
import folium
from Yardımcı.durak import Durak
from Yardımcı.konum import Konum
from Yardımcı.displayer import Displayer
from Arac.arac import Taksi
import json
import os
from dotenv import load_dotenv

# .env dosyasını yükleyin
load_dotenv()

# API anahtarını çevresel değişkenden alın
google_maps_api_key = os.getenv("GOOGLE_MAPS_API_KEY")

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


    # Varsayılan değerler
    baslangic_enlem = 41.0082  # İstanbul için varsayılan değer
    baslangic_boylam = 28.9784
    hedef_enlem = 41.0082
    hedef_boylam = 28.9784
    hata = None

    if request.method == "POST":
        try:
            baslangic_enlem = float(request.form["baslangic_enlem"])
            baslangic_boylam = float(request.form["baslangic_boylam"])
            hedef_enlem = float(request.form["hedef_enlem"])
            hedef_boylam = float(request.form["hedef_boylam"])
            
        except ValueError:
            hata = "Lütfen geçerli bir enlem ve boylam girin!"
            return render_template("index.html", hata=hata)


    return render_template("index.html",
            baslangic_enlem=baslangic_enlem,
            baslangic_boylam=baslangic_boylam,
            hedef_enlem=hedef_enlem,
            hedef_boylam=hedef_boylam,
            api_key=google_maps_api_key,
            duraklar=durak.durak_verisi,
            hata = hata
        )


if __name__ == "__main__":
    app.run(debug=True)