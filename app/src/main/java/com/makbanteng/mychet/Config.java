package com.makbanteng.mychet;

public class Config {

    /*
    Ubah ON_OFF_JSON = "0"; menjadi ON_OFF_JSON = "1";
    jika menggunakan iklan online
     */
    public static String ON_OFF_JSON = "0";
    public static  String LINK_Json="https://infaltech.com/vpn/api/speedvpn.json";
    public static String gbSIGNAL ="https://infaltech.com/vpn/api/flag/ic_signal_full.png";
    /*
    PENGATURAN_IKLAN="1" untuk Admob
    PENGATURAN_IKLAN="2" untuk FAN
    PENGATURAN_IKLAN="3" untuk StartApp
     */
    public static String PENGATURAN_IKLAN="1";
    public static int Klik_IKLAN=3;
    /*
    ID Admob, untuk appid ada di string.xml
     */
    //testad
   /* public static String ADMOB_INTER ="ca-app-pub-3940256099942544/1033173712";
    public static String ADMOB_NATIV = "ca-app-pub-3940256099942544/2247696110";
    public static String ADMOB_BANNER = "ca-app-pub-3940256099942544/6300978111";*/
    public static String ADMOB_INTER ="ca-app-pub-6841438106698846/4744373297";
    public static String ADMOB_NATIV = "ca-app-pub-3940256099942544/2247696110";
    public static String ADMOB_BANNER = "ca-app-pub-6841438106698846/7178964940";

    /*
    ID FAn
    TESTMODE_FAN = true untuk pengujian
    dan TESTMODE_FAN = false untuk publish app
     */
    public static String FAN_INTER ="2485695071727620_2485695361727591";
    public static String FAN_NATIVE="2485695071727620_2487610871536040";
    public static String FAN_BANNER = "YOUR_PLACEMENT_ID";
    public static String FAN_BANNER_BIG = "YOUR_PLACEMENT_ID";
    public static String FAN_REWARD ="YOUR_PLACEMENT_ID" ;
    public static boolean TESTMODE_FAN = false;

    /*
    ID StartApp
     */
    public static String STRATAPPID="123456789";

    /*Redirect App, ubah STATUS = "1" untuk melakukan redirect ke aplikasi baru, fitur ini harus tetap dalam
      kedaaan STATUS = "0"; selama aplikasi masih live
       */
    public static String STATUS = "0";
    public static String LINK = "https://play.google.com/store/apps/details?id=com.digiteam.speedVPN";


}
