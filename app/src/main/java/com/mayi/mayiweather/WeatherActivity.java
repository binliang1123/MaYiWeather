package com.mayi.mayiweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.mayi.mayiweather.gson.Forecast;
import com.mayi.mayiweather.gson.Weather;
import com.mayi.mayiweather.service.AutoUpdateService;
import com.mayi.mayiweather.util.HttpUtil;
import com.mayi.mayiweather.util.PreferenceManager;
import com.mayi.mayiweather.util.Utilly;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private static final String TAG = WeatherActivity.class.getSimpleName();
    private ScrollView weatherLayout;

    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private TextView aqi;
    private TextView pm25;
    private TextView comfort;
    private TextView carwash;
    private TextView sport;

    private LinearLayout forecastLayout;

    private ImageView biYinImg;
    public SwipeRefreshLayout swipeLayout;
    public DrawerLayout drawerLayout;
    private Button navBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 21) {
            View dv = getWindow().getDecorView();
            dv.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_weather);

        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        aqi = (TextView) findViewById(R.id.aqi);
        pm25 = (TextView) findViewById(R.id.pm25);
        comfort = (TextView) findViewById(R.id.comfort);
        carwash = (TextView) findViewById(R.id.car_wash);
        sport = (TextView) findViewById(R.id.sprot);
        biYinImg = (ImageView) findViewById(R.id.biyin_img);
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        swipeLayout.setColorSchemeResources(R.color.colorPrimary);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navBtn = (Button) findViewById(R.id.choose_city);
        navBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });


        SharedPreferences preferences = PreferenceManager.getDefaultPreference(this);
        String weatherInfo = preferences.getString("weather", null);
        final String weatherId;
        if (!TextUtils.isEmpty(weatherInfo)) {
            Weather weather = Utilly.handleWeatherResponse(weatherInfo);
            weatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        } else {
            weatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            reqeustWeatherInfoFromServer(weatherId);
        }

        String biyinImgPic = preferences.getString("bing_pic", null);
        if (!TextUtils.isEmpty(biyinImgPic)) {
            Glide.with(this).load(biyinImgPic).into(biYinImg);
        } else {
            loadBiyingPic();
        }
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reqeustWeatherInfoFromServer(weatherId);
            }
        });


    }


    /**
     * 展示天气信息
     *
     * @param weather
     */
    private void showWeatherInfo(Weather weather) {

        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.tmp + "℃";
        String weatherInfo = weather.now.more.info;

        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);

        forecastLayout.removeAllViews();
        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(WeatherActivity.this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView date = (TextView) view.findViewById(R.id.date);
            TextView info = (TextView) view.findViewById(R.id.info);
            TextView max = (TextView) view.findViewById(R.id.max);
            TextView min = (TextView) view.findViewById(R.id.min);

            date.setText(forecast.date);
            info.setText(forecast.cond.info);
            max.setText(forecast.temperature.max);
            min.setText(forecast.temperature.min);

            forecastLayout.addView(view);
        }

        if (weather.aqi != null) {
            aqi.setText(weather.aqi.city.aqi);
            pm25.setText(weather.aqi.city.pm25);
        }

        comfort.setText("舒适指数：" + weather.suggestion.comfort.info);
        carwash.setText("洗车指数：" + weather.suggestion.carWash.info);
        sport.setText("运动建议：" + weather.suggestion.sport.info);

        weatherLayout.setVisibility(View.VISIBLE);

        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    /**
     * 去服务器请求数据
     *
     * @param id
     */
    public void reqeustWeatherInfoFromServer(String id) {
        Log.e(TAG, "reqeustWeatherInfoFromServer: " );
        String url = "http://guolin.tech/api/weather?cityid=" + id + "&key=a2cd2b3d3f584a5986669169c782bc77";

        HttpUtil.sendRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取数据失败", Toast.LENGTH_SHORT).show();

                    }
                });
                swipeLayout.setRefreshing(false);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String weatherInfo = response.body().string();
                final Weather weather = Utilly.handleWeatherResponse(weatherInfo);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultPreference(WeatherActivity.this).edit();
                            editor.putString("weather", weatherInfo);
                            editor.apply();
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取数据失败", Toast.LENGTH_SHORT).show();
                        }
                        swipeLayout.setRefreshing(false);
                    }
                });
            }
        });

        loadBiyingPic();
    }


    /**
     * 加载必应图片
     */
    private void loadBiyingPic() {

        String url = "http://guolin.tech/api/bing_pic";

        HttpUtil.sendRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String pic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultPreference(WeatherActivity.this).edit();
                editor.putString("bing_pic", pic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(pic).into(biYinImg);
                    }
                });
            }
        });
    }
}
