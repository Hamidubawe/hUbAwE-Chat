package com.arewatechacademy.myapplication

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.recreate
import androidx.fragment.app.FragmentTransaction
import com.arewatechacademy.myapplication.Fragments.*
import com.arewatechacademy.myapplication.Models.UsersModel
import com.arewatechacademy.myapplication.Notifications.ReminderBroadcast
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.InterstitialAd
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    private var mUserRef: DatabaseReference? = null
    private var currentUser: FirebaseUser? = null
    private lateinit var bottomNav : BottomNavigationView
    private lateinit var reference: DatabaseReference
    private var mAdView: AdView? = null
    private var mInterstitialAd: InterstitialAd? = null
    private lateinit var token: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        setContentView(R.layout.activity_main)
        scheduleNotification()
        checkConnection()

        refreshBtn.setOnClickListener {
            this.recreate()
        }

        setSupportActionBar(findViewById(R.id.main_toolbar))
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        //initializing ads
        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView!!.loadAd(adRequest)

        //initialize inter ads
        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd!!.adUnitId = Constants.interstitialAd
        mInterstitialAd!!.loadAd(AdRequest.Builder().build())


        mAuth = FirebaseAuth.getInstance()
        bottomNav = findViewById(R.id.bottomNavigation)
        reference = FirebaseDatabase.getInstance().reference
        bottomNav.background = ColorDrawable(resources.getColor(android.R.color.transparent))

        if (FirebaseAuth.getInstance().currentUser != null) {

            getTokenId()
            bottomNav.selectedItemId = R.id.friendsfv
            supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, PostFragment())
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit()

            postBtn.setOnClickListener {

                supportFragmentManager.beginTransaction()
                    .replace(R.id.frame_layout, PostFragment())
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit()
                bottomNav.selectedItemId = R.id.friendsfv

            }

            currentUser = FirebaseAuth.getInstance().currentUser

            mUserRef = FirebaseDatabase.getInstance().reference.child("Users")
                .child(currentUser!!.uid)

            mUserRef!!.addValueEventListener(object : ValueEventListener {

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val user = dataSnapshot.getValue(UsersModel::class.java)

                        name.text = user!!.name
                        val photo = user.profilePhoto!!
                        if (photo != "default") {
                            Picasso.get().load(photo).placeholder(R.drawable.avatar)
                                .into(picture)
                        } else
                            picture.setImageResource(R.drawable.avatar)


                    }

                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })

        }

        bottomNav.setOnNavigationItemSelectedListener { item ->

            when(item.itemId){

                R.id.chat -> {
                    val chatFragment =
                        ChatFragment()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, chatFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()
                    overridePendingTransition(
                        R.anim.slide_out_bottom,
                        R.anim.slide_in_bottom
                    )
                }
                R.id.friends -> {

                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, FriendsFragment())
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()
                    overridePendingTransition(
                        R.anim.slide_out_bottom,
                        R.anim.slide_in_bottom
                    )

                }
                R.id.request -> {

                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, RequestsFragment())
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()
                    overridePendingTransition(
                        R.anim.slide_out_bottom,
                        R.anim.slide_in_bottom
                    )

                }
                R.id.myProfile -> {

                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, MyProfileFragment())
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()
                    overridePendingTransition(
                        R.anim.slide_out_bottom,
                        R.anim.slide_in_bottom
                    )

                }

            }
            true

        }

    }

    private fun scheduleNotification() {

        val intent = Intent(this, ReminderBroadcast::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this, 0, intent, PendingIntent.FLAG_ONE_SHOT
        )

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val currentTime = System.currentTimeMillis()
        val alarmTime = 18800000

        alarmManager.set(AlarmManager.RTC_WAKEUP, currentTime + alarmTime, pendingIntent)
    }

    private fun getTokenId() {

        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            token = it.token

            val map = HashMap<String, Any>()
            map["token"] = token
            mUserRef!!.updateChildren(map)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        super.onCreateOptionsMenu(menu)
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.app_bar_search -> {
                //donothing for now
            }
            R.id.exit -> {
                showAd()
                super.onBackPressed()
                return true
            }
            R.id.findFriends -> {
                showAd()
                val intent = Intent(this, UsersActivity::class.java)
                startActivity(intent)
                overridePendingTransition(
                    R.anim.fui_slide_in_right,
                    R.anim.fui_slide_out_left
                )

                return true
            }
            R.id.log_out -> {
                showAd()
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                overridePendingTransition(
                    R.anim.fui_slide_in_right,
                    R.anim.slide_in_bottom
                )

                return true
            }
            R.id.settings -> {
                showAd()
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                overridePendingTransition(
                    R.anim.fui_slide_in_right,
                    R.anim.slide_in_bottom
                )

                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {

        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        if (currentUser == null || !currentUser!!.isEmailVerified) {
            sendToStart()
        }
        /*else{

        }

         */

    }

    /*override fun onStop() {
        super.onStop()

        if (currentUser != null) {
            val map = HashMap<String, Any>()
            map["presence"] = System.currentTimeMillis().toString()
            reference.child("Users").child(currentUser!!.uid).updateChildren(map)
        }
    }

     */

    private fun sendToStart() {

        val startIntent = Intent(this@MainActivity, LoginActivity::class.java)
        startActivity(startIntent)
        overridePendingTransition(
            R.anim.slide_out_bottom,
            R.anim.slide_in_bottom
        )
        finish()
    }

    override fun onBackPressed() {

        showAd()
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(true)
        builder.setTitle("Exit???")
        builder.setMessage("Are you sure you want to exit?")
        builder.setPositiveButton(
            "Yes"
        ) { _: DialogInterface?, _: Int -> finish() }
        builder.setNegativeButton("No", null).show()



    }

    private fun showAd() {
        if (mInterstitialAd!!.isLoaded) mInterstitialAd!!.show()
    }

    private fun checkConnection(){

        val connectivityManager = this.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        val mobileNetwork = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)

        when {
            wifi!!.isConnected -> {
                recyclerLayout.visibility = View.VISIBLE
                noInternetTv.visibility = View.GONE

            }
            mobileNetwork!!.isConnected -> {
                recyclerLayout.visibility = View.VISIBLE
                noInternetTv.visibility = View.GONE
            }
            else -> {
                recyclerLayout.visibility = View.GONE
                noInternetTv.visibility = View.VISIBLE
            }
        }

    }
}
