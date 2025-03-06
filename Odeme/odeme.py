from abc import ABC, abstractmethod
class Odeme(ABC):
    def __init__(self, isim):
        self.isim = isim  
    
    @abstractmethod
    def pay(self):
        pass

class Nakit(Odeme):
    def __init__(self, isim):
        super().__init__(isim)
    
    def pay(self):
        pass
class Kredikart(Odeme):
    pass

class Kentkart(Odeme):
    pass

