from abc import ABC, abstractmethod
class Yolcu(ABC):
    def __init__(self, isim):
        self.isim = isim  

    @abstractmethod
    def indirim_orani(self):
        pass

if __name__ == "__main__":
    print("Hello")