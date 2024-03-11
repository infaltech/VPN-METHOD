package com.makbanteng.mychet.view;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.VpnService;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.ump.ConsentDebugSettings;
import com.google.android.ump.ConsentForm;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.FormError;
import com.google.android.ump.UserMessagingPlatform;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.startapp.sdk.ads.banner.Banner;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.StartAppSDK;
import com.makbanteng.mychet.CheckInternetConnection;
import com.makbanteng.mychet.Config;
import com.makbanteng.mychet.R;
import com.makbanteng.mychet.SharedPreference;
import com.makbanteng.mychet.databinding.FragmentMainBinding;
import com.makbanteng.mychet.interfaces.ChangeServer;
import com.makbanteng.mychet.model.DataProccessor;
import com.makbanteng.mychet.model.MyService;
import com.makbanteng.mychet.model.Server;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.TimeZone;

import de.blinkt.openvpn.OpenVpnApi;
import de.blinkt.openvpn.core.OpenVPNService;
import de.blinkt.openvpn.core.OpenVPNThread;
import de.blinkt.openvpn.core.VpnStatus;

import static android.app.Activity.RESULT_OK;
import static com.makbanteng.mychet.Config.FAN_NATIVE;

import static com.google.android.gms.ads.RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE;
import static com.makbanteng.mychet.Config.FAN_BANNER;
import static com.makbanteng.mychet.Config.STRATAPPID;

@SuppressLint("SetTextI18n")
public class MainFragment extends Fragment implements View.OnClickListener, ChangeServer {
    private String currentVersion;
    /*
      variabel BANNER FAN
       */
    private com.facebook.ads.AdView bannerAdView;
    private RelativeLayout bannerAdContainer;
    private NativeAdLayout nativeAdLayout;
    private LinearLayout faView;
    private NativeAd nativeAd;

    /*
    variable BANNER Admob
     */
    private AdView adView;
    private InterstitialAd mInterstitialAd;
    int ctad_counter;
    /*
    Big banner untuk Admob, fan dan startapp
     */
    private RelativeLayout bannerLayout;

    private Server server;
    private CheckInternetConnection connection;

    private OpenVPNThread vpnThread = new OpenVPNThread();
    private OpenVPNService vpnService = new OpenVPNService();
    boolean vpnStart = false;
    private SharedPreference preference;

    public static com.facebook.ads.InterstitialAd interstitialAdfb;
    /*
GDRP
 */
    private ConsentInformation consentInformation;
    private ConsentForm consentForm;

    private FragmentMainBinding binding;
    private View fragView;
    private View fbview;
    Animation btnAnim;
    ObjectAnimator animY;
    private DataProccessor dataProccessor;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //json iklan

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false);
        View view = binding.getRoot();
        fbview = inflater.inflate(R.layout.native_ad_layout,container,false);
        fragView = view;
        dataProccessor = new DataProccessor(getActivity());
        btnAnim =  AnimationUtils.loadAnimation(getActivity(),R.anim.bouncelipse);
        animY = ObjectAnimator.ofFloat(binding.ellipse0, "translationY", -100f, 0f);
        animY.setDuration(1000);//1sec
        animY.setInterpolator(new BounceInterpolator());
        animY.setRepeatCount(Animation.INFINITE);
        try{ ctad_counter= dataProccessor.getInt("hitunginterst"); }catch (Exception e){ Log.e("intersads","dimulai nol");ctad_counter=0;}

        initializeAll();


        return view;
    }

    /**
     * Initialize all variable and object
     */
    private void initializeAll() {
        preference = new SharedPreference(Objects.requireNonNull(getContext()));
        server = preference.getServer();
        String namaserver = dataProccessor.getStr("SERVER_COUNTRY");
        if (namaserver == null) {
            Log.d("FRAGMENT", "Init ");
            FirebaseFirestore.getInstance().collection("vpn").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    List<DocumentSnapshot> doc = Objects.requireNonNull(task.getResult()).getDocuments();
                    DocumentSnapshot best = doc.get(new Random().nextInt(doc.size()));
                    String username = best.getString("username");
                    String password = best.getString("password");
                    server = new Server(
                            "Best vpn server",
                            Config.gbSIGNAL,
                            Objects.requireNonNull(best.getString("ovpn")).replace("\\n", "\n"), username, password
                    );
                    updateCurrentServerIcon(server.getFlagUrl());
                    updateNMserver(server.getCountry());
                }
            });
        }else{
            updateCurrentServerIcon(dataProccessor.getStr("SERVER_FLAG"));
            updateNMserver(dataProccessor.getStr("SERVER_COUNTRY"));
        }
        connection = new CheckInternetConnection();
        LocalBroadcastManager.getInstance(Objects.requireNonNull(getActivity())).registerReceiver(broadcastReceiver, new IntentFilter("connectionState"));

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MobileAds.initialize(Objects.requireNonNull(getActivity()).getApplicationContext(), getString(R.string.Admob_app_id));
        binding.vpnBtn.setOnClickListener(this);
        binding.icNavdown.setOnClickListener(this);binding.countryName.setOnClickListener(this);binding.imageStatus.setOnClickListener(this);
        binding.selectedServerIcon.setOnClickListener(this);
        binding.selLok.setOnClickListener(this);
        try {
            currentVersion = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        new GetVersionCode().execute();
        if (checkConnectivity()) {
            loadIKLAN();


        }


       // MobileAds.initialize Admob

        MobileAds.initialize(getActivity(), new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

      /*  GDPR ADMOB*/


        ConsentDebugSettings debugSettings = new ConsentDebugSettings.Builder(getActivity())
                .setDebugGeography(ConsentDebugSettings
                        .DebugGeography
                        .DEBUG_GEOGRAPHY_NOT_EEA)
                .addTestDeviceHashedId("TEST-DEVICE-HASHED-ID")
                .build();

        ConsentRequestParameters params = new ConsentRequestParameters
                .Builder()
                .setConsentDebugSettings(debugSettings)
                .build();

        consentInformation = UserMessagingPlatform.getConsentInformation(getActivity());
        consentInformation.requestConsentInfoUpdate(
                getActivity(),
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



       // Targeting iklan untuk anak kecil, rating dll. Silahkan ubah sesuai panduan admob dan app

        RequestConfiguration requestConfiguration = MobileAds.getRequestConfiguration()
                .toBuilder()
                .setTagForChildDirectedTreatment(TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
                .build();
        MobileAds.setRequestConfiguration(requestConfiguration);

        dataProccessor.setBool("willshowCustomdl",true);


        // Checking is vpn already running or not
        isServiceRunning();
        VpnStatus.initLogCache(getActivity().getCacheDir());





    }
    private void loadBanner() {
        AdRequest adRequest =
                new AdRequest.Builder() .build();

        AdSize adSize = getAdSize();
        adView.setAdSize(adSize);
        adView.loadAd(adRequest);
    }

    private AdSize getAdSize() {
        Display display = Objects.requireNonNull(getActivity()).getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;
        int adWidth = (int) (widthPixels / density);
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(getActivity(), adWidth);
    }


    /*
    Konfigurasi Banner FAN
     */
    private void bannerfan(){
        bannerAdView= new com.facebook.ads.AdView(Objects.requireNonNull(getActivity()), FAN_BANNER, com.facebook.ads.AdSize.BANNER_HEIGHT_50);
        // Add the ad view to your activity layout
        binding.bannerContainer.addView(bannerAdView);
        // Request an ad
        bannerAdView.loadAd();
    }

    /*
  Konfigurasi Banner FAN
   */
    private void bannerfanBesar(){

        bannerAdView= new com.facebook.ads.AdView(Objects.requireNonNull(getActivity()), FAN_NATIVE, com.facebook.ads.AdSize.RECTANGLE_HEIGHT_250);
        // Add the ad view to your activity layout
       // bannerLayout.addView(bannerAdView);
        binding.IDnativefb.addView(bannerAdView);
        // Request an ad
        bannerAdView.loadAd();
    }




    /*
        Kongigurasi Admob Kotak/besar
         */
    private void BannerAdmobMedium() {
        adView = new AdView(getActivity());
        adView.setAdSize(AdSize.MEDIUM_RECTANGLE);
        adView.setAdUnitId(Config.ADMOB_BANNER);
        adView.loadAd(new AdRequest.Builder().build());
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {


            }

            @Override
            public void onAdFailedToLoad(int errorCode) {


                bannerLayout.setVisibility(View.GONE);
            }

        });

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        bannerLayout.addView(adView, params);
    }
    /*
  Load GDPR
   */
    public void loadForm(){
        UserMessagingPlatform.loadConsentForm(
                getActivity(),
                new UserMessagingPlatform.OnConsentFormLoadSuccessListener() {
                    @Override
                    public void onConsentFormLoadSuccess(ConsentForm consentForm) {
                        MainFragment.this.consentForm = consentForm;
                        if(consentInformation.getConsentStatus() == ConsentInformation.ConsentStatus.REQUIRED) {
                            consentForm.show(
                                    getActivity(),
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

    void gotoUrl(String url) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }
    void cekModeInterstitial(){
        switch (Config.PENGATURAN_IKLAN) {
            case "1":
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {
                    mInterstitialAd.loadAd(new AdRequest.Builder().build());
                }
                break;
            case "2":
                if (interstitialAdfb == null || !interstitialAdfb.isAdLoaded()) {
                    Objects.requireNonNull(interstitialAdfb).loadAd();
                } else {
                    interstitialAdfb.show();

                }
                break;
            case "3":
                StartAppSDK.init(Objects.requireNonNull(getActivity()), STRATAPPID, true);
                StartAppAd.disableSplash();
                StartAppAd.showAd(getActivity());
                break;
        }
    }

    /**
     * @param v: click listener view
     */
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_status:
            case R.id.vpnBtn:
                if (vpnStart) {
                    confirmDisconnect();
                } else {

                    prepareVpn();
                }
                break;
            case R.id.selectedServerIcon:
                MainActivity activity = (MainActivity) getActivity();
                assert activity != null;
                activity.changeServer();
                break;
            case R.id.ic_navdown:
            case R.id.sel_lok:
                if (interstitialAdfb == null || !interstitialAdfb.isAdLoaded()) {
                    Objects.requireNonNull(interstitialAdfb).loadAd();
                } else {
                    interstitialAdfb.show();

                }
                break;
            case R.id.country_name:
                startActivityForResult(new Intent(getActivity(), SelectVPNActivity.class), 2);
                break;
        }
    }

    /**
     * Show show disconnect confirm dialog
     */
    public void confirmDisconnect() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()), R.style.MyDialogStyle);
        builder.setMessage(R.string.connection_close_confirm);

        builder.setPositiveButton(getActivity().getString(R.string.yes), (dialog, id) -> stopVpn());
        builder.setNegativeButton(getActivity().getString(R.string.no), (dialog, id) -> {
        });


        AlertDialog dialog = builder.create();

        dialog.show();
    }
    private boolean checkConnectivity() {
        boolean enabled = true;

        ConnectivityManager connectivityManager = (ConnectivityManager) Objects.requireNonNull(getActivity()).getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();

        if ((info == null || !info.isConnected() || !info.isAvailable())) {
            // Toast.makeText(getApplicationContext(), "Sin conexión a Internet...", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }


    }
    /**
     * Prepare for vpn connect with required permission
     */
    private void prepareVpn() {
        if (!vpnStart) {
            if (getInternetStatus()) {
                Intent intent = VpnService.prepare(getContext());
                if (intent != null)
                    startActivityForResult(intent, 1);
                else startVpn();
                status("connecting");
            } else {
                showToast("you have no internet connection !!");
            }

        } else if (stopVpn()) {

            // VPN is stopped, show a Toast message.
            showToast("Disconnect Successfully");
        }
    }

    /**
     * Stop vpn
     *
     * @return boolean: VPN status
     */
    public boolean stopVpn() {
        try {
            OpenVPNThread.stop();
            status("connect");
            vpnStart = false;

            /*InterstitialAd interstitialAd = new InterstitialAd(getContext());
            interstitialAd.setAdUnitId(getString(R.string.Admob_interstitial_second_id));
            interstitialAd.setAdListener(new AdListener(){
                @Override
                public void onAdLoaded() {
                    interstitialAd.show();
                    super.onAdLoaded();
                }
            });
            interstitialAd.loadAd(new AdRequest.Builder().build());*/
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Taking permission for network access
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
           binding.vpnBtn.callOnClick();
            updateCurrentServerIcon(dataProccessor.getStr("SERVER_FLAG"));
            updateNMserver(dataProccessor.getStr("SERVER_COUNTRY"));
            loadInterstitial();
       /* } else {*/
            //showToast("Permission Deny !! ");
        }
    }

    /**
     * Internet connection status.
     */
    public boolean getInternetStatus() {
        return connection.netCheck(Objects.requireNonNull(getContext()));
    }

    /**
     * Get service status
     */
    public void isServiceRunning() {
        setStatus(vpnService.getStatus());
    }

    /**
     * Start the VPN
     */
    private void startVpn() {
        try {
            if(TextUtils.isEmpty(dataProccessor.getStr("SERVER_OVPN"))){
                OpenVpnApi.startVpn(getContext(), server.getOvpn(), server.getCountry(), server.getOvpnUserName(), server.getOvpnUserPassword());
            }else{
                OpenVpnApi.startVpn(getContext(), dataProccessor.getStr("SERVER_OVPN"), dataProccessor.getStr("SERVER_COUNTRY"), dataProccessor.getStr("SERVER_OVPN_USER"), dataProccessor.getStr("SERVER_OVPN_PASSWORD"));
            }
            Log.i("serverinf","serverinf mainfr"+server.getCountry());
            vpnStart = true;
        } catch (RemoteException e) {
            e.printStackTrace();
        }


    }

    public void setStatus(String connectionState) {
        Resources gres = getResources();
        if (connectionState != null)
            switch (connectionState) {
                case "DISCONNECTED":
                    status("connect");
                    vpnStart = false;
                    vpnService.setDefaultStatus();
                    binding.logTv.setText("");
                    break;
                case "CONNECTED":
                    vpnStart = true;// it will use after restart this activity
                    status("connected");
                    loadInterstitial();
                    binding.logTv.setText("");
                    break;
                case "WAIT":
                    binding.logTv.setText(gres.getString(R.string.lbl_waitserv));
                    break;
                case "AUTH":
                    txtKonekting();
                    binding.logTv.setText(gres.getString(R.string.lbl_authserv));
                    break;
                case "RECONNECTING":
                    status("connecting");
                    binding.logTv.setText(gres.getString(R.string.lbl_reconSer));
                    break;
                case "NONETWORK":
                    binding.logTv.setText(gres.getString(R.string.lbl_nonetserv));
                    break;
            }

    }

    /**
     * Change button background color and text
     *
     * @param status: VPN current status
     */
    public void status(String status) {
        switch (status) {
            case "connect":
                matiVPN();
                binding.vpnBtn.setText(getString(R.string.connect));
                checkIP();
                break;
            case "connecting":
                binding.linerStat.setVisibility(View.GONE);
                binding.vpnBtn.setText(getString(R.string.connecting));
                waitVPn();
                break;
            case "connected":
                aktifVPN();
                binding.vpnBtn.setText(getString(R.string.disconnect));
                checkIP();
                break;
            case "tryDifferentServer":
                binding.vpnBtn.setBackgroundResource(R.drawable.button_connected);
                binding.vpnBtn.setText("Try Different\nServer");
                break;
            case "loading":
                binding.vpnBtn.setBackgroundResource(R.drawable.button);
                binding.vpnBtn.setText("Loading Server..");
                break;
            case "invalidDevice":
                binding.vpnBtn.setBackgroundResource(R.drawable.button_connected);
                binding.vpnBtn.setText("Invalid Device");
                break;
            case "authenticationCheck":
                binding.vpnBtn.setBackgroundResource(R.drawable.button_connecting);
                binding.vpnBtn.setText("Authentication \n Checking...");
                break;
        }

    }

    /**
     * Receive broadcast message
     */
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                setStatus(intent.getStringExtra("state"));
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {

                String duration = intent.getStringExtra("duration");
                String lastPacketReceive = intent.getStringExtra("lastPacketReceive");
                String byteIn = intent.getStringExtra("byteIn");
                String byteOut = intent.getStringExtra("byteOut");

                if (duration == null) duration = "00:00:00";
                if (lastPacketReceive == null) lastPacketReceive = "0";
                if (byteIn == null) byteIn = " ";
                if (byteOut == null) byteOut = " ";

                updateConnectionStatus(duration, lastPacketReceive, byteIn, byteOut);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };

    void aktifVPN() {
        binding.imageStatus.setImageResource(R.drawable.button1);
        Objects.requireNonNull(getActivity()).startService(new Intent(getActivity(), MyService.class));

        binding.ellipse0.setVisibility(View.GONE);animY.cancel();
    }
    void waitVPn(){
        binding.imageStatus.setImageResource(R.drawable.button2);
        binding.ellipse0.setVisibility(View.VISIBLE);
        animY.start();
    }
    void matiVPN() {
        Objects.requireNonNull(getActivity()).stopService(new Intent(getActivity(), MyService.class));
        binding.imageStatus.setImageResource(R.drawable.button3);
        binding.ellipse0.setVisibility(View.GONE);animY.cancel();
       /* binding.imageOff.setVisibility(View.VISIBLE);
        binding.imageOn.setVisibility(View.GONE);*/
    }
    public void loadInterstitial() {
        ctad_counter++;
        if (ctad_counter >= Config.Klik_IKLAN) {
            if(dataProccessor.getBool("willshowCustomdl")){
                showCustomDialog();
                dataProccessor.setBool("willshowCustomdl",false);
            }
            cekModeInterstitial();
            ctad_counter = 0;
        }
        dataProccessor.setInt("hitunginterst",ctad_counter);
        Log.i("intersads","NUMBER "+ctad_counter);
    }
    /**
     * Update status UI
     *
     * @param duration:          running time
     * @param lastPacketReceive: last packet receive time
     * @param byteIn:            incoming data
     * @param byteOut:           outgoing data
     */
    @SuppressLint("SetTextI18n")
    public void updateConnectionStatus(String duration, String lastPacketReceive, String byteIn, String byteOut) {
        binding.durationTv.setText("Duration: " + duration);
        binding.lastPacketReceiveTv.setText("Packet Received: " + lastPacketReceive + " second ago");
        binding.byteInTv.setText("Bytes In: " + byteIn);
        binding.byteOutTv.setText("Bytes Out: " + byteOut);
    }

    @SuppressLint("SetTextI18n")
    void txtKonekting() {
        binding.idCountry.setText(getResources().getString(R.string.lbl_country)+" : ...");
        binding.idIp.setText("IP : ...");
      /*  binding.idKota.setText("City : ... ");
        binding.idTimezone.setText("Time Zone : ...");*/
    }

    @SuppressLint("SetTextI18n")
    void checkIP() {
        String[] getCIty;
        //line cekking
        txtKonekting();
        TimeZone timeZone = TimeZone.getDefault();
        try {
            getCIty = timeZone.getID().split("/");
        } catch (Exception e) {
            getCIty = new String[]{"dad", "Undefined"};
        }
        RequestQueue req = Volley.newRequestQueue(Objects.requireNonNull(getActivity()).getApplicationContext());
        String[] finalGetCIty = getCIty;
        req.add(new StringRequest(Request.Method.GET, "https://wtfismyip.com/json", response -> {
            try {
                JSONObject jsonObject = new JSONObject(response);
                binding.idCountry.setText(getResources().getString(R.string.lbl_country)+" : " + jsonObject.getString("YourFuckingLocation") + ", " + jsonObject.getString("YourFuckingCountryCode"));
                binding.idIp.setText("IP : " + jsonObject.getString("YourFuckingIPAddress"));
             /*   binding.idKota.setText("City : " + finalGetCIty[1]);
                binding.idTimezone.setText("Time Zone : " + timeZone.getID());*/
                Log.i("dataIP","dataIP "+finalGetCIty[1]+" "+timeZone.getID()+" "+jsonObject.getString("YourFuckingIPAddress")+" "+jsonObject.getString("YourFuckingISP"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            binding.idCountry.setText(getResources().getString(R.string.lbl_noinfo));
        }));
        req.addRequestFinishedListener(request -> req.getCache().clear());
    }
    private void loadNativeAd() {
        // Instantiate a NativeAd object.
        // NOTE: the placement ID will eventually identify this as your App, you can ignore it for
        // now, while you are testing and replace it later when you have signed up.
        // While you are using this temporary code you will only get test ads and if you release
        // your code like this to the Google Play your users will not receive ads (you will get a no fill error).
        nativeAd = new NativeAd(Objects.requireNonNull(getActivity()), FAN_NATIVE);

        NativeAdListener nativeAdListener = new NativeAdListener() {
            @Override
            public void onMediaDownloaded(Ad ad) {

            }

            @Override
            public void onError(Ad ad, AdError adError) {

            }

            @Override
            public void onAdLoaded(Ad ad) {
                inflateAd(nativeAd);
            }

            @Override
            public void onAdClicked(Ad ad) {

            }

            @Override
            public void onLoggingImpression(Ad ad) {

            }
        };

        // Request an ad
        nativeAd.loadAd(
                nativeAd.buildLoadAdConfig()
                        .withAdListener(nativeAdListener)
                        .build());
    }

    private void inflateAd(NativeAd nativeAd) {

        nativeAd.unregisterView();

        // Add the Ad view into the ad container.
        nativeAdLayout = binding.nativeAdContainer;
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        // Inflate the Ad view.  The layout referenced should be the one you created in the last step.
        faView = (LinearLayout) inflater.inflate(R.layout.native_ad_layout, nativeAdLayout, false);
        nativeAdLayout.addView(faView);

        // Add the AdOptionsView

        LinearLayout adChoicesContainer = fbview.findViewById(R.id.ad_choices_container);
        AdOptionsView adOptionsView = new AdOptionsView(getActivity(), nativeAd, nativeAdLayout);
        adChoicesContainer.removeAllViews();
        adChoicesContainer.addView(adOptionsView, 0);

        // Create native UI using the ad metadata.
        MediaView nativeAdIcon = faView.findViewById(R.id.native_ad_icon);
        TextView nativeAdTitle = faView.findViewById(R.id.native_ad_title);
        MediaView nativeAdMedia = faView.findViewById(R.id.native_ad_media);
        TextView nativeAdSocialContext = faView.findViewById(R.id.native_ad_social_context);
        TextView nativeAdBody = faView.findViewById(R.id.native_ad_body);
        TextView sponsoredLabel = faView.findViewById(R.id.native_ad_sponsored_label);
        Button nativeAdCallToAction = faView.findViewById(R.id.native_ad_call_to_action);

        // Set the Text.
        nativeAdTitle.setText(nativeAd.getAdvertiserName());
        nativeAdBody.setText(nativeAd.getAdBodyText());
        nativeAdSocialContext.setText(nativeAd.getAdSocialContext());
        nativeAdCallToAction.setVisibility(nativeAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
        nativeAdCallToAction.setText(nativeAd.getAdCallToAction());
        sponsoredLabel.setText(nativeAd.getSponsoredTranslation());

        // Create a list of clickable views
        List<View> clickableViews = new ArrayList<>();
        clickableViews.add(nativeAdTitle);
        clickableViews.add(nativeAdCallToAction);

        // Register the Title and CTA button to listen for clicks.
        nativeAd.registerViewForInteraction(
                faView, nativeAdMedia, nativeAdIcon, clickableViews);
    }
    private class GetVersionCode extends AsyncTask<Void, String, String> {

        @Override

        protected String doInBackground(Void... voids) {

            String newVersion = null;

            try {
                Document document = Jsoup.connect("https://play.google.com/store/apps/details?id=" + Objects.requireNonNull(getActivity()).getPackageName()  + "&hl=en")
                //Document document = Jsoup.connect("https://play.google.com/store/apps/details?id=id.go.pinrangkab.pinrang360&hl=en")
                        //Document document = Jsoup.connect("http://infaltech.com/alim/ver.txt")
                        .timeout(30000)
                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        .referrer("http://www.google.com")
                        .get();
                if (document != null) {
                    Elements element = document.getElementsContainingOwnText("Current Version");
                    for (Element ele : element) {
                        if (ele.siblingElements() != null) {
                            Elements sibElemets = ele.siblingElements();
                            for (Element sibElemet : sibElemets) {
                                newVersion = sibElemet.text();
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return newVersion;

        }


        @Override

        protected void onPostExecute(String onlineVersion) {

            super.onPostExecute(onlineVersion);

            if (onlineVersion != null && !onlineVersion.isEmpty()) {

                //work but hanya persamaan
                if(!currentVersion.equals(onlineVersion)){
                    Log.d("New update", "Current version " + currentVersion + "terbaru " + onlineVersion);
                    updateku();

                }
                /*if (Float.valueOf(currentVersion) < Float.valueOf(onlineVersion)) {
                    //show anything

                }*/

            }

            Log.d("cekupdate info", "Current version " + currentVersion + "playstore version " + onlineVersion);

        }
    }

    void updateku(){
        final Dialog dialog = new Dialog(Objects.requireNonNull(getActivity()));
        @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.dialog_update, null, false);

        LinearLayout adContainer = view.findViewById(R.id.adLine);
        AdView adView = new AdView(Objects.requireNonNull(getContext()));
        adView.setAdSize(AdSize.MEDIUM_RECTANGLE);
        adView.setAdUnitId(Config.ADMOB_BANNER);
        adView.loadAd(new AdRequest.Builder().build());

        adContainer.addView(adView);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view);

        dialog.setCancelable(false);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(Objects.requireNonNull(dialog.getWindow()).getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        dialog.findViewById(R.id.bt_close).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=com.makbanteng.mychet"));
            startActivity(intent);
            dialog.dismiss();
        });
        dialog.findViewById(R.id.bt_restart).setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }
    void showCustomDialog() {
        final Dialog dialog = new Dialog(Objects.requireNonNull(getActivity()));
        @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.dialog_warning, null, false);

        LinearLayout adContainer = view.findViewById(R.id.adLine);
        AdView adView = new AdView(Objects.requireNonNull(getContext()));
        adView.setAdSize(AdSize.MEDIUM_RECTANGLE);
        adView.setAdUnitId(Config.ADMOB_BANNER);
        adView.loadAd(new AdRequest.Builder().build());

        adContainer.addView(adView);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view);

        dialog.setCancelable(false);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(Objects.requireNonNull(dialog.getWindow()).getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        dialog.findViewById(R.id.bt_close).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=com.makbanteng.mychet"));
            startActivity(intent);
            dialog.dismiss();
        });
        dialog.findViewById(R.id.bt_restart).setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }
    private void loadIKLAN() {
        try {
            Log.i("iklanfb","fb iklan "+Config.FAN_INTER );
                      /*
        Intertitial Admob
         */
            mInterstitialAd = new InterstitialAd(Objects.requireNonNull(getActivity()).getApplicationContext());
            mInterstitialAd.setAdUnitId(Config.ADMOB_INTER);
            mInterstitialAd.loadAd(new AdRequest.Builder().build());
            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    super.onAdClosed();
                    mInterstitialAd.loadAd(new AdRequest.Builder().build());
                }
            });
        /*
        Intertitial FAN
         */
            interstitialAdfb = new com.facebook.ads.InterstitialAd(getActivity(),  Config.FAN_INTER);
            interstitialAdfb.loadAd();
            Log.i("IKLANID","IKLANID: "+Config.ON_OFF_JSON+" : "+Config.ADMOB_BANNER+" : "+ Config.ADMOB_INTER + " : KLIK IKLAN " + Config.Klik_IKLAN);
            adView = new AdView(Objects.requireNonNull(getActivity()));
            adView.setAdUnitId(Config.ADMOB_BANNER);
            binding.adViewContainer.addView(adView);
            //iklan banner
            switch (Config.PENGATURAN_IKLAN) {
                case "1":
                    loadBanner();
                    //disable blum disetting layout rencana di dialog
                    //BannerAdmobMedium();
                    break;
                case "2":
                    //disable blum disetting layout rencana di dialog
                    loadNativeAd();
                    //bannerfanBesar();
                    break;
                case "3":
            /*
             Konfigurasi Banner StartApp
            */

                    Banner startAppBanner = new Banner(getActivity());
                    RelativeLayout.LayoutParams bannerParameters =
                            new RelativeLayout.LayoutParams(
                                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                                    RelativeLayout.LayoutParams.WRAP_CONTENT);
                    bannerParameters.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    bannerAdContainer.addView(startAppBanner, bannerParameters);

                    //banner gede startapp
            /*    Mrec startAppMrec = new Mrec(getActivity());
                RelativeLayout.LayoutParams mrecParameters =
                        new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.WRAP_CONTENT,
                                RelativeLayout.LayoutParams.WRAP_CONTENT);
                mrecParameters.addRule(RelativeLayout.CENTER_HORIZONTAL);


        // Add to main Layout
                bannerLayout.addView(startAppMrec, mrecParameters);*/
                    break;
            }
        }catch (Exception e){
            Log.e("IKLAN","iklan error "+e);
        }




    }
    /**
     * Show toast message
     *
     * @param message: toast message
     */
    public void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * VPN server country icon change
     *
     * @param serverIcon: icon URL
     */
    public void updateCurrentServerIcon(String serverIcon) {
        Glide.with(Objects.requireNonNull(getContext()))
                .load(serverIcon)
                .into(binding.selectedServerIcon);
    }
    public void updateNMserver(String servername){
        binding.countryName.setText(servername);
    }
    /**
     * Change server when user select new server
     *
     * @param server ovpn server details
     */
    @Override
    public void newServer(Server server) {
        this.server = server;
        updateCurrentServerIcon(server.getFlagUrl());
        updateNMserver(server.getCountry());
        try {
            if (vpnStart) {
                stopVpn();
            }

            prepareVpn();
        }catch (Exception ignored){}
    }

    @Override
    public void onResume() {
        if (server == null) {
            server = preference.getServer();
        }

        super.onResume();
    }

    /**
     * Save current selected server on local shared preference
     */
    @Override
    public void onStop() {
        if (server != null) {
            preference.saveServer(server);
        }

        super.onStop();
    }
}
