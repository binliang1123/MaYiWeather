package com.mayi.mayiweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.mayi.mayiweather.WeatherActivity;
import com.mayi.mayiweather.gson.Weather;
import com.mayi.mayiweather.util.HttpUtil;
import com.mayi.mayiweather.util.PreferenceManager;
import com.mayi.mayiweather.util.Utilly;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.content.ContentValues.TAG;

public class AutoUpdateService extends Service {
    public AutoUpdateService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeatherInfo();
        updateBinyPic();
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
//        long hour = 8*60*60*1000;
        long hour = 5*1000;
        long triggleAtTime = SystemClock.elapsedRealtime()+hour;
        PendingIntent pi = PendingIntent.getService(this,0,new Intent(this,AutoUpdateService.class),0);
        am.cancel(pi);
        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggleAtTime,pi);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 获取每日一图
     */
    private void updateBinyPic() {
        String url = "http://guolin.tech/api/bing_pic";

        HttpUtil.sendRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String pic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultPreference(AutoUpdateService.this).edit();
                editor.putString("bing_pic", pic);
                editor.apply();
            }
        });
    }

    /**
     * 更新天气信息
     */
    private void updateWeatherInfo() {
        Log.e(TAG, "updateWeatherInfo:service");
        String weatherInfo = PreferenceManager.getDefaultPreference(this).getString("weather",null);
        if (weatherInfo!=null){
            String weatherId = Utilly.handleWeatherResponse(weatherInfo).basic.weatherId;
            String url = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=a2cd2b3d3f584a5986669169c782bc77";

            HttpUtil.sendRequest(url, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String weatherInfo = response.body().string();
                    final Weather weather = Utilly.handleWeatherResponse(weatherInfo);
                    if (weather != null && "ok".equals(weather.status)) {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultPreference(AutoUpdateService.this).edit();
                        editor.putString("weather", weatherInfo);
                        editor.apply();
                    }
                }
            });
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
