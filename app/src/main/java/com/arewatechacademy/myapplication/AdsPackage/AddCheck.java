package com.arewatechacademy.myapplication.AdsPackage;

import android.content.Context;

public class AddCheck {

    private Context mContext ;
    public AddCheck(Context context) {
        mContext = context;
    }

    public final static String MySourceActivity = "MainActivity";

    public static boolean adEnabled = true; // Assumes that by default ad is required

    public boolean bannerAdRequired(String activity){

        switch (activity){
            case MySourceActivity:
                adEnabled = ! inAdsFreePeriod();
                if (adEnabled){
                    return true;
                }
                else {
                    return false;
                }
            default:
                return false;
        }
    }

    public boolean interstitialAdRequired(String activity){
        switch (activity){
            case MySourceActivity:
                adEnabled = ! inAdsFreePeriod();
                if (adEnabled){
                    return true;
                }
                else {
                    return false;
                }
            default:
                return false;
        }
    }

    private boolean inAdsFreePeriod(){
        try {
            ReadSaveHandle RSH = new ReadSaveHandle(mContext);
            double adFreeEndPeriod = Double.parseDouble(RSH.getKeyValue(MyConstants.keyForAdFreeEndDate));
            double currentTime = System.currentTimeMillis();
            if (adFreeEndPeriod-currentTime>=0){
                return true;
            }
            else return false;
        }
        catch (Exception e){
            return false;
        }
    }

    public boolean shouldLoadRewardVideo(){
        adEnabled = ! inAdsFreePeriod();
        if (adEnabled){
            return true;
        }
        else {
            return false;
        }
    }

}
