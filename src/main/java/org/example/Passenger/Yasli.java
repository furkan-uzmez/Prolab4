package org.example.Passenger;

public class Yasli implements Yolcu{
    private double indirim;
    public Yasli(double indirim){
        this.indirim = indirim;
    }
    @Override
    public double get_indirim_orani(){
        return this.indirim;
    }
}
