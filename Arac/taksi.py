from dataclasses import dataclass,field
from .arac import Arac

@dataclass
class Taksi(Arac):
    opening_fee : float = field(default = 0.0)
    cost_per_km : float = field(default = 0.0)
    
    def ucret_hesapla(self,mesafe):
        pass

if __name__ == "__main__":
    print("Hello")