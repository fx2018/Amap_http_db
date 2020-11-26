package amap.android_multiple_infowindows;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
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

import java.sql.Date;
import java.text.SimpleDateFormat;

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


public class MainActivity extends AppCompatActivity
        implements
        AMapLocationListener, LocationSource, AMap.OnMapClickListener {
    private AMap aMap;
    private MapView mapView;
    private LatLng centerpoint1;
    private OnLocationChangedListener mListener;
    private RadioGroup rg_mainBottom;
    private RadioButton rb_mainBottom_mine;
    private RadioButton rb_mainBottom_find;
    private EditText et_search;

    //声明mlocationClient对象
    public AMapLocationClient mlocationClient;
    //声明mLocationOption对象
    public AMapLocationClientOption mLocationOption = null;

    // 中心点marker
    private Marker centerMarker;
    private BitmapDescriptor ICON_YELLOW = BitmapDescriptorFactory
            .defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);
    private BitmapDescriptor ICON_RED = BitmapDescriptorFactory
            .defaultMarker(BitmapDescriptorFactory.HUE_RED);
    private MarkerOptions markerOption = null;
    // 中心点坐标
    private LatLng centerLatLng = null;
    private TextView textview_mine = null;

    // 当前的坐标点集合，主要用于进行地图的可视区域的缩放
    LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();


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

        //注意是给RadioGroup绑定监视器
        rg_mainBottom.setOnCheckedChangeListener(new MyRadioButtonListener() );
        textview_mine.setOnClickListener(new MineClickListener());

        aMap.setOnMapClickListener(MainActivity.this);
        markerOption = new MarkerOptions().draggable(true);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        markerOption.icon(ICON_YELLOW);
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
}

