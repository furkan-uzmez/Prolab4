from abc import ABC, abstractmethod
class Arac(ABC):  
    def __init__(self, hat_adi):
        self.hat_adi = hat_adi

    @abstractmethod
    def ucret_hesapla(self, mesafe):
        """Araç için ücret hesaplamalı"""
        pass

if __name__ == "__main__":
    print("Hello")