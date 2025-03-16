package org.example.Passenger;

public class Ogrenci implements Yolcu{
    private double indirim;
    public Ogrenci(double indirim){
        this.indirim = indirim;
    }

    @Override
    public double get_indirim_orani(){
        return this.indirim;
    }
}
