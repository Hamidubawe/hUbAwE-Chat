package com.arewatechacademy.myapplication.AdsPackage;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import com.arewatechacademy.myapplication.R;
import com.mopub.common.MoPub;
import com.mopub.common.SdkConfiguration;
import com.mopub.common.SdkInitializationListener;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.MoPubView;

public class BannerAdsHandler implements MoPubInterstitial.InterstitialAdListener {

    private Context mContext;
    private String sActivity;
    private AddCheck adCheck;

    private MoPubView moPubView;
    private MoPubInterstitial mInterstitial;

    private boolean doNotSkipAd;
    private boolean interstitialLoaded = false;
    private boolean interstitialAlreadyDisplayed = false;

    private boolean adsInitializationCompleted = false;

    private boolean loadBannerOnInitializationComplete = false;
    private boolean loadInterstitialOnInitializationComplete = false;

    public BannerAdsHandler(Context context, String requestingActivity) {
        mContext = context;
        sActivity = requestingActivity;
        adCheck = new AddCheck(mContext);
    }

    public void handleInterstitialAds(){
        if (sActivity.equals(AddCheck.MySourceActivity)){
            doNotSkipAd = adCheck.interstitialAdRequired(AddCheck.MySourceActivity);
            if (MyConstants.hasAd && doNotSkipAd) {
                if (adsInitializationCompleted){
                    mInterstitial = new MoPubInterstitial((Activity) mContext, MyConstants.myMainActivityInterstitialID);
                    mInterstitial.setInterstitialAdListener(this);
                    mInterstitial.load();
                }
                else {
                    loadInterstitialOnInitializationComplete = true;
                    initializeMoPubSDK(MyConstants.myMainActivityInterstitialID);
                }
            }
        }
        else {
            Toast.makeText(mContext,"No Add strategy defined for this activity",Toast.LENGTH_SHORT).show();
        }
    }

    public void handleBannerAds(){
        if (sActivity.equals(AddCheck.MySourceActivity)){
            doNotSkipAd = adCheck.bannerAdRequired(AddCheck.MySourceActivity);
            if (MyConstants.hasAd && doNotSkipAd) {
                Activity activity = (Activity) mContext;
                //moPubView = (MoPubView) activity.findViewById(R.id.moPubView);
                if (adsInitializationCompleted){
                    moPubView.setAdUnitId(MyConstants.myMainActivityBannerID); // Enter your Ad Unit ID from www.mopub.com
                    //moPubView.setAdSize(MoPubAdSize); // Call this if you are not setting the ad size in XML or wish to use an ad size other than what has been set in the XML. Note that multiple calls to `setAdSize()` will override one another, and the MoPub SDK only considers the most recent one.
                    //moPubView.loadAd(MoPubAdSize); // Call this if you are not calling setAdSize() or setting the size in XML, or if you are using the ad size that has not already been set through either setAdSize() or in the XML
                    moPubView.loadAd();
                }
                else {
                    loadBannerOnInitializationComplete = true;
                    initializeMoPubSDK(MyConstants.myMainActivityInterstitialID);
                }
            }
        }
        else {
            Toast.makeText(mContext,"No Add strategy defined for this activity",Toast.LENGTH_SHORT).show();
        }
    }

    public boolean showInterstitial(){
        if (interstitialAlreadyDisplayed && AddCheck.adEnabled){
            mInterstitial.load();//load again so that it can be displayed in the next call
            interstitialAlreadyDisplayed = false;
            return false;
        }
        if (sActivity.equals(AddCheck.MySourceActivity)){
            if (mInterstitial != null && AddCheck.adEnabled){
                if (mInterstitial.isReady() ) {
                    mInterstitial.show();
                    interstitialAlreadyDisplayed = true;
                    return true;
                }
            }
        }
        return false;
    }

    public void destroyRequested(){
        if (mInterstitial != null){
            mInterstitial.destroy();
        }
    }
    public void pauseRequested(){
        MoPub.onPause((Activity) mContext);
    }
    public void stopRequested(){
        MoPub.onStop((Activity) mContext);
    }
    public void resumeRequested(){
        MoPub.onResume((Activity) mContext);
    }

    @Override
    public void onInterstitialLoaded(MoPubInterstitial interstitial) {
        interstitialLoaded = true;
    }
    @Override
    public void onInterstitialFailed(MoPubInterstitial interstitial, MoPubErrorCode errorCode) {
    }
    @Override
    public void onInterstitialShown(MoPubInterstitial interstitial) {
    }
    @Override
    public void onInterstitialClicked(MoPubInterstitial interstitial) {
    }
    @Override
    public void onInterstitialDismissed(MoPubInterstitial interstitial) {
    }

    private void initializeMoPubSDK(String adUnit){
        // configurations required to initialize
        /*
        Map<String, String> mediatedNetworkConfiguration1 = new HashMap<>();
        mediatedNetworkConfiguration1.put("<custom-adapter-class-data-key>", "<custom-adapter-class-data-value>");
        Map<String, String> mediatedNetworkConfiguration2 = new HashMap<>();
        mediatedNetworkConfiguration2.put("<custom-adapter-class-data-key>", "<custom-adapter-class-data-value>");
         */
        SdkConfiguration sdkConfiguration = new SdkConfiguration.Builder(adUnit)
                /*.withMediationSettings("MEDIATION_SETTINGS")
                .withAdditionalNetworks(CustomAdapterConfiguration.class.getName())
                .withMediatedNetworkConfiguration(CustomAdapterConfiguration1.class.getName(), mediatedNetworkConfiguration)
                .withMediatedNetworkConfiguration(CustomAdapterConfiguration2.class.getName(), mediatedNetworkConfiguration)
                .withMediatedNetworkConfiguration(CustomAdapterConfiguration1.class.getName(), mediatedNetworkConfiguration1)
                .withMediatedNetworkConfiguration(CustomAdapterConfiguration2.class.getName(), mediatedNetworkConfiguration2)
                .withLogLevel(LogLevel.Debug)*/
                .withLegitimateInterestAllowed(false)
                .build();
        MoPub.initializeSdk(mContext, sdkConfiguration, initSdkListener());
    }
    private SdkInitializationListener initSdkListener() {
        return new SdkInitializationListener() {
            @Override
            public void onInitializationFinished() {
	   /* MoPub SDK initialized.
	   Check if you should show the consent dialog here, and make your ad requests. */
                adsInitializationCompleted = true;
                Toast.makeText(mContext,"Initialization completed",Toast.LENGTH_SHORT).show();

                if (loadInterstitialOnInitializationComplete){
                    mInterstitial = new MoPubInterstitial((Activity) mContext, MyConstants.myMainActivityInterstitialID);
                    mInterstitial.setInterstitialAdListener(BannerAdsHandler.this);
                    mInterstitial.load();
                    loadInterstitialOnInitializationComplete = false;
                }
                if (loadBannerOnInitializationComplete){
                    moPubView.setAdUnitId(MyConstants.myMainActivityBannerID); // Enter your Ad Unit ID from www.mopub.com
                    //moPubView.setAdSize(MoPubAdSize); // Call this if you are not setting the ad size in XML or wish to use an ad size other than what has been set in the XML. Note that multiple calls to `setAdSize()` will override one another, and the MoPub SDK only considers the most recent one.
                    //moPubView.loadAd(MoPubAdSize); // Call this if you are not calling setAdSize() or setting the size in XML, or if you are using the ad size that has not already been set through either setAdSize() or in the XML
                    moPubView.loadAd();
                    loadBannerOnInitializationComplete = false;
                }
            }
        };
    }

}
