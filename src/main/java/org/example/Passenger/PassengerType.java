package org.example.Passenger;

public enum PassengerType {
    Genel(new Genel()),
    Ogrenci(new Ogrenci()),
    Yasli(new Yasli());

    Yolcu passenger_type;

    PassengerType(Yolcu passenger_type){
        this.passenger_type = passenger_type;
    }

    // String türündeki girişleri daha güvenli çevirmek için bir yardımcı metot
    public static Yolcu fromString(String text) {
        for (PassengerType pt : PassengerType.values()) {
            if (pt.name().equalsIgnoreCase(text)) {
                return pt.passenger_type;
            }
        }
        throw new IllegalArgumentException("Geçersiz yolcu türü: " + text);
    }

}
