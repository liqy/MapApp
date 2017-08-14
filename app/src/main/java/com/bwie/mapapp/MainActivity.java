package com.bwie.mapapp;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {


    TextView hello;
    MapView mMapView = null;

    //定位类
    public LocationClient mLocationClient = null;

    //监听类
    public MyLocationListenner myListener = new MyLocationListenner();

    boolean isFirstLoc = true;

    // 是否首次定位
    private MyLocationData locData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        hello = (TextView) findViewById(R.id.hello);

        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.bmapView);

        // 开启定位图层
        mMapView.getMap().setMyLocationEnabled(true);

        // 定位初始化
        mLocationClient = new LocationClient(this);

        //注册监听事件
        mLocationClient.registerLocationListener(myListener);

        /**
         * 初始化
         */
        initLocation();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
        // 关闭定位图层
        mMapView.getMap().setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(0);
        option.setIsNeedAddress(true);
        option.setIsNeedLocationPoiList(true);
        mLocationClient.setLocOption(option);
    }

    @NeedsPermission({Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION})
    void location() {
        Toast.makeText(this, "开始定位", Toast.LENGTH_SHORT).show();
        mLocationClient.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
        MainActivityPermissionsDispatcher.locationWithCheck(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mLocationClient != null) {
            mLocationClient.stop();
        }
        mMapView.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    /**
     * 定位SDK监听函数
     */
    class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {

            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) {
                return;
            }

            //设置位置信息
            hello.setText(location.getAddrStr());

            //初始化我的位置信息
            locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(0).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();

            //展示我的我的位置
            mMapView.getMap().setMyLocationData(locData);

            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(18.0f);
                mMapView.getMap().animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
        }

    }

}
