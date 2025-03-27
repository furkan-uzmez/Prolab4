package org.example.Rota;


public class VehicleManager {
    private Vehicle vehicle_type;

    public void setVehicle_type(double distance){
        if(distance<=3){
            this.vehicle_type = new WalkingController();
        }
        else {
            this.vehicle_type = new TaxiController();
        }
    }

    public Vehicle getVehicle_type(){
        return this.vehicle_type;
    }

}
