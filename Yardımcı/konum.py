from dataclasses import dataclass
class Konum():
    enlem : float
    boylam : float
    
    def set_konum(self,enlem,boylam):
        self.enlem = enlem
        self.boylam = boylam

    def get_konum(self):
        return (self.enlem,self.boylam)
    