package com.myweather.android.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Machenike on 2018/2/16.
 */

public class Weather {
    public String status;

    public Basic basic;

    public AQI aqi;

    public Now now;

    public Suggestion suggestion;

    //Forecast定义的单日天气，用List集合来引用表示未来多天天气
    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;
}
