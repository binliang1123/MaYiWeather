package com.mayi.mayiweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2017/5/31.
 */

public class Forecast {

    public String date;

    @SerializedName("tmp")
    public Temperature temperature;
    public Cond cond;

    public class Cond {
        @SerializedName("txt_d")
        public String info;
    }

    public class Temperature {
        public String max;
        public String min;
    }

}
