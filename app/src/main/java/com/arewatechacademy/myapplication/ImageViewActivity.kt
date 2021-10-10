package com.arewatechacademy.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.arewatechacademy.myapplication.AdsPackage.AddCheck
import com.arewatechacademy.myapplication.AdsPackage.BannerAdsHandler
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_image_view.*

class ImageViewActivity : AppCompatActivity() {
    private var photo = ""
    private lateinit var mAdView: AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_view)

        //initializing ads
        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        photo = intent.getStringExtra("photo")!!.toString()

        if (photo != "default") {

            try {
                Picasso.get().load(photo).placeholder(R.drawable.loading).into(photoImageView)
            } catch (e: Exception) {
                photoImageView.setImageResource(R.drawable.avatar)
                Toast.makeText(this, "Error failed to load photo", Toast.LENGTH_LONG)
                    .show()
            }
        }else{
            photoImageView.setImageResource(R.drawable.avatar)
            Toast.makeText(this, "No Photo", Toast.LENGTH_LONG)
                .show()
        }
    }
}
