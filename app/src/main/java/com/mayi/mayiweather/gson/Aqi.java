package com.mayi.mayiweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2017/5/31.
 */

public class Aqi {

    public AqiCity city;
    public class AqiCity {

        public String aqi;
        public String pm25;
    }

}
