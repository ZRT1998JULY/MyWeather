package com.myweather.android;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ScrollingView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.bumptech.glide.Glide;
import com.myweather.android.gson.Weather;
import com.myweather.android.util.HttpUtil;
import com.myweather.android.util.Utility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;



public class MainActivity extends AppCompatActivity {

    public LocationClient mLocationClient;
    private String location;//

    private TextView positionText;
    private TextView mycity;
    private TextView myprovince;
    private TextView findway;
    private TextView mylocation;

    private ImageView bingMainImg;

    private MapView mapView;
    private BaiduMap baiduMap;
    private boolean isFirstLocate=true;
    private ScrollView mainlayout;

    private String weatherId;

    private Boolean refresh;

    private TextView mytemp;

    private String mWeatherId;

    public void setLocation(String location){//
        this.location=location;//
    }//

    public String getLocation(){//
        return location;//
    }//


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
        if(prefs.getString("weather",null)!=null){
            SharedPreferences.Editor editor=prefs.edit();
            editor.putString("weather",null);
            editor.apply();
        }

        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }


        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());

        //SDKInitializer.initialize(getApplicationContext());

       setContentView(R.layout.activity_main);

       // mapView=(MapView)findViewById(R.id.bmapView) ;
       // baiduMap=mapView.getMap();
       // baiduMap.setMyLocationEnabled(true);

        //mainlayout=(ScrollView)findViewById(R.id.main_layout);
        positionText = (TextView) findViewById(R.id.position_text_view);
        mycity = (TextView) findViewById(R.id.my_city);
        mylocation = (TextView) findViewById(R.id.my_location);
        myprovince = (TextView) findViewById(R.id.my_province);
        findway = (TextView) findViewById(R.id.find_way);
        bingMainImg=(ImageView)findViewById(R.id.bing_main_img);
        mytemp=(TextView)findViewById(R.id.my_temp);

        //mainlayout.setVisibility(View.VISIBLE);
        String bingPic = prefs.getString("bing_pic", null);
        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(bingMainImg);
        } else {
            loadBingPic();
        }


        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String [] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        } else {
            requestLocation();
        }
    }

   /* private void navigateTo(BDLocation location){
        if(isFirstLocate){
            LatLng ll=new LatLng(location.getLatitude(),location.getLongitude());
            //MapStatusUpdate update=MapStatusUpdateFactory.newLatLng(ll);
            //baiduMap.animateMapStatus(update);
            //update=MapStatusUpdateFactory.zoomTo(16f);
           // baiduMap.animateMapStatus(update);
           // isFirstLocate=false;
            MapStatus mMapStatus=new MapStatus.Builder()
                    .target(ll)
                    .zoom(18)
                    .build();
            MapStatusUpdate mMapStatusUpdate=MapStatusUpdateFactory.newMapStatus(mMapStatus);
            baiduMap.animateMapStatus(mMapStatusUpdate);
        }
        MyLocationData.Builder locationBuilder=new MyLocationData.Builder();
        locationBuilder.latitude(location.getLatitude());
        locationBuilder.longitude(location.getLongitude());
        MyLocationData locationData=locationBuilder.build();
        baiduMap.setMyLocationData(locationData);
    }*/

    private void requestLocation() {
        initLocation();
        mLocationClient.start();
    }

    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        //option.setScanSpan(5000);
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
    }
    /*@Override
    protected void onResume(){
        super.onResume();
       // mapView.onResume();
    }
    /*@Override
    protected void onPause(){
        super.onPause();
        mapView.onPause();
    }*/


    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
       // mapView.onDestroy();
      //  baiduMap.setMyLocationEnabled(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有权限才能使用本程序", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {

            final String mlocation=location.getDistrict(); //
            setLocation(mlocation);  //
            /*StringBuilder currentPosition = new StringBuilder();
            currentPosition.append("纬度：").append(location.getLatitude()).append("\n");
            currentPosition.append("经线：").append(location.getLongitude()).append("\n");
           currentPosition.append("国家：").append(location.getCountry()).append("\n");
            currentPosition.append("省：").append(location.getProvince()).append("\n");
            currentPosition.append("市：").append(location.getCity()).append("\n");
            currentPosition.append("区：").append(location.getDistrict()).append("\n");            currentPosition.append("街道：").append(location.getStreet()).append("\n");
            currentPosition.append("定位方式：");
            if (location.getLocType() == BDLocation.TypeGpsLocation) {
                currentPosition.append("GPS");
            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
               currentPosition.append("网络");
            }
            positionText.setText(currentPosition);*/
            StringBuilder currentPosition1 = new StringBuilder();
            StringBuilder currentPosition2 = new StringBuilder();
            StringBuilder currentPosition3 = new StringBuilder();
            StringBuilder currentPosition4 = new StringBuilder();
            StringBuilder currentPosition5 = new StringBuilder();
            currentPosition1.append(location.getCity());//mycity
            currentPosition2.append(location.getProvince());//myprovince
            currentPosition3.append("具体地址：").append(location.getCountry()).append(location.getProvince()).
                    append(location.getCity()).append(location.getDistrict());//myposition
            currentPosition4.append("纬度：").append(location.getLatitude()).append("\n");
            currentPosition4.append("经线：").append(location.getLongitude());//positionText
            if (location.getLocType() == BDLocation.TypeGpsLocation) {
                currentPosition5.append("GPS");
            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
                currentPosition5.append("网络");
            }
            mycity.setText(currentPosition1);
            myprovince.setText(currentPosition2);
            mylocation.setText(currentPosition3);
            positionText.setText(currentPosition4);
            findway.setText(currentPosition5);


           // setTemp();

           /* if(location.getLocType()==BDLocation.TypeGpsLocation||location.getLocType()
                    ==BDLocation.TypeNetWorkLocation){
                navigateTo(location);
            }*/
        }


    }

    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(MainActivity.this).load(bingPic).into(bingMainImg);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });
    }



  /*  private void setTemp(){
        WeatherActivity activity=new WeatherActivity();
        findCounty(location);
         weatherId=findWeatherId();
         activity.requestWeather(weatherId);
          Weather MainWeather=activity.ReturnWeather();
          String degree = MainWeather.now.temperature + "℃";
         mytemp.setText(degree);
    }

    private void findCounty(final String location) {
        String address = "https://free-api.heweather.com/s6/search?location=" + location +
                "&key=b0993c8c443641b4aecaf5612abe667d";
        HttpUtil.sendOkHttpRequest(address, new Callback() {
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        final String responseText = response.body().string();
                        final Weather weather = Utility.handleWeatherResponse(responseText);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (weather != null && "ok".equals(weather.status)) {
                                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
                                    editor.putString("weather", responseText);
                                    editor.apply();
                                    mWeatherId = weather.basic.weatherId;
                                } else {
                                    Toast.makeText(MainActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }



                /*final String locationText = response.body().string();
                refresh = false;
                Log.e("findCounty", locationText);
                try {
                    JSONObject jsonObject = new JSONObject(locationText);
                    JSONArray jsonArray = jsonObject.getJSONArray("HeWeather6");
                    JSONObject i = jsonArray.getJSONObject(0);
                    JSONObject a = i.getJSONObject("basic");
                    String weatherID = a.getString("cid");
                    if (weatherID != null) {
                        SharedPreferences.Editor editor=getSharedPreferences("data",MODE_PRIVATE).edit();
                        editor.putString("weatherID", weatherID);
                        editor.apply();
                        Log.e("weatherID", weatherID);
                        refresh = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }*/

   /*     });

    }*/

  /*  private String findWeatherId() {
        String weatherID = "CN101010100";
        SharedPreferences prefs = getSharedPreferences("data",MODE_PRIVATE);
//        while(!refresh) {
      //      weatherID = prefs.getString("weatherID", "CN101010100");
      //  }
   //     Log.e("tag", weatherID);
        return weatherID;
    }*/


}
