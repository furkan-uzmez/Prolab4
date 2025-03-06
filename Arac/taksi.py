from .arac import Arac

class Taksi(Arac):
    def __init__(self):
        self.opening_fee = 0
        self.cost_per_km = 0

    def ucret_hesapla(self,mesafe):
        return self.opening_fee + (self.cost_per_km * mesafe)

if __name__ == "__main__":
    print("Hello")