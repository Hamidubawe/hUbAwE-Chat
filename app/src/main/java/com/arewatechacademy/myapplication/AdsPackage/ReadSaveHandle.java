package com.arewatechacademy.myapplication.AdsPackage;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

public class ReadSaveHandle {

    Context context;
    String myConfigFileName = "yourConfigFileName";
    public ReadSaveHandle(Context context) {
        this.context = context;
    }

    public void saveAdCount(String totalAdsCount) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(myConfigFileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(MyConstants.keyForAdCounter, totalAdsCount);
        //editor.putString("Count", count);
        editor.commit();
    }
    public void saveKeyValue(String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(myConfigFileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        //editor.putString("Count", count);
        editor.commit();
    }

    public String getKeyValue(String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(myConfigFileName, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, "");
    }
    public void showToastMessage(String message){
        Toast.makeText(context, message,Toast.LENGTH_LONG).show();
    }



}
