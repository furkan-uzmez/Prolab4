from .arac import Arac

class Taksi(Arac):
    def __init__(self):
        self.opening_fee = 0
        self.cost_per_km = 0

    def set_opening_fee(self,opening_fee):
        self.opening_fee = opening_fee

    def get_opening_fee(self):
        return self.opening_fee 

    def set_cost_per_km(self,cost_per_km):
        self.cost_per_km = cost_per_km

    def get_cost_per_km(self):
        return self.cost_per_km 

    def ucret_hesapla(self,mesafe):
        pass

if __name__ == "__main__":
    print("Hello")