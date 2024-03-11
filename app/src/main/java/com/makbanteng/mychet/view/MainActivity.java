package com.makbanteng.mychet.view;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.ump.ConsentForm;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.FormError;
import com.google.android.ump.UserMessagingPlatform;


import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.makbanteng.mychet.Config;
import com.makbanteng.mychet.R;
import com.makbanteng.mychet.interfaces.ChangeServer;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.makbanteng.mychet.model.DataProccessor;


public class MainActivity extends AppCompatActivity {
    private FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
    private ConsentInformation consentInformation;
    private ConsentForm consentForm;
    private Fragment fragment;
    private DrawerLayout drawer;
    private ChangeServer changeServer;
    ActionBarDrawerToggle toggle;
    private DataProccessor dataProccessor;
//    TextView privtext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dataProccessor = new DataProccessor(this);
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        ConsentRequestParameters params = new ConsentRequestParameters.Builder().build();

        consentInformation = UserMessagingPlatform.getConsentInformation(this);
        consentInformation.requestConsentInfoUpdate(
                this,
                params,
                new ConsentInformation.OnConsentInfoUpdateSuccessListener() {
                    @Override
                    public void onConsentInfoUpdateSuccess() {
                        // The consent information state was updated.
                        // You are now ready to check if a form is available.
                        if (consentInformation.isConsentFormAvailable()) {
                            loadForm();
                        }
                    }
                },
                new ConsentInformation.OnConsentInfoUpdateFailureListener() {
                    @Override
                    public void onConsentInfoUpdateFailure(FormError formError) {
                        // Handle the error.
                    }
                });

         FirebaseAnalytics.getInstance(this);

        initializeAll();

        ImageButton changeServer = findViewById(R.id.change_server);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NavigationView navView = findViewById(R.id.nav_view);

        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        toggle.syncState();
        navView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.mnu1:
                    changeServer();
                    break;
                case R.id.mnu2:
                    dataProccessor.setStr("infopriv","privacy");
                    Intent prIntent = new Intent(MainActivity.this, PrivacyPolicyActivity.class);
                    startActivity(prIntent);
                    break;
                case R.id.mnu3:
                    dataProccessor.setStr("infopriv","terms");
                    Intent myIntent = new Intent(MainActivity.this, PrivacyPolicyActivity.class);
                    startActivity(myIntent);
                    break;
                case R.id.mnu5:
                    dataProccessor.setStr("infopriv","terms");
                    break;
                case R.id.mnu4:
                    Intent intentMarket = new Intent(Intent.ACTION_VIEW);
                    intentMarket.setData(Uri.parse("market://details?id=com.makbanteng.mychet"));
                    startActivity(intentMarket);
                    break;
            }
            drawer.closeDrawer(Gravity.LEFT);
            return false;
        });



        changeServer.setOnClickListener(v -> changeServer());
//        privtext.setOnClickListener(view -> {
//            Intent intent = new Intent(Intent.ACTION_VIEW);
//            intent.setData(Uri.parse("https://vpnhub.ngaplikasi.com/privacy-policy-2/"));
//            startActivity(intent);
//        });

        transaction.add(R.id.container, fragment);
        transaction.commit();

    }

    public void changeServer() {
        startActivityForResult(new Intent(this, SelectVPNActivity.class), 2);
    }

    public void loadForm(){
        UserMessagingPlatform.loadConsentForm(
                this,
                new UserMessagingPlatform.OnConsentFormLoadSuccessListener() {
                    @Override
                    public void onConsentFormLoadSuccess(ConsentForm consentForm) {
                        MainActivity.this.consentForm = consentForm;
                        if(consentInformation.getConsentStatus() == ConsentInformation.ConsentStatus.REQUIRED) {
                            consentForm.show(
                                    MainActivity.this,
                                    new ConsentForm.OnConsentFormDismissedListener() {
                                        @Override
                                        public void onConsentFormDismissed(@Nullable FormError formError) {
                                            // Handle dismissal by reloading form.
                                            loadForm();
                                        }
                                    });

                        }

                    }
                },
                new UserMessagingPlatform.OnConsentFormLoadFailureListener() {
                    @Override
                    public void onConsentFormLoadFailure(FormError formError) {
                        /// Handle Error.
                    }
                }
        );
    }

    private void initializeAll() {
        drawer = findViewById(R.id.drawer_layout);
        TextView titleTv =findViewById(R.id.titleTv);
        fragment = new MainFragment();
        changeServer = (ChangeServer) fragment;
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        LinearLayout adContainer = headerView.findViewById(R.id.adLine);
        AdView adView = new AdView(MainActivity.this);
        adView.setAdSize(AdSize.MEDIUM_RECTANGLE);
        adView.setAdUnitId(Config.ADMOB_BANNER);
        adView.loadAd(new AdRequest.Builder().build());
        adContainer.addView(adView);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) return true;

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 2 && resultCode == Activity.RESULT_OK) {
            assert data != null;
            changeServer.newServer(data.getParcelableExtra("ovpn"));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
