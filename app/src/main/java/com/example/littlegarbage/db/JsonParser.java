package com.example.littlegarbage.db;

import com.example.littlegarbage.bean.GarbageBean;
import com.google.gson.Gson;

public class JsonParser {
    public GarbageBean GarbageParse (String garbageString) {

        Gson gson = new Gson();
        GarbageBean garbageBean = gson.fromJson(garbageString, GarbageBean.class);
        if (garbageBean.getResult().message.equals("success")) {
            return garbageBean;
        } else {
            return null;
        }

    }

}
