package com.arewatechacademy.myapplication

import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arewatechacademy.myapplication.Adapters.CommentAdapter
import com.arewatechacademy.myapplication.Models.CommentModel
import com.arewatechacademy.myapplication.Models.PostModel
import com.arewatechacademy.myapplication.Models.UsersModel
import com.arewatechacademy.myapplication.Notifications.NotificationData
import com.arewatechacademy.myapplication.Notifications.PushNotification
import com.arewatechacademy.myapplication.Notifications.RetrofitInstance
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_post.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class PostActivity : AppCompatActivity() {

    private lateinit var postId:String
    private lateinit var token: String
    private lateinit var recyclerView: RecyclerView
    lateinit var commentList: List<CommentModel>
    private lateinit var reference: DatabaseReference
    private lateinit var commentAdapter: CommentAdapter

    var liked = false
    var unliked = false
    val currentUser = FirebaseAuth.getInstance().currentUser
    private lateinit var currentUsername: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.stackFromEnd = true
        recyclerView = findViewById(R.id.commentRecycler)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.setHasFixedSize(true)

        postId = intent.getStringExtra("postId")!!
        reference = FirebaseDatabase.getInstance().reference.child("Post")

        FirebaseDatabase.getInstance().reference.child("Users").child(currentUser!!.uid)
            .addValueEventListener(object :ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {

                val user = snapshot.getValue(UsersModel::class.java)
                currentUsername = user!!.name!!
            }

            override fun onCancelled(error: DatabaseError) {


            }

        })


        getPostInfo()
        getLikesCount(likesCount, likeBtn)
        getUnlikeCounts(unlikesCount, unlikeBtn)
        getCommentCounts(commentsCount)
        showComments()


        likeBtn.setOnClickListener {

            if (liked) {
                FirebaseDatabase.getInstance().reference.child("Likes")
                    .child(postId).child(currentUser.uid).removeValue()
                likeBtn.setImageResource(R.drawable.ic_thumbs_up)
            }
            if (unliked){
                FirebaseDatabase.getInstance().reference.child("UnLikes").
                child(postId).child(currentUser.uid).removeValue()
                unlikeBtn.setImageResource(R.drawable.ic_thumbs_down)

                /*FirebaseDatabase.getInstance().reference.child("Likes")
                    .child(postList[position].postId).child(currentUser.uid).setValue("liked")
                holder.likeBtn.setImageResource(R.drawable.ic_liked)*/
            }
            if (!liked) {

                FirebaseDatabase.getInstance().reference.child("Likes")
                    .child(postId).child(currentUser.uid).setValue("liked")
                likeBtn.setImageResource(R.drawable.ic_liked)

                PushNotification(
                    NotificationData("New Like", "$currentUsername likes your photo", postId, "comment"), token).also {
                    sendNotification(it)
                }

            }

        }

        unlikeBtn.setOnClickListener {

            if (unliked) {
                FirebaseDatabase.getInstance().reference.child("UnLikes")
                    .child(postId).child(currentUser.uid).removeValue()
                unlikeBtn.setImageResource(R.drawable.ic_thumbs_down)
            }
            if (liked){
                FirebaseDatabase.getInstance().reference.child("Likes").
                child(postId).child(currentUser.uid).removeValue()
                likeBtn.setImageResource(R.drawable.ic_thumbs_up)

                /*FirebaseDatabase.getInstance().reference.child("UnLikes")
                    .child(postId).child(currentUser.uid).setValue("unliked")
                unlikeBtn.setImageResource(R.drawable.ic_unliked)*/
            }
            if (!unliked) {
                FirebaseDatabase.getInstance().reference.child("UnLikes")
                    .child(postId).child(currentUser.uid).setValue("unliked")
               unlikeBtn.setImageResource(R.drawable.ic_unliked)

                PushNotification(
                    NotificationData("New Like", "$currentUsername unlikes your photo", postId, "comment"), token).also {
                    sendNotification(it)
                }

            }

        }

        commentBtn.setOnClickListener {
            if (commentEditText.text.isNotEmpty()){
                postComment(commentEditText.text.toString())
                commentEditText.setText("")
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
                        this@PostActivity, response.errorBody().toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        }
        catch (e: Exception){

        }

    }

    private fun postComment(comment: String) {

        val dbRef = FirebaseDatabase.getInstance().reference
        val commentId = dbRef.push().key.toString()

        val map = HashMap<String, Any>()
        map["publisher"] = FirebaseAuth.getInstance().currentUser!!.uid
        map["comment"] = comment
        map["postId"] = postId
        map["commentId"] = commentId

        dbRef.child("Comments").child(commentId).setValue(map).addOnCompleteListener {
            Toast.makeText(this, "Commented", Toast.LENGTH_SHORT).show()

            PushNotification(
                NotificationData("New Comment", "$currentUsername Commented on your photo", postId
                    , "comment"),
                token).also {
                sendNotification(it)
            }
        }

    }

    private fun showComments() {

        val comentList: List<CommentModel>

        comentList = ArrayList()
        val dbRef = FirebaseDatabase.getInstance().reference
        dbRef.child("Comments").addValueEventListener(object : ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                for (ds in snapshot.children){

                    val comment = ds.getValue(CommentModel::class.java)
                    if (comment!!.postId == postId){

                        comentList.add(comment)

                    }

                }

            }

            override fun onCancelled(error: DatabaseError) {


            }

        })



        commentAdapter = CommentAdapter(this, commentList)
        recyclerView.adapter = commentAdapter
        commentAdapter.notifyDataSetChanged()

    }

    private fun getPostInfo() {

        reference.child(postId).addValueEventListener(object : ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                val post = snapshot.getValue(PostModel::class.java)

                if (post!!.type == "text"){
                    postImage.visibility = View.GONE
                }
                postText.text = post.postText
                try {
                    Picasso.get().load(post.postPhoto).placeholder(R.drawable.avatar).into(postImage)
                }catch (e: Exception){
                    postImage.setImageResource(R.drawable.avatar)
                }
                getUserInfo(post.publisher)

                if (post.postTimestamp.isNullOrEmpty()){
                    postDate.visibility = View.GONE
                }
                else{
                    setPostTIme(post.postTimestamp, postDate)
                }



                postImage.setOnClickListener {

                    val intent = Intent(this@PostActivity, ImageViewActivity::class.java)
                    intent.putExtra("photo", post.postPhoto)
                    startActivity(intent)

                }


            }



            override fun onCancelled(error: DatabaseError) {


            }


        })

    }

    private fun setPostTIme(timestamp: String, postDate: TextView) {
        //converting timestamp to actual time;
        val calendar = Calendar.getInstance(Locale.ENGLISH)
        calendar.timeInMillis = timestamp.toLong()

        val t24 = 86400000 //24hrs in milliseconds
        val t48 = t24 * 2  //48hrs in millisecond
        val chatTime: Long = timestamp.toLong() // time message is sent
        val timeCurrent = System.currentTimeMillis() - chatTime

        if ((System.currentTimeMillis() - chatTime) < t24) {
            val time = DateFormat.format("hh:mm aa", calendar).toString()
            postDate.text = "Today $time"
        }
        if ((timeCurrent > t24) && (timeCurrent < t48)) {
            val time = DateFormat.format("hh:mm aa", calendar).toString()
            postDate.text = "Yesterday $time"
        } else if (timeCurrent > t48) {
            val time = DateFormat.format("dd-MM-yyyy hh:mm aa ", calendar).toString()
            postDate.text = time

        }

    }

    private fun getUserInfo(publisher: String?) {

        val ref = FirebaseDatabase.getInstance().reference.child("Users")

        ref.child(publisher!!).addValueEventListener(object : ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(UsersModel::class.java)

                posterName.text = user!!.name
                token = user.token!!
                try {
                    Picasso.get().load(user.profilePhoto).into(userPostPhoto)
                }catch (e:Exception){
                    userPostPhoto.setImageResource(R.drawable.avatar)
                }
                if (user.verified == true){
                    verified.visibility = View.VISIBLE
                }

                posterName.setOnClickListener {
                    val intent = Intent(this@PostActivity, ProfileActivity::class.java)
                    intent.putExtra("user_id", user.userId)
                    startActivity(intent)
                }

            }
            override fun onCancelled(error: DatabaseError) {
            }

        })

    }

    private fun getLikesCount(like: TextView, likeBtn: ImageView){

        val ref = FirebaseDatabase.getInstance().reference.child("Likes")
            .child(postId)


        ref.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {
                    val count = snapshot.childrenCount
                    like.text = "$count likes"

                    if (snapshot.hasChild(currentUser!!.uid)) {
                        liked = true
                        likeBtn.setImageResource(R.drawable.ic_liked)
                    } else {
                        liked = false
                        likeBtn.setImageResource(R.drawable.ic_thumbs_up)
                    }

                } else
                    like.text = "0 likes"

            }

            override fun onCancelled(error: DatabaseError) {


            }
        })
    }

    private fun getUnlikeCounts(unlike: TextView, favorBtn: ImageView) {

        val ref = FirebaseDatabase.getInstance().reference.child("UnLikes")
            .child(postId)

        ref.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {
                    val count = snapshot.childrenCount
                    unlike.text = "$count unlikes"

                    if (snapshot.hasChild(currentUser!!.uid)) {
                        unliked = true
                        favorBtn.setImageResource(R.drawable.ic_unliked)
                    } else {
                        unliked = false
                        favorBtn.setImageResource(R.drawable.ic_thumbs_down)
                    }

                } else
                    unlike.text = "0 unlikes"

            }

            override fun onCancelled(error: DatabaseError) {


            }
        })
    }

    private fun getCommentCounts(commentCount: TextView) {

        val ref = FirebaseDatabase.getInstance().reference.child("Comments")
        commentList = ArrayList()


        ref.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                var commentNumber = 0
                for (ds in snapshot.children){

                    val comment = ds.getValue(CommentModel::class.java)

                    if (comment!!.postId == postId) {

                        (commentList as ArrayList).add(comment)

                        commentNumber += 1
                        commentCount.text = "${commentNumber.toString()} comments"
                    }
                    else
                        commentCount.text = "0 Comments"
                }


            }

            override fun onCancelled(error: DatabaseError) {


            }
        })
    }
}