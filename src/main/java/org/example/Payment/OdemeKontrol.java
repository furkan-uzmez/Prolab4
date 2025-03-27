package org.example.Payment;

import org.example.Passenger.Yolcu;

import java.util.ArrayList;
import java.util.Map;

public class OdemeKontrol {
    public void kontrol(Map<String,Object> route, Odeme odeme, Yolcu yolcu,double bakiye){
        ArrayList index = new ArrayList();
        for (String key:route.keySet()){
            index.add(key);
        }
//        index.add("1");
//        index.add("2");
//        index.add("3");
//        index.add("taxi");

        for(int i = 0; i<route.size() ;i++){
            Map<String,Object> rota = (Map<String, Object>) route.get(index.get(i));
            double toplam_ucret = (Double) rota.get("toplam_ucret");
            double kalan = odeme.ode(bakiye, toplam_ucret ,yolcu.get_indirim_orani());

            if(kalan<0){
                rota.put("bakiye_yeterli",false);
            }
            else {
                rota.put("bakiye_yeterli",true);
                rota.put("kalan_bakiye",kalan);
            }
        }

    }
}
