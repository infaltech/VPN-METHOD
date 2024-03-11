package com.makbanteng.mychet.view;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.makbanteng.mychet.R;
import com.makbanteng.mychet.model.DataProccessor;


public class PrivacyPolicyActivity extends AppCompatActivity {
    WebView webView;
    ProgressBar mProgressBar;
    private static final String urlpriv = "https://shufflevpn.blogspot.com/p/privacy-policy.html";
    private static final String urlterm = "https://shufflevpn.blogspot.com/p/privacy-policy.html";
    private DataProccessor dataProccessor;
    TextView judul;
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);
        dataProccessor = new DataProccessor(this);
        judul =(TextView) findViewById(R.id.title);
        webView = (WebView) findViewById(R.id.webView);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);

        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        if (Build.VERSION.SDK_INT >= 19) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
        else {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        webView.getSettings().setJavaScriptEnabled(true);
        // Tiga baris di bawah ini agar laman yang dimuat dapat melakukan zoom.
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        if(TextUtils.isEmpty(dataProccessor.getStr("infopriv"))) {
            Log.e("ERROR","Error Empty priv");
        }else{
            if(dataProccessor.getStr("infopriv").equalsIgnoreCase("privacy")){
                webView.loadUrl(urlpriv);
                judul.setText(getResources().getString(R.string.priv_c));
            }else{
                webView.loadUrl(urlterm);
                judul.setText(getResources().getString(R.string.term_c));
            }
        }


        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                //ProgressBar akan Terlihat saat Halaman Dimuat
                mProgressBar.setVisibility(View.VISIBLE);
                webView.setVisibility(View.GONE);

            }

        });
        webView.setWebViewClient(new WebViewClient(){

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {

            }

            public void onLoadResource(WebView view, String url) {

                try {
                    webView.loadUrl("javascript:(window.onload = function() { " +
                            "(elem1 = document.getElementById('header-container')); elem.parentNode.removeChild(elem1); " +
                            "(elem2 = document.getElementById('footer')); elem2.parentNode.removeChild(elem2); " +
                            "})()");
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            public void onPageFinished(WebView view, String url) {
                //ProgressBar akan Menghilang setelah Halaman Selesai Dimuat
                super.onPageFinished(view, url);
                mProgressBar.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
                try {
                    view.loadUrl("javascript:(window.onload = function() { " +
                            "(elem1 = document.getElementById('header')); elem.parentNode.removeChild(elem1); " +
                            "(elem2 = document.getElementById('footer')); elem2.parentNode.removeChild(elem2); " +
                            "})()");
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }

    @Override
    public void onBackPressed () {

        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            finish();
        }
    }

}
