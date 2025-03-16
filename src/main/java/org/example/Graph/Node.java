package org.example.Graph;

import java.util.ArrayList;

public class Node {
    private ArrayList<Edge> edges;
    private final String name;

    public Node(String name){
        this.name = name;
        this.edges = new ArrayList<Edge>();
    }

    public void add_edge(Node end, double mesafe, double sure, double ucret, String baglanti_tipi){
        this.edges.add(new Edge(this,end,mesafe,sure,ucret,baglanti_tipi));
    }

    public String get_name(){
        return this.name;
    }

    public ArrayList<Edge> get_edges() {
        return this.edges;
    }

}