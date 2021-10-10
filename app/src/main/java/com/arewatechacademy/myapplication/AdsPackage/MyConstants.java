package com.arewatechacademy.myapplication.AdsPackage;

public class MyConstants {

    //Below are test IDs use only this when testing
    final static String myMainActivityBannerID = "0b44525ad3bf4ce282239478714e4782";
    final static String myMainActivityInterstitialID = "33ea772eba5d4b82ba2769eb99ba6d07";
    final static String myRewardVideoID = "920b6145fb1546cf8b5cf2ac34638bb7";


    //below is for facebook ads testing, HashID of the device can be found in the run log when running an app with facebook SDK
    //look for something like I/AdInternalSettings: Test mode device hash: c9d1da04-c84c-4d36-a315-0aa7b6a8a7a8
    //final static String MyEmulatorHashID = "c9d1da04-c84c-4d36-a315-0aa7b6a8a7a8";

    final static boolean hasAd = true;

    final static String keyForAdCounter = "TotalAdsCount";
    final static String keyForAdFreeStartDate = "AdFreeStart";
    public final static String keyForAdFreeEndDate = "AdFreeEnd";

    final static double rewardedPeriod = 60.0*1000.0*4.0;//for Four minutes (testing)
    //final static double rewardedPeriod = 60.0*1000.0*60.0*24.0*30.0; // 30 days equivalent in miliseconds
    final static String videoAdRewardedMessage = "Congratulations! \n Thank you for support, you can now enjoy Ad free App for 5 minutes";
    //final static String videoAdRewardedMessage = "Congratulations! \n Thank you for support, you can now enjoy Ad free App for 1 month";


}
