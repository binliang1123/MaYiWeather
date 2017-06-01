package com.mayi.mayiweather.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;

/**
 * Created by Administrator on 2017/5/31.
 */

public class PreferenceManager {

    private static SharedPreferences preference;

    public static SharedPreferences getDefaultPreference(Context context) {
        if (preference == null) {
            preference = context.getSharedPreferences("mayi_weather_sharedF", Context.MODE_PRIVATE);
        }
        return preference;
    }


}
