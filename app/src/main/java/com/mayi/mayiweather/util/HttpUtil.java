package com.mayi.mayiweather.util;

import android.util.Log;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;


/**
 * Created by Administrator on 2017/5/27.
 */

public class HttpUtil {

    private static final String TAG = HttpUtil.class.getSimpleName();

    public static void sendRequest(String url, Callback callback) {
        Log.e(TAG, "url: " + url);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(callback);
    }
}
