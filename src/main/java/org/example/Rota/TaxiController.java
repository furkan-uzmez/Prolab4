package org.example.Rota;

public class TaxiController extends Vehicle {
    private double openingFee;
    private double costPerKm;

    @Override
    public String get_name(){
        return "taxi";
    }

    @Override
    double get_sure(double distance) {
        return distance*5;
    }

    @Override
    double get_ucret(double distance) {
        return openingFee + (costPerKm*distance);
    }

    public void setCostPerKm(double costPerKm) {
        this.costPerKm = costPerKm;
    }

    public void setOpeningFee(double openingFee) {
        this.openingFee = openingFee;
    }
}
