import json
import os
from Arac.arac import Arac
from Arac.taksi import Taksi
file_path = "veriseti.json"
if os.path.exists(file_path):
    with open(file_path, "r", encoding="utf-8") as dosya:
        veri = json.load(dosya)
    print('Veri yüklendi')
else:
    print(f"File {file_path} does not exist.")

taksi_veri = veri['taxi']

Taksi.opening_fee = taksi_veri['openingFee']
Taksi.cost_per_km = taksi_veri['costPerKm']