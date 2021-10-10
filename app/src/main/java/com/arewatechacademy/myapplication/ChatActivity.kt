package com.arewatechacademy.myapplication

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arewatechacademy.myapplication.Adapters.ChatAdapter
import com.arewatechacademy.myapplication.AdsPackage.AddCheck
import com.arewatechacademy.myapplication.AdsPackage.BannerAdsHandler
import com.arewatechacademy.myapplication.Models.ChatModel
import com.arewatechacademy.myapplication.Models.UsersModel
import com.arewatechacademy.myapplication.Notifications.NotificationData
import com.arewatechacademy.myapplication.Notifications.PushNotification
import com.arewatechacademy.myapplication.Notifications.RetrofitInstance
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.InterstitialAd
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class ChatActivity : AppCompatActivity() {

    var userId : String = ""
    private var ref: DatabaseReference? = null
    private  var dbRefForSeen: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    private var chatRecycler: RecyclerView? = null
    private var myUserId: String = ""
    private var chatModels: List<ChatModel>? = null
    private var chatAdapter: ChatAdapter? = null
    private var mAdView: AdView? = null
    private var seenListener: ValueEventListener? = null
    private val GALLERY_PICK = 1
    private lateinit var progressDialog: ProgressDialog
    private var mStorageRef: StorageReference? = null
    private lateinit var textListener : EditText
    private lateinit var adsHandler: BannerAdsHandler
    private lateinit var token: String
    private val devId = "pOp8KCGYzeQuz18hudiRMPLLePf2"
    private lateinit var toolbar: Toolbar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        toolbar = findViewById(R.id.chat_toolbar)
        textListener = findViewById(R.id.msgText)

        progressDialog = ProgressDialog(this)
        progressDialog.setCanceledOnTouchOutside(false)

        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        toolbar.overflowIcon!!.setColorFilter(ContextCompat.getColor(this, R.color.gnt_white), PorterDuff.Mode.SRC_ATOP)

        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.stackFromEnd = true


        //initializing storage reference
        mStorageRef = FirebaseStorage.getInstance().reference


        //implementing recyclerView
        chatRecycler = findViewById(R.id.chatRecycler)
        chatRecycler!!.setHasFixedSize(true)
        chatRecycler!!.layoutManager = linearLayoutManager

        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView!!.loadAd(adRequest)


        mAuth = FirebaseAuth.getInstance()
        myUserId = mAuth!!.currentUser!!.uid
        userId = intent.getStringExtra("user_id")!!

        ref = FirebaseDatabase.getInstance().reference

        textListener.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if (s.toString().trim().isEmpty()){
                    val map = HashMap<String, Any>()
                    map ["typing"] = "no"
                    ref!!.child("Users").child(myUserId).updateChildren(map)
                }
                else{
                    val map = HashMap<String, Any>()
                    map ["typing"] = userId
                    ref!!.child("Users").child(myUserId).updateChildren(map)
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }
        })

        ref!!.child("Users").child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                if (p0.exists()){
                    val user = p0.getValue(UsersModel::class.java)

                    if (!user!!.profilePhoto.equals("default")) {
                        Picasso.get().load(user.profilePhoto).placeholder(R.drawable.avatar).into(picture)
                    }
                    else
                        picture.setImageResource(R.drawable.avatar)

                    name.text = user.name!!
                    token = user.token.toString()

                    when {
                        user.typing == myUserId -> {
                            lastSeen.text = "typing..."
                        }
                        user.presence == "online" -> {
                            lastSeen.text = "online"
                        }
                        else -> {
                            setChatTIme(user.presence, lastSeen)
                        }
                    }


                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

        sendMsgBtn.setOnClickListener {

            val message = msgText.text.toString()
            if (message.isNotEmpty())
                sendMessage(message, userId)

        }
        sendPhotoBtn.setOnClickListener {
            val pickImage = Intent()
            pickImage.type = "image/*"
            pickImage.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(pickImage, GALLERY_PICK)
        }

        reedMessage()
        seenMessage()
    }

    private fun setChatTIme(timestamp: String?, date: TextView?) {

        //converting timestamp to actual time;
        val calendar = Calendar.getInstance(Locale.ENGLISH)
        calendar.timeInMillis = timestamp!!.toLong()

        val t24 = 86400000 //24hrs in milliseconds
        val t48 = t24 * 2  //48hrs in millisecond
        val chatTime: Long = timestamp.toLong() // time message is sent
        val timeCurrent = System.currentTimeMillis() - chatTime

        if ((System.currentTimeMillis() - chatTime) < t24) {
            val time = DateFormat.format("hh:mm aa", calendar).toString()
            date!!.text = "Last seen today $time"
        }
        if ((timeCurrent > t24) && (timeCurrent < t48)) {
            val time = DateFormat.format("hh:mm aa", calendar).toString()
            date!!.text = "Last seen Yesterday $time"
        } else if (timeCurrent > t48) {
            val time = DateFormat.format("dd-MM-yyyy hh:mm aa", calendar).toString()
            date!!.text = "Last seen $time"

        }

    }

    private fun seenMessage() {

        dbRefForSeen = FirebaseDatabase.getInstance().getReference("Messages")
        seenListener = dbRefForSeen!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (ds in dataSnapshot.children) {
                    val chatM = ds.getValue(ChatModel::class.java)
                    if (chatM!!.receiver.equals(myUserId) && chatM.sender.equals(userId)) {
                        val seen =
                            HashMap<String, Any>()
                        seen["seen"] = true
                        ds.ref.updateChildren(seen)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })

    }

    private fun reedMessage() {

        chatModels = ArrayList()
        val dbref =
            FirebaseDatabase.getInstance().reference.child("Messages")
        dbref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                (chatModels as ArrayList<ChatModel>).clear()
                for (ds in p0.children){
                    val chat: ChatModel? = ds.getValue(ChatModel::class.java)

                    if (chat!!.sender == myUserId && chat.receiver == userId
                        ||  chat.sender == userId && chat.receiver == myUserId) {

                        (chatModels as ArrayList<ChatModel>).add(chat)

                    }
                    chatAdapter =
                        ChatAdapter(
                            this@ChatActivity,
                            chatModels as ArrayList<ChatModel>
                        )
                    chatAdapter!!.notifyDataSetChanged()
                    chatRecycler!!.adapter = chatAdapter
                }
            }

            override fun onCancelled(p0: DatabaseError) {


            }
        })

    }

    private fun sendMessage(message: String, userId: String) {

        myUserId = mAuth!!.currentUser!!.uid
        ref = FirebaseDatabase.getInstance().reference
        val timestamp = System.currentTimeMillis().toString()

        val hashMap =
            HashMap<String, Any>()
        hashMap["sender"] = myUserId
        hashMap["receiver"] = userId
        hashMap["message"] = message
        hashMap["timestamp"] = timestamp
        hashMap["seen"] = false
        hashMap["type"] = "text"

        msgText.setText("")
        ref!!.child("Messages").push().setValue(hashMap).addOnCompleteListener { task ->
            if (task.isSuccessful){

                PushNotification(
                    NotificationData("New Message", message, myUserId, "chat"), token).also {
                    sendNotification(it)
                }

                ref!!.child("Chats").child(myUserId).child(userId).child("id")
                    .setValue(userId)

                ref!!.child("Chats").child(userId).child(myUserId).child("id")
                    .setValue(myUserId)


            }else {
                val toast = Toast.makeText(this,
                    "error: check your internet", Toast.LENGTH_LONG)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()

            }
        }

    }

    private fun sendNotification(notification : PushNotification) = CoroutineScope(Dispatchers.IO).launch{

        try {
            val response = RetrofitInstance.api.postNotification(notification)

            when {
                response.isSuccessful -> {

                }
                else -> {
                    Toast.makeText(
                        this@ChatActivity, response.errorBody().toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        }
        catch (e: Exception){

        }

    }


    override fun onPause() {
        super.onPause()
        this.seenListener?.let { dbRefForSeen!!.removeEventListener(it) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY_PICK && resultCode == Activity.RESULT_OK) {
            val imageUri = data!!.data

            val progress = progressDialog.progress
            progressDialog.setMessage("Sending Photo $progress%")
            progressDialog.show()

            if (imageUri != null) {
                //get the download url from firebase
                val photoName = ref!!.push().key
                val filepath = mStorageRef!!.child("Chat Images")
                    .child("$photoName.jpg")

                filepath.putFile(imageUri).addOnSuccessListener {
                    filepath.downloadUrl.addOnCompleteListener { task ->
                        //val downloadURL = task.result.toString()
                        if (task.isSuccessful) {

                            val downloadURL = task.result.toString()

                            val timestamp = System.currentTimeMillis().toString()

                            val photoMap =
                                HashMap<String, Any>()
                            photoMap["sender"] = myUserId
                            photoMap["receiver"] = userId
                            photoMap["message"] = downloadURL
                            photoMap["timestamp"] = timestamp
                            photoMap["seen"] = false
                            photoMap["type"] = "image"

                            msgText.setText("")
                            ref!!.child("Messages").push().setValue(photoMap).addOnCompleteListener {
                                    sent ->
                                if (sent.isSuccessful) {
                                    Toast.makeText(this, "Sent", Toast.LENGTH_SHORT).show()
                                    progressDialog.dismiss()

                                    PushNotification(
                                        NotificationData("Unread Message", "Sent you a photo", myUserId, "chat"), token).also {
                                        sendNotification(it)
                                    }

                                }else {
                                    Toast.makeText(this, "Failed to send photo", Toast.LENGTH_SHORT).show()
                                    progressDialog.dismiss()
                                }
                            }

                        }
                    }
                }

            }
        }
    }

    override fun onStart() {
        val ref: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Friends")
        val currentUser = FirebaseAuth.getInstance().currentUser!!.uid

        ref.child(userId).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {

                if (userId == devId || currentUser == devId) {
                    linearLayout.visibility = View.VISIBLE
                    failedMessage.visibility = View.GONE
                }

                else if (!p0.hasChild(currentUser)) {
                    linearLayout.visibility = View.GONE
                    failedMessage.visibility = View.VISIBLE
                    failedMessage.text = "Cant send message to ${name.text}"
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

        super.onStart()
        val reference = FirebaseDatabase.getInstance().reference
        if (FirebaseAuth.getInstance().currentUser != null){
            val map = HashMap<String, Any>()
            map ["presence"] = "online"
            reference.child("Users").child(myUserId).updateChildren(map)
        }



    }

    override fun onStop() {

        val reference = FirebaseDatabase.getInstance().reference
        super.onStop()
        val map = HashMap<String, Any>()
        map ["presence"] = System.currentTimeMillis().toString()
        reference.child("Users").child(myUserId).updateChildren(map)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        val inflater = menuInflater
        inflater.inflate(R.menu.chat_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)

        when(item.itemId){
            R.id.viewProfile ->{
                val intent = Intent(this, ProfileActivity::class.java)
                intent.putExtra("user_id", userId)
                startActivity(intent)
            }
            R.id.viewProfilePic ->{
                val ref = FirebaseDatabase.getInstance().reference.child("Users")
                    .child(userId)
                ref.addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(p0: DataSnapshot) {
                        val photo = p0.child("profilePhoto").value.toString()

                        val intent = Intent(applicationContext, ImageViewActivity::class.java)
                        intent.putExtra("photo", photo)
                        startActivity(intent)
                    }

                    override fun onCancelled(p0: DatabaseError) {

                    }
                })

            }
            R.id.call -> {
                Toast.makeText(applicationContext, "Coming soon...", Toast.LENGTH_LONG).show()
            }
        }

        return true
    }
}
