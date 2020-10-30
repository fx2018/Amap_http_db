package amap.android_multiple_infowindows;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.animation.Animation;
import com.amap.api.maps.model.animation.RotateAnimation;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import java.sql.Connection;
import java.sql.*;


import java.util.HashMap;
import java.util.List;

import overlay.PoiOverlay;

import static java.sql.DriverManager.println;

public class MainActivity extends Activity implements PoiSearch.OnPoiSearchListener {
    private AMap aMap;
    private MapView mapView;
    private LatLng centerpoint1;
    private LatLng centerpoint2;
    private LatLng centerpoint3;
    private LatLng centerpoint4;

    private ViewPoiOverlay poiOverlay;
    private LatLng position;

    private TextView tv_data;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what){
                case 0x11:
                    String s = (String) msg.obj;
                    tv_data.setText(s);
                    break;
                case 0x12:
                    String ss = (String) msg.obj;
                    tv_data.setText(ss);
                    break;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*
         * 设置离线地图存储目录，在下载离线地图或初始化地图设置;
         * 使用过程中可自行设置, 若自行设置了离线地图存储的路径，
         * 则需要在离线地图下载和使用地图页面都进行路径设置
         * */
        //Demo中为了其他界面可以使用下载的离线地图，使用默认位置存储，屏蔽了自定义设置
        // MapsInitializer.sdcardDir =OffLineMapUtils.getSdCacheDir(this);
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        init();
        doPOISearch();
    }

    private void LoadParaFromDB() {

        centerpoint1 = new LatLng(39.983178,114.464348);
        centerpoint2 = new LatLng(37.983178,116.464348);
        centerpoint3 = new LatLng(40.983178,120.464348);
        centerpoint4 = new LatLng(36.983178,110.464348);


    }

    private void doPOISearch() {

        connectDB();

        addMarker(centerpoint1, "Beef");
        addMarker(centerpoint2, "Chicken");
        addMarker(centerpoint3, "Rice");
        addMarker(centerpoint4, "Sara");



        /*
        PoiSearch.Query query = new PoiSearch.Query("公园","110101","成都");
        query.setPageSize(10);// 设置每页最多返回多少条poiitem
        query.setPageNum(1);
        query.requireSubPois(true);
        PoiSearch poiSearch = new PoiSearch(this,query);
        poiSearch.setOnPoiSearchListener(this);
        poiSearch.setBound(new PoiSearch.SearchBound(centerpoint1, 5000, true));//
        // 设置搜索区域为以lp点为圆心，其周围5000米范围
        poiSearch.searchPOIAsyn();// 异步搜索
        */
    }

    private void setStyle() {

    }

    private void connectDB() {

        HashMap<String, Object> map = DBUtil.getInfoByName("1");

        if(map != null){
            String s = "";
            for (String key : map.keySet()){
                s += key + ":" + map.get(key) + "\n";
                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG);
                println(s);
            }
            //message.what = 0x12;
            //message.obj = s;
        }else {
            Toast.makeText(getApplicationContext(),"error: 查询结果为空", Toast.LENGTH_SHORT);
            println("error");
        }
        /*
        // 创建一个线程来连接数据库并获取数据库中对应表的数据
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 调用数据库工具类DBUtils的getInfoByName方法获取数据库表中数据
                HashMap<String, Object> map1 = DBUtil.getInfoByName("1");
                HashMap<String, Object> map2 = DBUtil.getInfoByName("2");

                if(!map1.isEmpty() && !map2.isEmpty())
                {

                }
                /*
                Message message = handler.obtainMessage();
                if(map != null){
                    String s = "";
                    for (String key : map.keySet()){
                        s += key + ":" + map.get(key) + "\n";
                    }
                    message.what = 0x12;
                    message.obj = s;
                }else {
                    message.what = 0x11;
                    message.obj = "查询结果为空";
                }
                // 发消息通知主线程更新UI
                handler.sendMessage(message);

            }
        }).start();
        */

    }

    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
        }

        LoadParaFromDB();

        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centerpoint1,13));
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centerpoint2,13));
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centerpoint3,13));
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centerpoint4,13));
    }

    private void addMarker(LatLng position, String title) {
        this.position = position;
        if (position != null){
            //初始化marker内容
            MarkerOptions markerOptions = new MarkerOptions();
            //这里很简单就加了一个TextView，根据需求可以加载复杂的View
            View view = View.inflate(this, R.layout.custom_view, null);
            TextView textView = ((TextView) view.findViewById(R.id.title));
            textView.setText(title);
            BitmapDescriptor markerIcon = BitmapDescriptorFactory.fromView(view);
            markerOptions.position(position).icon(markerIcon);
            markerOptions.setFlat(true);//设置marker平贴地图效果
            markerOptions.draggable(true);
            Animation animation = new RotateAnimation(markerOptions.getRotateAngle(),markerOptions.getRotateAngle()+180,0,0,0);
            long duration = 1000L;
            animation.setDuration(duration);
            animation.setInterpolator(new LinearInterpolator());

            aMap.addMarker(markerOptions);
            aMap.addMarker(markerOptions).setAnimation(animation);
            aMap.addMarker(markerOptions).startAnimation();
        }
    }

    //搜索返回结果回调
    @Override
    public void onPoiSearched(PoiResult poiResult, int errorCode) {
        if (errorCode == 1000) {
            if (poiResult != null && poiResult.getQuery() != null) {

                List<PoiItem> poiItems = poiResult.getPois();
                if (poiItems != null && poiItems.size() > 0) {
                    aMap.clear();// 清理之前的图标
                    poiOverlay = new ViewPoiOverlay(aMap, poiItems);
                    poiOverlay.removeFromMap();
                    poiOverlay.addToMap();
                    poiOverlay.zoomToSpan();
                } else {
                    Toast.makeText(MainActivity.this, "无搜索结果", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "无搜索结果", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }
    /**
     * 把LatLonPoint对象转化为LatLon对象
     */
    public static LatLng convertToLatLng(LatLonPoint latLonPoint) {
        if (latLonPoint ==null){
            return null;
        }
        return new LatLng(latLonPoint.getLatitude(), latLonPoint.getLongitude());
    }

    public class ViewPoiOverlay extends PoiOverlay {

        public ViewPoiOverlay(AMap aMap, List<PoiItem> list) {
            super(aMap, list);
        }

        @Override
        protected BitmapDescriptor getBitmapDescriptor(int index) {
            View view = null;
            view = View.inflate(MainActivity.this, R.layout.custom_view, null);
            TextView textView = ((TextView) view.findViewById(R.id.title));
            textView.setText(getTitle(index));

            return  BitmapDescriptorFactory.fromView(view);
        }
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();

    }
    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }
    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }


}
