package org.example.Data;

import org.json.JSONArray;
import org.json.JSONObject;

public class DurakData {
    private JSONArray duraklar;

    public DurakData(Data data){
        setDuraklar(data);
    }

    public JSONArray getDuraklar() {
        return this.duraklar;
    }

    private void setDuraklar(Data data) {
        JSONObject json_data = new JSONObject(data.get_data());
        this.duraklar = (JSONArray) json_data.get("duraklar");
    }

}
