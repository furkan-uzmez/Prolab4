package org.example.FrontEnd;

import org.example.Passenger.Yolcu;
import org.example.Payment.Odeme;

public class FrontEndInfo {
    double bakiye;
    Yolcu yolcu_turu;
    Odeme odeme_turu;

    public FrontEndInfo(Double bakiye,Yolcu yolcu_turu,Odeme odeme_turu){
        this.bakiye=bakiye;
        this.yolcu_turu=yolcu_turu;
        this.odeme_turu=odeme_turu;
    }

    public double getBakiye() {
        return bakiye;
    }

    public Yolcu getYolcu_turu() {
        return yolcu_turu;
    }

    public Odeme getOdeme_turu() {
        return odeme_turu;
    }

}
