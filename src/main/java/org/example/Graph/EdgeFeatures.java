package org.example.Graph;

import org.jgrapht.graph.DefaultWeightedEdge;

public class EdgeFeatures extends DefaultWeightedEdge {
    private double mesafe;
    private double sure;
    private double ucret;
    private String type;
    private String baglanti_tipi;

    public EdgeFeatures(double mesafe, double sure, double ucret, String type, String baglanti_tipi) {
        this.mesafe = mesafe;
        this.sure = sure;
        this.ucret = ucret;
        this.type = type;
        this.baglanti_tipi = baglanti_tipi;
    }

    public double getMesafe() { return mesafe; }
    public double getSure() { return sure; }
    public double getUcret() { return ucret; }
    public String getBaglanti_tipi() { return baglanti_tipi; }
}
