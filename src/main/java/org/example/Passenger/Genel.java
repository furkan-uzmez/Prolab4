package org.example.Passenger;

public class Genel implements Yolcu{
    private double indirim;
    public Genel(double indirim){
        this.indirim = indirim;
    }
    @Override
    public double get_indirim_orani(){
        return this.indirim;
    }
}
