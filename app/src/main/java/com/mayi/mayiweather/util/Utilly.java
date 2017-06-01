package com.mayi.mayiweather.util;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.mayi.mayiweather.db.City;
import com.mayi.mayiweather.db.County;
import com.mayi.mayiweather.db.Province;
import com.mayi.mayiweather.gson.Weather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017/5/27.
 */

public class Utilly {

    /**
     * 解析省数据
     *
     * @param response
     * @return
     */
    public static boolean handleProvinceResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray array = new JSONArray(response);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceCode(obj.getString("id"));
                    province.setProvinceName(obj.getString("name"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析市数据
     *
     * @param response
     * @return
     */
    public static boolean handleCityResponse(String response, String provinceId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray array = new JSONArray(response);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    City city = new City();
                    city.setCityCode(obj.getString("id"));
                    city.setCityName(obj.getString("name"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析区数据
     *
     * @param response
     * @return
     */
    public static boolean handleCountyResponse(String response, String cityId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray array = new JSONArray(response);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(obj.getString("name"));
                    county.setCityId(cityId);
                    county.setWeatherId(obj.getString("weather_id"));
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析天气
     *
     * @param response
     * @return
     */
    public static Weather handleWeatherResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
                String weatherContent = jsonArray.get(0).toString();
                return new Gson().fromJson(weatherContent,Weather.class);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
