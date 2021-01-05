package amap.android_multiple_infowindows;

import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import static android.view.View.SCROLLBARS_OUTSIDE_OVERLAY;

public class ShopDetails extends AppCompatActivity {

    WebView webView;
    String uri = "http://player.bilibili.com/player.html?aid=542806233&bvid=BV1Xi4y1L76s&cid=257770318";
    //<iframe src="//player.bilibili.com/player.html?aid=543067851&bvid=BV18i4y1572g&cid=262686005&page=1" scrolling="no" border="0" frameborder="no" framespacing="0" allowfullscreen="true"> </iframe>
    //String uri = "https://www.bilibili.com/video/BV1oZ4y1n7vc";
    String time_1 = "t=58";
    String uri_real = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_details);
        new Thread(new ListenBuyButton()).start();

        /*获取Intent中的Bundle对象*/
        Bundle bundle = this.getIntent().getExtras();

        /*获取Bundle中的数据，注意类型和key*/
        String shopDescp = bundle.getString("shopDesc");

        if(shopDescp!="")
        {
            uri = shopDescp.toString();
        }

        webView = (WebView)this.findViewById(R.id.webview);

        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress){
                if(newProgress==100){
                    // 这里是设置activity的标题， 也可以根据自己的需求做一些其他的操作
                    //title.setText("加载完成");
                }else{
                    //title.setText("加载中.......");

                }
            }
        });

        webView.setWebViewClient(new WebViewClient(){

           @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //重写此方法表明点击网页里面的链接还是在当前的webview里跳转，不跳到浏览器那边
                view.loadUrl(url);
                return true;
            }

/*
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if(url.startsWith("http:") || url.startsWith("https:") ) {
                    view.loadUrl(url);
                    return false;
                }else{
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    return true;
                }
            }
*/

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, android.net.http.SslError error) {
                // 重写此方法可以让webview处理https请求
                handler.proceed();
                //super.onReceivedSslError(view, handler, error);
            }
        });

        webView.getSettings().setJavaScriptEnabled(true);

        loadURI(uri);
    }


    private void loadURI(String uri)
    {
        //load URI
        webView.loadUrl(uri);

    }

    private String getItemName()
    {
        return "";
    }

    private String getShopID()
    {
        return "";
    }

    private String getUserName()
    {
        return "";
    }

    private String getUserAddr()
    {
        return "";
    }

    private boolean callAliPay()
    {
        return false;
    }

    public void submitDataToMeiTuan(String username, String shopid, String itemname, String useraddr)
    {

    }

    public class ListenBuyButton implements Runnable {

        @Override
        public void run() {

            Button btnBuy = (Button) findViewById(R.id.button_buy);
            btnBuy.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    uri_real = uri;
                    loadURI(uri_real);
                    webView.reload();
                    btnBuy.setText("Buy");
                    submitDataToMeiTuan(getUserName(),getShopID(),getItemName(),getUserAddr());
                    if(!callAliPay())
                    {
                        Toast.makeText(ShopDetails.this, "buy sccuess！", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
