from flask import Flask, jsonify, render_template, request
from Yardımcı.durak import Durak
from Yardımcı.konum import Konum
from Yardımcı.rotahesaplayıcı import RotaHesaplayici
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

rota = RotaHesaplayici(veri)

taksi.opening_fee = taksi_veri['openingFee'] 
taksi.cost_per_km = taksi_veri['costPerKm']

durak.durak_verisi = veri['duraklar']

durak.set_duraklar()

duraklar = durak.duraklar

@app.route("/", methods=["GET", "POST"])
def home():
    print("Index.html çalıştırılıyor...")  # Debugging için

    # Varsayılan değerler
    baslangic_enlem = 40.7669   # İzmit için varsayılan değer
    baslangic_boylam = 29.9169
    hedef_enlem = 40.7669
    hedef_boylam = 29.9169
    hata = None
    en_yakin = None
    
    if request.method == "POST":
        print("POST request processing started")  # Debug: Confirm POST handling
        data = request.form
        print("Form data received:", dict(data))  # Debug: Log form data
        try:
            baslangic_enlem = float(data.get('baslangic_enlem', baslangic_enlem))
            baslangic_boylam = float(data.get('baslangic_boylam', baslangic_boylam))
            hedef_enlem = float(data.get('hedef_enlem', hedef_enlem))
            hedef_boylam = float(data.get('hedef_boylam', hedef_boylam))
            print(f"Parsed coordinates: ({baslangic_enlem}, {baslangic_boylam}) to ({hedef_enlem}, {hedef_boylam})")
            
            sonuc = rota.rota_bul_koordinatlarla(
                baslangic_enlem, baslangic_boylam, hedef_enlem, hedef_boylam, "sure"
            )
            # Rota açıklamasını göster
            rota.rota_aciklama_goster(sonuc)
            
            # JSON olarak kaydet (örnek)
            with open("rota_sonuc.json", "w", encoding="utf-8") as f:
                json.dump(sonuc, f, ensure_ascii=False, indent=2)
            print("Rota 'rota_sonuc.json' dosyasına kaydedildi.")
            
            # For POST, return JSON response to frontend
            return jsonify(sonuc)
        
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
            nearest_stop=en_yakin,
            hata = hata
        )


if __name__ == "__main__":
    app.run(debug=True)