import requests
class Durak():
    def __init__(self):
        self.durak_verisi = {}
        self.duraklar = []
    
    def set_duraklar(self):
        for durak_ in self.durak_verisi:
            self.duraklar.append((durak_['lat'] , durak_['lon'] , durak_['name']))
    
    def en_yakin_durak(self, kullanici_konumu, API_KEY):
        # Routes API URL
        url = "https://routes.googleapis.com/distanceMatrix/v2:computeRouteMatrix"
        
        headers = {
            "X-Goog-Api-Key": API_KEY,
            "Content-Type": "application/json",
            "X-Goog-FieldMask": "originIndex,destinationIndex,distanceMeters,duration"
        }
        
        # Create origins and destinations in the correct format
        origins = [{
            "waypoint": {
                "location": {
                    "latLng": {
                        "latitude": kullanici_konumu[0],
                        "longitude": kullanici_konumu[1]
                    }
                }
            }
        }]
        
        destinations = []
        for durak in self.duraklar:
            destinations.append({
                "waypoint": {
                    "location": {
                        "latLng": {
                            "latitude": durak[0],
                            "longitude": durak[1]
                        }
                    }
                }
            })
        
        # Build request body
        body = {
            "origins": origins,
            "destinations": destinations,
            "travelMode": "WALK"
        }
        
        # Send request
        response = requests.post(url, json=body, headers=headers)
        if response.status_code != 200:
            print(f"Hata: {response.status_code} - {response.text}")
            return None
        
        # Parse response
        result = response.json()
        distances = []
        
        
        for element in result:
            if "destinationIndex" in element and "distanceMeters" in element:
                idx = element["destinationIndex"]
                if idx < len(self.duraklar):
                    durak = self.duraklar[idx]
                    distance_m = element["distanceMeters"]
                    distances.append({
                        "name": durak[2],
                        "lat": durak[0],
                        "lon": durak[1],
                        "distance_m": distance_m,
                        "distance_text": f"{distance_m / 1000:.2f} km"
                    })
        
        if not distances:
            print("Hiçbir durak için mesafe hesaplanamadı.")
            return None
        
        # Find the nearest stop
        nearest_stop = min(distances, key=lambda x: x["distance_m"])
        print(f"En yakın durak: {nearest_stop['name']}, Mesafe: {nearest_stop['distance_text']}")
        return nearest_stop