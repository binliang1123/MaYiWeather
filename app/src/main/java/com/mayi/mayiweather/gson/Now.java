package com.mayi.mayiweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2017/5/31.
 */

public class Now {
    public String tmp;

    @SerializedName("cond")
    public More more;

    public class More {
        @SerializedName("txt")
        public String info;
    }

}
