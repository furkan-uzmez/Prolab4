from abc import ABC, abstractmethod
class Yolcu(ABC):
    def __init__(self, isim):
        self.isim = isim  

    @abstractmethod
    def indirim_orani(self):
        pass

class Genel(Yolcu):
    pass

class Yasli(Yolcu):
    pass

class Ogrenci(Yolcu):
    pass