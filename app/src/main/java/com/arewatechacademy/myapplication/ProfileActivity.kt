package com.arewatechacademy.myapplication

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.arewatechacademy.myapplication.AdsPackage.AddCheck
import com.arewatechacademy.myapplication.AdsPackage.BannerAdsHandler
import com.arewatechacademy.myapplication.Models.UsersModel
import com.arewatechacademy.myapplication.Notifications.NotificationData
import com.arewatechacademy.myapplication.Notifications.PushNotification
import com.arewatechacademy.myapplication.Notifications.RetrofitInstance
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.*
import kotlin.collections.HashMap

class ProfileActivity : AppCompatActivity() {

    lateinit var mAuth: FirebaseAuth
    lateinit var currentUser: FirebaseUser
    lateinit var reference: DatabaseReference
    var userId: String = ""
    lateinit var currentState: String
    private var pd: ProgressDialog? = null
    lateinit var dialog: ProgressDialog
    private lateinit var photo: String
    private lateinit var adsHandler: BannerAdsHandler
    private val devId = "pOp8KCGYzeQuz18hudiRMPLLePf2"
    private var mAdView: AdView? = null
    private lateinit var token: String
    private lateinit var currentUserName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        //Setting ads in the app
        //initializing ads
        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView!!.loadAd(adRequest)

        //setting up progress dialog
        dialog = ProgressDialog(this)
        dialog.setMessage("Loading user Profile...")
        dialog.setCanceledOnTouchOutside(false)

        dialog.show()

        val handler = Handler()
        handler.postDelayed({ dialog.dismiss() }, 4000)

        //initializing current state  and implementing current user
        currentState = "not_friends"
        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth.currentUser!!

        //implementing progress dialog
        pd = ProgressDialog(this)
        pd!!.setMessage("Loading Profile...")
        pd!!.setCanceledOnTouchOutside(false)
        pd!!.show()

        //getting intent value of the profile userId
        userId = intent.getStringExtra("user_id")!!.toString()


        //getting user information
        reference = FirebaseDatabase.getInstance().reference
        reference.child("Users").child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val user = p0.getValue(UsersModel::class.java)

                    photo = user!!.profilePhoto!!
                    token = user.token!!
                    if (photo != "default") {
                        Picasso.get().load(photo).placeholder(R.drawable.avatar)
                            .into(picture_profile)
                    } else
                        picture_profile.setImageResource(R.drawable.avatar)
                    pd!!.dismiss()

                    if (user.coverPhoto != null) {
                        Picasso.get().load(user.coverPhoto).placeholder(R.drawable.avatar)
                            .into(coverPic)
                    } else
                        coverPic.setImageResource(R.drawable.avatar)

                    status_profile.text = user.status!!
                    userName.text = user.name!!
                    userGender.text = user.gender

                    if (user.state != "default") {
                        userState.text = user.state
                        //pd!!.dismiss()
                    } else {
                        userState.text = "Not Assigned"
                        //pd!!.dismiss()
                    }

                    if (user.relationship != "default")
                        userRelationship.text = user.relationship
                    else
                        userRelationship.text = "Not Assigned"

                    if (user.currentCity == "default")
                        userCurrentCity.text = "Not Assigned"
                    else
                        userCurrentCity.text = user.currentCity

                }

                reference.child("Users").child(currentUser.uid).addValueEventListener(object :ValueEventListener{

                    override fun onDataChange(snapshot: DataSnapshot) {

                        val user = snapshot.getValue(UsersModel::class.java)
                        currentUserName = user!!.name!!
                    }

                    override fun onCancelled(error: DatabaseError) {


                    }

                })

                //checking if users are already friends
                reference.child("Friends").child(currentUser.uid)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.hasChild(userId)) {
                                currentState = "friends"
                                add_user.text = "Unfriend"

                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {}
                    })

                //checking if user have receive friend request to profile user
                reference.child("Friend_Requests").child("received_request")
                    .child(currentUser.uid).addValueEventListener(object :
                        ValueEventListener {
                        override fun onDataChange(p0: DataSnapshot) {
                            if (p0.hasChild(userId)) {
                                currentState = "request_received"
                                add_user.text = "Accept Request"
                                sendUsermessage.text = "Decline Request"


                            }

                        }

                        override fun onCancelled(p0: DatabaseError) {

                        }
                    })


                ////checking if user have receive friend request from current user
                reference.child("Friend_Requests").child("sent_request")
                    .child(currentUser.uid).addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(p0: DataSnapshot) {
                            if (p0.hasChild(userId)) {
                                currentState = "req_sent"
                                add_user.text = "Cancel Request"


                            }
                        }

                        override fun onCancelled(p0: DatabaseError) {

                        }
                    })


            }

            override fun onCancelled(p0: DatabaseError) {

            }


        })

        picture_profile.setOnClickListener {
            val intent = Intent(this@ProfileActivity, ImageViewActivity::class.java)
            intent.putExtra("photo", photo)
            startActivity(intent)
        }

        if (devId == userId){

            add_user.text = "Following"
            add_user.isEnabled = false
        }

        //add/remove/cancel user button implementation
        add_user.setOnClickListener {
            pd!!.setCanceledOnTouchOutside(false)

            //sending friend request to user
            if (currentState == "not_friends") {

                val dbREf = FirebaseDatabase.getInstance().reference.child("Friends")
                dbREf.child(currentUser.uid).addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(p0: DataSnapshot) {
                        val friends = p0.childrenCount.toInt()
                        //checking to see if user didnt exceed the maimum number of people he can have
                        if (friends in 45..49){
                            val builder = AlertDialog.Builder(this@ProfileActivity)
                            builder.setMessage("You are about to reach the maximum number of friends you can have")
                                .setTitle("Friends Limit")
                                .setPositiveButton("Okay", null)
                                .show()
                        }
                        if (friends >= 50){
                            val builder = AlertDialog.Builder(this@ProfileActivity)
                            builder.setMessage("You have reached the maximum number of friends you can have " +
                                    "${userName.text} may not be able to accept your friend request")
                                .setTitle("Friends Limit")
                                .setPositiveButton("Okay", null)
                                .show()
                        }
                        sendingRequest()

                    }

                    override fun onCancelled(p0: DatabaseError) {

                    }


                })

            }

            //canceling friend request sent
            if (currentState == "req_sent") {

                val builder =
                    AlertDialog.Builder(this)
                builder.setCancelable(true)
                builder.setTitle("Cancel Request")
                builder.setMessage("Cancel request sent to ${userName.text}??")
                builder.setPositiveButton(
                    "Yes"
                ) { _, _ -> cancelRequest() }
                builder.setNegativeButton("No", null).show()


            }

            //accepting friend request sent
            if (currentState == "request_received") {

                val dbREf = FirebaseDatabase.getInstance().reference.child("Friends")
                dbREf.child(currentUser.uid).addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(p0: DataSnapshot) {
                        val friends = p0.childrenCount.toInt()

                        dbREf.child(userId).addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(p0: DataSnapshot) {
                                val userFriends = p0.childrenCount.toInt()

                                if (friends in 45..50){
                                    val builder = AlertDialog.Builder(this@ProfileActivity)
                                    builder.setMessage("You are about to reach the maximum number of friends you can have")
                                        .setTitle("Friends Limit")
                                        .setPositiveButton("Okay", null)
                                        .show()
                                    acceptRequest()
                                }
                                if (friends >= 50 ){
                                    val builder = AlertDialog.Builder(this@ProfileActivity)
                                    builder.setMessage("You have reached the maximum number of friends you can have")
                                        .setTitle("Friends Limit")
                                        .setPositiveButton("Okay", null)
                                        .show()
                                }
                                if (userFriends >= 50 ){
                                    val builder = AlertDialog.Builder(this@ProfileActivity)
                                    builder.setMessage("${userName.text} have reached the maximum number of friends they can have")
                                        .setTitle("Can Accept Request")
                                        .setPositiveButton("Okay", null)
                                        .show()
                                }
                                else
                                    acceptRequest()

                            }

                            override fun onCancelled(p0: DatabaseError) {

                            }
                        })


                    }

                    override fun onCancelled(p0: DatabaseError) {

                    }


                })


            }

            //unfriending user
            if (currentState == "friends") {
                val builder = AlertDialog.Builder(this)
                builder.setCancelable(true)
                builder.setTitle("Remove??")
                builder.setMessage("Are you sure you want to remove ${userName.text}??")
                builder.setPositiveButton(
                    "Yes"
                ) { _, _ -> unfriendUser() }
                builder.setNegativeButton("Cancel", null).show()
            }


        }

        //sending message to user
        sendUsermessage.setOnClickListener {

            if (currentState == "not_friends") {
                Toast.makeText(
                    this@ProfileActivity,
                    "You are not friends with ${userName.text}", Toast.LENGTH_LONG
                ).show()
            }

            if (currentState == "req_sent") {
                Toast.makeText(
                    this@ProfileActivity,
                    "${userName.text} have not accepted your request yet", Toast.LENGTH_LONG
                ).show()
            }

            if (currentState == "request_received") {

                val builder = AlertDialog.Builder(this)
                builder.setCancelable(true)
                builder.setTitle("Decline Request??")
                builder.setMessage("Are you sure you want to decline ${userName.text}'s request??")
                builder.setPositiveButton(
                    "Yes"
                ) { _, _ -> declineRequest() }
                builder.setNegativeButton("Cancel", null).show()
                declineRequest()


            }
            else if (currentState == "friends" || (currentUser.uid == devId) || userId == devId) {
                val intent = Intent(this@ProfileActivity, ChatActivity::class.java)
                intent.putExtra("user_id", userId)
                startActivity(intent)
                overridePendingTransition(
                    R.anim.slide_out_bottom,
                    R.anim.slide_in_bottom
                )
            }

        }
    }

    private fun declineRequest() {
        pd!!.setMessage("Declining request...")
        pd!!.setCanceledOnTouchOutside(false)
        pd!!.show()

        reference.child("Friend_Requests").child("sent_request")
            .child(userId).child(currentUser.uid).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    reference.child("Friend_Requests").child("received_request")
                        .child(currentUser.uid).child(userId).removeValue()
                        .addOnCompleteListener { po ->
                            if (po.isSuccessful) {
                                currentState = "not_friends"
                                pd!!.dismiss()

                                add_user.text = "Add Friend"
                                sendUsermessage.text = "Send Message"

                                Toast.makeText(
                                    this@ProfileActivity,
                                    "Friend request declined", Toast.LENGTH_LONG
                                ).show()

                            }

                        }
                } else {
                    pd!!.dismiss()
                    Toast.makeText(
                        this@ProfileActivity,
                        "error: failed to cancel Friend request", Toast.LENGTH_LONG
                    ).show()
                    currentState = "req_sent"

                }
            }
    }

    private fun sendingRequest() {

        pd!!.setMessage("Sending Request")
        pd!!.show()

        val map = HashMap<String, Any>()
        map["receiver"] = userId

        reference.child("Friend_Requests").child("sent_request")
            .child(currentUser.uid).child(userId).setValue(map)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    val receiveMap = HashMap<String, Any>()
                    receiveMap["sender"] = currentUser.uid
                    reference.child("Friend_Requests").child("received_request")
                        .child(userId).child(currentUser.uid)
                        .setValue(receiveMap).addOnCompleteListener { receive ->
                            if (receive.isSuccessful) {
                                pd!!.dismiss()

                                PushNotification(
                                    NotificationData("New friend Request", "$currentUserName sent you a friend request",
                                        currentUser.uid, "request"), token).also {
                                    sendNotification(it)
                                }

                                currentState = "req_sent"
                                add_user.text = "Cancel Request"
                                Toast.makeText(
                                    this@ProfileActivity,
                                    "Success: Friend Request sent to ${userName.text} successfully",
                                    Toast.LENGTH_SHORT
                                ).show()

                            }
                        }
                } else {
                    Toast.makeText(
                        this@ProfileActivity,
                        "error: Friend request not sent check your network connection",
                        Toast.LENGTH_LONG
                    ).show()
                    pd!!.dismiss()
                    currentState == "not_friends"
                }
            }
    }

    private fun cancelRequest() {

        pd!!.setMessage("Canceling request...")
        pd!!.setCanceledOnTouchOutside(false)
        pd!!.show()

        reference.child("Friend_Requests").child("sent_request")
            .child(currentUser.uid).child(userId).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    reference.child("Friend_Requests").child("received_request")
                        .child(userId).child(currentUser.uid).removeValue()
                        .addOnCompleteListener { po ->
                            if (po.isSuccessful) {
                                currentState = "not_friends"
                                pd!!.dismiss()
                                add_user.text = "Add Friend"

                                Toast.makeText(
                                    this@ProfileActivity,
                                    "Friend request cancelled", Toast.LENGTH_LONG
                                ).show()

                            }

                        }
                } else {
                    pd!!.dismiss()
                    Toast.makeText(
                        this@ProfileActivity,
                        "error: failed to cancel Friend request",
                        Toast.LENGTH_LONG
                    ).show()
                    currentState == "req_sent"

                }
            }
    }

    private fun unfriendUser() {

        pd!!.setMessage("Removing ${userName.text}...")
        pd!!.setCanceledOnTouchOutside(false)
        pd!!.show()

        reference.child("Friends").child(currentUser.uid).child(userId).removeValue()
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    reference.child("Friends").child(userId)
                        .child(currentUser.uid).removeValue()
                        .addOnCompleteListener { remove ->

                            if (remove.isSuccessful) {
                                add_user.text = "Add Friend"
                                currentState = "not_friends"
                                pd!!.dismiss()
                                Toast.makeText(
                                    this@ProfileActivity,
                                    "Removed: You are no longer friends with ${userName.text}",
                                    Toast.LENGTH_LONG
                                ).show()


                            }
                        }
                } else {
                    pd!!.dismiss()
                    Toast.makeText(
                        this@ProfileActivity,
                        "error: Failed to remove ${userName.text} pls check your internet",
                        Toast.LENGTH_LONG
                    ).show()
                    currentState = "friends"

                }
            }
    }

    private fun acceptRequest() {

        pd!!.setMessage("Accepting request...")
        pd!!.setCanceledOnTouchOutside(false)
        pd!!.show()

        val date =
            DateFormat.getDateTimeInstance().format(Date())
        val friendMap = HashMap<String, Any>()
        friendMap["id"] = userId
        friendMap["date"] = date
        reference.child("Friends")
            .child(currentUser.uid).child(userId).setValue(friendMap)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    val friendMap2 = HashMap<String, Any>()
                    friendMap2["id"] = currentUser.uid
                    friendMap2["date"] = date
                    reference.child("Friends")
                        .child(userId).child(currentUser.uid)
                        .setValue(friendMap2).addOnCompleteListener { add ->

                            if (add.isSuccessful) {
                                reference.child("Friend_Requests")
                                    .child("sent_request")
                                    .child(userId).child(currentUser.uid).removeValue()
                                    .addOnCompleteListener { sent ->
                                        if (sent.isSuccessful) {
                                            reference.child("Friend_Requests")
                                                .child("received_request")
                                                .child(currentUser.uid).child(userId)
                                                .removeValue().addOnCompleteListener { po ->
                                                    if (po.isSuccessful) {

                                                        currentState = "friends"
                                                        pd!!.dismiss()
                                                        add_user.text = "Unfriend"
                                                        PushNotification(
                                                            NotificationData("New friend", "$currentUserName accepted you a friend request",
                                                                currentUser.uid, "request"), token).also {
                                                            sendNotification(it)
                                                        }

                                                        Toast.makeText(
                                                            this@ProfileActivity,
                                                            "You are now friends with ${userName.text}",
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                    }

                                                }
                                        }
                                    }
                            }
                        }
                } else {
                    Toast.makeText(
                        this@ProfileActivity,
                        "error: pls check your internet connection",
                        Toast.LENGTH_LONG
                    ).show()
                    currentState == "request_received"
                }
            }

    }

    override fun onStart() {
        super.onStart()

        val map = HashMap<String, Any>()
        map["presence"] = "online"
        FirebaseDatabase.getInstance().reference.child("Users").child(currentUser.uid).updateChildren(map)
    }

    override fun onStop() {
        super.onStop()
        val map = HashMap<String, Any>()
        map["presence"] = System.currentTimeMillis().toString()
        reference.child("Users").child(currentUser.uid).updateChildren(map)
    }

    private fun sendNotification(notification : PushNotification) = CoroutineScope(Dispatchers.IO).launch{

        try {
            val response = RetrofitInstance.api.postNotification(notification)

            when {
                response.isSuccessful -> {

                }
                else -> {
                    Toast.makeText(this@ProfileActivity, response.errorBody().toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        }
        catch (e: Exception){

        }

    }


}
