package amap.android_multiple_infowindows;

import android.content.ComponentName;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationClientOption.AMapLocationProtocol;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.animation.Animation;
import com.amap.api.maps.model.animation.RotateAnimation;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;

import overlay.PoiOverlay;


public class MainActivity extends AppCompatActivity
        implements
        AMapLocationListener, LocationSource, AMap.OnMapClickListener, PoiSearch.OnPoiSearchListener {
    private AMap aMap;
    private MapView mapView;
    private LatLng centerpoint1;
    private OnLocationChangedListener mListener;
    private RadioGroup rg_mainBottom;
    private RadioButton rb_mainBottom_mine;
    private RadioButton rb_mainBottom_find;
    private EditText et_search;
    private TextView tvResult;
    LatLng position;
    String shopName = "";
    private static final int MSG_SUCCESS = 0;
    private static final int MSG_FAILURE = 1;

    //声明mlocationClient对象
    public AMapLocationClient mlocationClient;
    //声明mLocationOption对象
    public AMapLocationClientOption mLocationOption = null;

    // 中心点marker
    ViewPoiOverlay poiOverlay;
    private Marker centerMarker;
    private BitmapDescriptor ICON_YELLOW = BitmapDescriptorFactory
            .defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);
    private BitmapDescriptor ICON_RED = BitmapDescriptorFactory
            .defaultMarker(BitmapDescriptorFactory.HUE_RED);
    private MarkerOptions markerOption = null;
    // 中心点坐标
    private LatLng centerLatLng = null;
    private TextView textview_mine = null;

    public static final String URL = "http://192.168.43.75:8080/ServLetTest/";
    public static final String URL_getShopInfo = URL + "getShopInfo";

    public ShopInfo[] shopinfo;
    // 当前的坐标点集合，主要用于进行地图的可视区域的缩放
    LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();


    private Handler mHandler = new Handler() {
        public void handleMessage (Message msg) {//此方法在ui线程运行
            switch(msg.what) {
                case MSG_SUCCESS:
                    shopinfo = analyzeData(msg.obj.toString());
                    for(int i =0; i < shopinfo.length; i++) {
                        if("null" == shopinfo[i].companyName) {
                            continue;
                        }
                        //Draw every location
                        LatLng location = new LatLng(Double.valueOf(shopinfo[i].locationX),Double.valueOf(shopinfo[i].locationY));

                        showDataOnMap(location, shopinfo[i].companyName);
                    }

                    break;

                case MSG_FAILURE:

                    break;
            }
        }
    };


    public class MyAsyncTask extends AsyncTask<String, Integer, String> {
        private String tv;

        public MyAsyncTask(String v) {
            tv = v;
        }

        @Override
        protected void onPreExecute() {
            Log.w("feifei", "task onPreExecute()");
        }
        @Override
        protected String doInBackground(String... params) {
            Log.w("WangJ", "task doInBackground()");
            HttpURLConnection connection = null;
            StringBuilder response = new StringBuilder();
            try {
                java.net.URL url = new URL(params[0]); // 声明一个URL,注意如果用百度首页实验，请使用https开头，否则获取不到返回报文
                connection = (HttpURLConnection) url.openConnection(); // 打开该URL连接
                connection.setRequestMethod("GET"); // 设置请求方法，“POST或GET”，我们这里用GET，在说到POST的时候再用POST
                connection.setConnectTimeout(80000); // 设置连接建立的超时时间
                connection.setReadTimeout(80000); // 设置网络报文收发超时时间
                InputStream in = connection.getInputStream();  // 通过连接的输入流获取下发报文，然后就是Java的流处理
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response.toString(); // 这里返回的结果就作为onPostExecute方法的入参
        }


        @Override
        protected void onProgressUpdate(Integer... values) {
            // 如果在doInBackground方法，那么就会立刻执行本方法
            // 本方法在UI线程中执行，可以更新UI元素，典型的就是更新进度条进度，一般是在下载时候使用
        }

        @Override
        protected void onPostExecute(String s) {
            mHandler.obtainMessage(MSG_SUCCESS,s).sendToTarget();
            if(s.contains("code:200"))
            {
                Intent intent = new Intent();
                ComponentName cn = new ComponentName("amap.android_multiple_infowindows", "amap.android_multiple_infowindows.MainActivity");
                //param1:Activity所在应用的包名
                //param2:Activity的包名+类名
                intent.setComponent(cn);
                startActivity(intent);
            }
        }
    }

    private void ShowAllShop(String shopInfo) {
        String getShopInfoUrlStr = URL_getShopInfo;
        //TextView tvResult = null;
        new MyAsyncTask(shopInfo).execute(getShopInfoUrlStr);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写


        // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        // 注意设置合适的定位时间的间隔（最小间隔支持为1000ms），并且在合适时间调用stopLocation()方法来取消定位请求
        // 在定位结束后，在合适的生命周期调用onDestroy()方法
        // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
        //启动定位
        aMap = mapView.getMap();
        aMap.setLocationSource(MainActivity.this);
        aMap.setMyLocationEnabled(true);
        aMap.getUiSettings().setMyLocationButtonEnabled(true);
        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);

        textview_mine = (TextView) findViewById(R.id.textView1);
        rg_mainBottom = (RadioGroup) findViewById(R.id.radioGroupMainBottom);
        rb_mainBottom_mine = (RadioButton) findViewById(R.id.radio_mine);
        rb_mainBottom_find = (RadioButton) findViewById(R.id.radio_find);
        et_search = (EditText) findViewById(R.id.editText_search);

        //Bottom Bar Change Click
        rg_mainBottom.setOnCheckedChangeListener(new MyRadioButtonListener() );

        //Mine Head Click
        textview_mine.setOnClickListener(new MineClickListener());

        aMap.setOnMapClickListener(MainActivity.this);
        markerOption = new MarkerOptions().draggable(true);

        new Thread(new customViewButton()).start();

        getInfoByDB();
        //showDataOnMap("sad");

    }

    public class customViewButton implements Runnable {

        @Override
        public void run() {

            TextView txv = (TextView) findViewById(R.id.title1);
            txv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                }
            });
        }
    }

    public class ShopInfo {
        String companyName;
        String locationX;
        String locationY;
        String shopDesc;

        public ShopInfo()
        {
            companyName = "0";
            locationX = "0";
            locationY = "0";
            shopDesc = "0";
        }

    }

    public ShopInfo[] analyzeData(String retVal)
    {
        String[] strArr = {};
        strArr = retVal.split(";");
        ShopInfo[] si = new ShopInfo[strArr.length];

        for(int i=0;i< strArr.length;i++) {
            String[] strArrEver = strArr[i].split(",");
            si[i]=new ShopInfo();
            if(strArrEver[0].isEmpty() || strArrEver[0]=="null") {
                continue;
            }
            si[i].companyName = strArrEver[0];
            si[i].locationX = strArrEver[1];
            si[i].locationY = strArrEver[2];
            si[i].shopDesc = strArrEver[3];
        }

        return si;
    }

    private void getInfoByDB()
    {
        ShowAllShop(shopName);
    }

    private void showDataOnMap(LatLng location,String shopName)
    {
        //centerLatLng = new LatLng(30.221750,104.242985);
        addMarker(location, shopName);
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,13));
    }

    private void addMarker(LatLng position, String title) {
        this.position = position;
        if (position != null){
            //初始化marker内容
            MarkerOptions markerOptions = new MarkerOptions();
            //这里很简单就加了一个TextView，根据需求可以加载复杂的View
            View view = View.inflate(this, R.layout.custom_view, null);
            TextView textView = ((TextView) view.findViewById(R.id.title1));
            textView.setText(title);
            BitmapDescriptor markerIcon = BitmapDescriptorFactory.fromView(view);
            markerOptions.position(position).icon(markerIcon);
            markerOptions.setFlat(true);//设置marker平贴地图效果
            //markerOptions.icon(ICON_YELLOW);
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

    @Override
    public void onMapClick(LatLng latLng) {
        markerOption.icon(ICON_YELLOW);
        markerOption.title(shopName);
        centerLatLng = latLng;
        addCenterMarker(centerLatLng);

        //System.out.println("center point" + centerLatLng.latitude+ "-----" + centerLatLng.longitude);
        //drawCircle(centerLatLng);

    }

    private void transDataToRegNewShop()
    {
        Intent intent = new Intent();
        ComponentName cn = new ComponentName("amap.android_multiple_infowindows", "amap.android_multiple_infowindows.AddShop");
        //param1:Activity所在应用的包名
        //param2:Activity的包名+类名
        intent.setComponent(cn);

        /* 通过Bundle对象存储需要传递的数据 */
        Bundle bundle = new Bundle();
        /*字符、字符串、布尔、字节数组、浮点数等等，都可以传*/
        bundle.putDouble("locationX", centerLatLng.latitude);
        bundle.putDouble("locationY", centerLatLng.longitude);

        /*把bundle对象assign给Intent*/
        intent.putExtras(bundle);

        startActivity(intent);
    }

    private void transDataToShopDetails()
    {
        Intent intent = new Intent();
        ComponentName cn = new ComponentName("amap.android_multiple_infowindows", "amap.android_multiple_infowindows.ShopDetails");
        //param1:Activity所在应用的包名
        //param2:Activity的包名+类名
        intent.setComponent(cn);


        /* 通过Bundle对象存储需要传递的数据 */
        Bundle bundle = new Bundle();
        /*字符、字符串、布尔、字节数组、浮点数等等，都可以传*/
        //bundle.putDouble("shopDesc", shopinfo[]);

        /*把bundle对象assign给Intent*/
        intent.putExtras(bundle);

        startActivity(intent);
    }

    private void addCenterMarker(LatLng latlng) {
        if(null == centerMarker){
            centerMarker = aMap.addMarker(markerOption);
        }
        centerMarker.setPosition(latlng);
        centerMarker.setVisible(true);
    }

    private void drawCircle(LatLng centerPoint) {
        // 绘制一个圆形
        aMap.addCircle(new CircleOptions().center(centerPoint)
                .radius(100).strokeColor(Const.STROKE_COLOR)
                .fillColor(Const.FILL_COLOR).strokeWidth(Const.STROKE_WIDTH));
        boundsBuilder.include(centerPoint);

        // 设置所有maker显示在当前可视区域地图中
        LatLngBounds bounds = boundsBuilder.build();
        aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));

    }

    class MineClickListener implements TextView.OnClickListener
    {
        @Override
        public void onClick(View view)
        {
            Intent intent = new Intent();
            ComponentName cn = new ComponentName("amap.android_multiple_infowindows", "amap.android_multiple_infowindows.RegisterActivity");
            //param1:Activity所在应用的包名
            //param2:Activity的包名+类名
            intent.setComponent(cn);
            startActivity(intent);
        }
    }

    class MyRadioButtonListener implements RadioGroup.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            // 选中状态改变时被触发
            switch (checkedId) {
                case R.id.radio_mine:

                    if(centerLatLng != null)
                    {
                        transDataToRegNewShop();
                    }
                    else
                    {
                        Toast.makeText(MainActivity.this, "请在地图上选择点", Toast.LENGTH_SHORT).show();
                    }
                    break;

                case R.id.radio_find:
                    Log.i("RadioGroup", "当前用户选择"+rb_mainBottom_find.getText().toString());
                    Intent intent_find = new Intent();
                    ComponentName cn_find = new ComponentName("amap.android_multiple_infowindows", "amap.android_multiple_infowindows.ShopDetails");
                    //param1:Activity所在应用的包名
                    //param2:Activity的包名+类名
                    intent_find.setComponent(cn_find);
                    startActivity(intent_find);
                    break;
                default:
                    Log.i("RadioGroup", "当前用户选择 未实现");

            }
        }
    }

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                mListener.onLocationChanged(amapLocation);
                //定位成功回调信息，设置相关消息
                amapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
                amapLocation.getLatitude();//获取纬度
                amapLocation.getLongitude();//获取经度
                amapLocation.getAccuracy();//获取精度信息
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date(amapLocation.getTime());
                df.format(date);//定位时间
                System.out.println("local point" + amapLocation.getLatitude() + "-----" + amapLocation.getLongitude());
            } else {
                //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                Log.e("AmapError","location Error, ErrCode:"
                        + amapLocation.getErrorCode() + ", errInfo:"
                        + amapLocation.getErrorInfo());
            }
        }
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        System.out.println("has actived");
        mListener=onLocationChangedListener;
        if(mlocationClient == null){
            /*get your current location  and send to sql to get shop name and info around you*/
            mlocationClient = new AMapLocationClient(this);
            //初始化定位参数
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mlocationClient.setLocationListener(this);
            //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
            mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
            //设置定位间隔,单位毫秒,默认为2000ms
            mLocationOption.setInterval(2000);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            mlocationClient.startLocation();
        }


    }

    @Override
    public void deactivate() {
        mlocationClient = null;
    }



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
            TextView textView = ((TextView) view.findViewById(R.id.title1));
            textView.setText(getTitle(index));

            return  BitmapDescriptorFactory.fromView(view);
        }
    }

}

