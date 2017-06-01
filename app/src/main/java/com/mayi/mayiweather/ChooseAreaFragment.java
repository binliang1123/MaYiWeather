package com.mayi.mayiweather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mayi.mayiweather.db.City;
import com.mayi.mayiweather.db.County;
import com.mayi.mayiweather.db.Province;
import com.mayi.mayiweather.util.HttpUtil;
import com.mayi.mayiweather.util.Utilly;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 选择地区
 */

public class ChooseAreaFragment extends Fragment {

    private static final int LEVEL_PROVINCE = 0;
    private static final int LEVEL_CITY = 1;
    private static final int LEVEL_COUNTY = 2;

    private int currentLevel;
    private Button backBtn;
    private ListView list;
    private TextView titleView;
    private ArrayAdapter<String> adapter;
    private List<String> datas = new ArrayList<>();

    private Province selectProvince;
    private City selectCity;

    private List<Province> provinces;
    private List<City> cities;
    private List<County> counties;

    private ProgressDialog progressDialog;

    public static String domain = "http://guolin.tech/api/china";

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area,null);
        backBtn = (Button) view.findViewById(R.id.backBtn);
        list = (ListView) view.findViewById(R.id.list);
        titleView = (TextView) view.findViewById(R.id.text);
        adapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_list_item_1,datas);
        list.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE){
                    selectProvince = provinces.get(position);
                    queryCity();
                }else if (currentLevel == LEVEL_CITY){
                    selectCity = cities.get(position);
                    queryCounty();
                } else if (currentLevel == LEVEL_COUNTY){
                    String weatherId = counties.get(position).getWeatherId();
                   if (getActivity() instanceof WeatherActivity){
                        WeatherActivity weatherActivity = (WeatherActivity) getActivity();
                       weatherActivity.drawerLayout.closeDrawers();
                       weatherActivity.swipeLayout.setRefreshing(true);
                       weatherActivity.reqeustWeatherInfoFromServer(weatherId);
                   }else {
                       Intent intent = new Intent(getActivity(),WeatherActivity.class);
                       intent.putExtra("weather_id",weatherId);
                       startActivity(intent);
                       getActivity().finish();
                   }
                }
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel == LEVEL_COUNTY){
                    queryCity();
                }else if (currentLevel == LEVEL_CITY){
                    queryProvince();
                }
            }
        });

        queryProvince();
    }

    /**
     * 查询省份
     */
    private void queryProvince() {

        titleView.setText("中国");
        backBtn.setVisibility(View.GONE);
        provinces = DataSupport.findAll(Province.class);
        if (provinces.size() >0){
            datas.clear();
            for (Province province : provinces) {
                datas.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            list.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        }else {
            requestFromServer(domain,LEVEL_PROVINCE);
        }
    }


    /**
     * 查询县级
     */
    private void queryCounty() {
        titleView.setText(selectCity.getCityName());
        backBtn.setVisibility(View.VISIBLE);
        counties = DataSupport.where("cityId = ?",selectCity.getId()+"").find(County.class);
        if (counties.size() >0){
            datas.clear();
            for (County county : counties) {
                datas.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            list.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }else {
            String url = domain+"/"+selectProvince.getProvinceCode()+"/"+selectCity.getCityCode();
            requestFromServer(url,LEVEL_COUNTY);
        }
    }

    /**
     * 查询城市
     */
    private void queryCity() {
        backBtn.setVisibility(View.VISIBLE);
        titleView.setText(selectProvince.getProvinceName());
        cities = DataSupport.where("provinceId = ?",selectProvince.getId()+"").find(City.class);
        if (cities.size() >0){
            datas.clear();
            for (City city : cities) {
                datas.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            list.setSelection(0);
            currentLevel = LEVEL_CITY;
        }else {

            requestFromServer(domain+"/"+selectProvince.getProvinceCode(),LEVEL_CITY);
        }
    }

    /**
     * 从服务器上获取数据
     * @param url
     */
    private void requestFromServer(String url, final int level) {
        showDialog();
        HttpUtil.sendRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissDialog();
                        Toast.makeText(getActivity(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                boolean result = false;
                String responseText = response.body().string();
                if (level == LEVEL_PROVINCE){
                    result = Utilly.handleProvinceResponse(responseText);
                }else if (level == LEVEL_CITY){
                    result = Utilly.handleCityResponse(responseText,selectProvince.getId()+"");
                }else if (level == LEVEL_COUNTY){
                    result = Utilly.handleCountyResponse(responseText,selectCity.getId()+"");
                }

                if (result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dismissDialog();
                            if (level == LEVEL_PROVINCE){
                                queryProvince();
                            }else if (level == LEVEL_CITY){
                                queryCity();
                            }else if (level == LEVEL_COUNTY){
                                queryCounty();
                            }
                        }
                    });
                }
            }
        });
    }

    private void showDialog(){
        if (progressDialog == null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("加载中。。。");
        }
        progressDialog.show();
    }

    private void dismissDialog(){
        if (progressDialog!=null&&progressDialog.isShowing())
            progressDialog.dismiss();
    }
}
