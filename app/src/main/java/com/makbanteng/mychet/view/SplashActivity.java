package com.makbanteng.mychet.view;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.makbanteng.mychet.model.AudienceNetworkInitializeHelper;
import com.facebook.ads.AdSettings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.makbanteng.mychet.Config.LINK_Json;
import static com.makbanteng.mychet.Config.TESTMODE_FAN;
import static com.facebook.ads.AdSettings.IntegrationErrorMode.INTEGRATION_ERROR_CRASH_DEBUG_MODE;
import com.makbanteng.mychet.R;
import com.makbanteng.mychet.Config;
public class SplashActivity extends AppCompatActivity {
private TextView errTEXT;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AudienceNetworkInitializeHelper.initialize(this);
        //AdSettings.addTestDevice("09b9f07b-54a2-4ecc-bdb1-78991d41cee3");
        AdSettings.setTestMode(TESTMODE_FAN);
        AdSettings.setIntegrationErrorMode(INTEGRATION_ERROR_CRASH_DEBUG_MODE);
        setContentView(R.layout.activity_splash);
        errTEXT = findViewById(R.id.texError);

            if (checkConnectivity()){
                loadUrlData();
            }

        /** Creates a count down timer, which will be expired after 5000 milliseconds */
        new CountDownTimer(5000, 1000) {

            /** This method will be invoked on finishing or expiring the timer */
            @Override
            public void onFinish() {
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onTick(long millisUntilFinished) {

            }
        }.start();

    }

    private void loadUrlData() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                LINK_Json, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {

                    JSONObject jsonObject = new JSONObject(response);

                    JSONArray array = jsonObject.getJSONArray("Iklan");

                    for (int i = 0; i < array.length(); i++){

                        JSONObject c = array.getJSONObject(i);
                        Config.STATUS = c.getString("status");
                        Config.LINK = c.getString("link");
                        Config.ADMOB_INTER = c.getString("interid");
                        Config.ADMOB_BANNER = c.getString("bannerid");
                        Config.FAN_INTER = c.getString("fan_inter");
                        Config.FAN_NATIVE =c.getString("fan_native");
                        Config.FAN_BANNER = c.getString("fan_banner");
                        Config.FAN_BANNER_BIG = c.getString("fan_banner_big");
                        Config.STRATAPPID = c.getString("startappid");
                        Config.PENGATURAN_IKLAN = c.getString("pengaturan_iklan");


                    }
                    Log.i("IKLANID","IKLANID splash: "+Config.ON_OFF_JSON+" : "+Config.FAN_NATIVE+" : "+ Config.FAN_BANNER);
                    errTEXT.setVisibility(View.GONE);

                } catch (JSONException e) {
                    errTEXT.setVisibility(View.VISIBLE);
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                //  Toast.makeText(getActivity(), "Error" + error.toString(), Toast.LENGTH_SHORT).show();

            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(SplashActivity.this);
        requestQueue.add(stringRequest);
    }


    private boolean checkConnectivity() {
        boolean enabled = true;

        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();

        if ((info == null || !info.isConnected() || !info.isAvailable())) {
            // Toast.makeText(getApplicationContext(), "Sin conexión a Internet...", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }


    }

}