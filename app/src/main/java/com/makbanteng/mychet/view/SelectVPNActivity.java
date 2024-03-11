package com.makbanteng.mychet.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.makbanteng.mychet.Config;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.makbanteng.mychet.R;
import com.makbanteng.mychet.adapter.ServerListRVAdapter;
import com.makbanteng.mychet.interfaces.NavItemClickListener;
import com.makbanteng.mychet.model.DataProccessor;
import com.makbanteng.mychet.model.Server;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;



public class SelectVPNActivity extends AppCompatActivity implements NavItemClickListener {
    private ArrayList<Server> serverLists;
    private RecyclerView serverListRv;
    private DataProccessor dataProccessor;
    private com.facebook.ads.AdView bannerAdView;
    private AdView adView;
    private RelativeLayout bannerAdContainer;
    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataProccessor =new DataProccessor(this);
        MobileAds.initialize(this.getApplicationContext(), getString(R.string.Admob_app_id));
        setContentView(R.layout.activity_select_v_p_n);
        setSupportActionBar(findViewById(R.id.toolbar));
        bannerAdContainer = (RelativeLayout)findViewById(R.id.banner_container);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }


        serverListRv = findViewById(R.id.serverListRv);
        serverListRv.setHasFixedSize(true);
        serverListRv.setLayoutManager(new LinearLayoutManager(this));

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("vpn").orderBy("country").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                serverLists = new ArrayList<>();
                List<DocumentSnapshot> doc = task.getResult().getDocuments();
                DocumentSnapshot best = doc.get(new Random().nextInt(doc.size()));
                String firstUsername = best.getString("username");
                String firstpassword = best.getString("password");
                serverLists.add(
                        new Server(
                                "Best vpn server",
                                Config.gbSIGNAL,
                                Objects.requireNonNull(best.getString("ovpn")).replace("\\n", "\n"), firstUsername, firstpassword
                        )
                );
                if (serverLists != null) {
                    ServerListRVAdapter serverListRVAdapter = new ServerListRVAdapter(serverLists, this);
                    serverListRv.setAdapter(serverListRVAdapter);
                }
                for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                    String username = document.getString("username");
                    String password = document.getString("password");
                    if (username == null) username = "";
                    if (password == null) password = "";
                    serverLists.add(new Server(
                            document.getString("country"),
                            document.getString("logo"),
                            Objects.requireNonNull(document.getString("ovpn")).replace("\\n", "\n"),
                            username,
                            password
                    ));
                }
            }
        });
        switch (Config.PENGATURAN_IKLAN) {
            case "1":
                loadBanner();
                //disable blum disetting layout rencana di dialog
                //BannerAdmobMedium();
                break;
            case "2":
                //disable blum disetting layout rencana di dialog
                bannerfan();
                //bannerfanBesar();
                break;
            case "3":
                break;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    private void loadBanner() {
        AdRequest adRequest =
                new AdRequest.Builder() .build();

        AdSize adSize = getAdSize();
        adView.setAdSize(adSize);
        adView.loadAd(adRequest);
    }

    private AdSize getAdSize() {
        Display display = SelectVPNActivity.this.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;
        int adWidth = (int) (widthPixels / density);
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(SelectVPNActivity.this, adWidth);
    }
    private void bannerfan(){
        if(Config.FAN_BANNER.equals("YOUR_PLACEMENT_ID")) {
            Log.e("IKLANID","IKLANID error"+Config.FAN_BANNER);
        }else {
            bannerAdView= new com.facebook.ads.AdView(SelectVPNActivity.this, Config.FAN_BANNER, com.facebook.ads.AdSize.BANNER_HEIGHT_50);
            bannerAdContainer.addView(bannerAdView);
            bannerAdView.loadAd();
        }

    }
    @Override
    public void clickedItem(int index) {
        Intent result = new Intent();
        result.putExtra("ovpn", serverLists.get(index));

        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString("country", serverLists.get(index).getCountry());
        Log.i("serverinf","serverinf "+serverLists.get(index).getCountry());
        mFirebaseAnalytics.logEvent("vpn_connect", bundle);
        dataProccessor.setStr("SERVER_COUNTRY",serverLists.get(index).getCountry());
        dataProccessor.setStr("SERVER_FLAG",serverLists.get(index).getFlagUrl());
        dataProccessor.setStr("SERVER_OVPN",serverLists.get(index).getOvpn());dataProccessor.setStr("SERVER_OVPN_USER",serverLists.get(index).getOvpnUserName());
        dataProccessor.setStr("SERVER_OVPN_PASSWORD",serverLists.get(index).getOvpnUserPassword());
        setResult(Activity.RESULT_OK, result);
        finish();

    }
}