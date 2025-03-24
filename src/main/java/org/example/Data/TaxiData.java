package org.example.Data;

import com.fasterxml.jackson.databind.JsonNode;

public class TaxiData {
    private final JsonNode taxiData;
    private final double openingFee;
    private final double costPerKm;

    public TaxiData(JsonNode taxiData) {
        this.taxiData = taxiData;
        this.openingFee = taxiData.get("openingFee").asDouble();
        this.costPerKm = taxiData.get("costPerKm").asDouble();
    }

    public double getCostPerKm() {
        return this.costPerKm;
    }

    public double getOpeningFee() {
        return this.openingFee;
    }

}
