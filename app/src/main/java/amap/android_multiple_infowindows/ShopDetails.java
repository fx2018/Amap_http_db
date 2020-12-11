package amap.android_multiple_infowindows;

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

            Button btnSubmit = (Button) findViewById(R.id.button_buy);
            btnSubmit.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
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
