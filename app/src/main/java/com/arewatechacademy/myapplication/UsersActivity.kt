package com.arewatechacademy.myapplication

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arewatechacademy.hubawe.Adapters.SuggestAdapter
import com.arewatechacademy.hubawe.Adapters.UsersAdapter
import com.arewatechacademy.myapplication.AdsPackage.AddCheck
import com.arewatechacademy.myapplication.AdsPackage.BannerAdsHandler
import com.arewatechacademy.myapplication.Models.UsersModel
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_users.*
import java.util.*
import java.util.Collections.*
import kotlin.collections.ArrayList

class UsersActivity : AppCompatActivity() {
    lateinit var usersAdapter: UsersAdapter
    lateinit var suggestAdapter: SuggestAdapter
    lateinit var mUsers : List<UsersModel>
    private lateinit var mSuggestions : List<UsersModel>

    private lateinit var recyclerView: RecyclerView
    private lateinit var peopleRecycler: RecyclerView
    private lateinit var search : EditText
    private lateinit var mAdView: AdView


    lateinit var dialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users)

        //initializing ads
        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)


        dialog = ProgressDialog(this)
        dialog.setMessage("Loading users...")
        dialog.setCanceledOnTouchOutside(false)

        //initializing recyclerview
        recyclerView = findViewById(R.id.usersRecycler)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(applicationContext)
        search = findViewById(R.id.searchEditText)

        //suggestion RecyclerView
        peopleRecycler = findViewById(R.id.people)
        peopleRecycler.setHasFixedSize(true)
        peopleRecycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)


        dialog.show()

        val handler = Handler()
        handler.postDelayed({ dialog.dismiss() }, 3000)

        mUsers = ArrayList()
        mSuggestions = ArrayList()
        peopleYouKnow()
        retrieveUsers()


        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchUsers(s.toString().toLowerCase())
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

    }

    private fun retrieveUsers() {

        val firebaseUser  = FirebaseAuth.getInstance().currentUser!!.uid
        val userRef = FirebaseDatabase.getInstance().getReference("Users")
        userRef.keepSynced(true)

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                (mUsers as ArrayList<UsersModel>).clear()
                if (search.text.toString() == ""){

                    for (ds in p0.children){
                        val user : UsersModel? = ds.getValue(UsersModel::class.java)
                        if (!(user!!.userId).equals(firebaseUser)) {
                            (mUsers as ArrayList<UsersModel>).add(user)
                            reverse(mUsers)
                        }
                    }

                }

                usersAdapter = UsersAdapter(
                    this@UsersActivity,
                    mUsers,
                    false
                )
                usersAdapter.notifyDataSetChanged()
                recyclerView.adapter = usersAdapter

            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    fun searchUsers(str: String){

        val firebaseUser  = FirebaseAuth.getInstance().currentUser!!.uid
        val queryRef = FirebaseDatabase.getInstance().getReference("Users")
            .orderByChild("name").startAt(str).endAt(str + "\uf8ff")

        queryRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                (mUsers as ArrayList<UsersModel>).clear()

                for (ds in p0.children){

                    val user : UsersModel? = ds.getValue(
                        UsersModel::class.java)
                    (mUsers as ArrayList<UsersModel>).add(user!!)

                }

                usersAdapter = UsersAdapter(
                    this@UsersActivity,
                    mUsers,
                    false
                )
                usersAdapter.notifyDataSetChanged()
                recyclerView.adapter = usersAdapter
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

    }

    private fun peopleYouKnow() {


        val currentUser  = FirebaseAuth.getInstance().currentUser!!.uid
        val userRef = FirebaseDatabase.getInstance().getReference("Users")
        userRef.keepSynced(true)

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                (mSuggestions as ArrayList<UsersModel>).clear()
                for (ds in p0.children){
                    val user : UsersModel? = ds.getValue(
                        UsersModel::class.java)

                    userRef.child(currentUser).addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(p0: DataSnapshot) {
                            val myId : UsersModel? = p0.getValue(
                                UsersModel::class.java)

                            if (!(user!!.userId).equals(currentUser)) {
                                if (user.state == myId!!.state || user.relationship == myId.relationship)
                                    (mSuggestions as ArrayList<UsersModel>).add(user)
                                reverse(mSuggestions)

                                if(mSuggestions.isEmpty())
                                    peopleYouKnow.visibility = View.GONE

                            }
                        }

                        override fun onCancelled(p0: DatabaseError) {

                        }
                    })

                    suggestAdapter =
                        SuggestAdapter(
                            this@UsersActivity,
                            mSuggestions,
                            true
                        )
                    suggestAdapter.notifyDataSetChanged()
                    peopleRecycler.adapter = suggestAdapter

                }



            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(
            R.anim.fui_slide_in_right,
            R.anim.slide_in_bottom
        )
    }
}
