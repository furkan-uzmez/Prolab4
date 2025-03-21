package org.example.Payment;


public enum PaymentType {
    Nakit(new Nakit()),
    Kredikart(new Kredikart()),
    Kentkart(new Kentkart());

    Odeme payment_type;
    PaymentType(Odeme payment_type){
        this.payment_type = payment_type;
    }

    // String türündeki girişleri daha güvenli çevirmek için bir yardımcı metot
    public static PaymentType fromString(String text) {
        for (PaymentType pt : PaymentType.values()) {
            if (pt.name().equalsIgnoreCase(text)) {
                return pt;
            }
        }
        throw new IllegalArgumentException("Geçersiz ödeme türü: " + text);
    }
}
