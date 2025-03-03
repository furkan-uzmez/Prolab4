import json
import os

file_path = "veriseti.json"
if os.path.exists(file_path):
    with open(file_path, "r", encoding="utf-8") as dosya:
        veri = json.load(dosya)
    print(veri)
else:
    print(f"File {file_path} does not exist.")