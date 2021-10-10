package com.arewatechacademy.myapplication.Adapters

import android.content.Context
import android.content.Intent
import android.text.format.DateFormat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.arewatechacademy.myapplication.*
import com.arewatechacademy.myapplication.Models.CommentModel
import com.arewatechacademy.myapplication.Models.PostModel
import com.arewatechacademy.myapplication.Models.UsersModel
import com.arewatechacademy.myapplication.Notifications.NotificationData
import com.arewatechacademy.myapplication.Notifications.PushNotification
import com.arewatechacademy.myapplication.Notifications.RetrofitInstance
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.formats.NativeAdOptions
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class PostAdapter(val context: Context, val postList: List<PostModel>)
    :RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var liked = false
    var unliked = false
    var currentUsername = ""
    val currentUser = FirebaseAuth.getInstance().currentUser
    private lateinit var userToken: String

    private val DEFAULT_VIEW_TYPE = 1
    private val NATIVE_AD_VIEW_TYPE = 2


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return if (viewType == DEFAULT_VIEW_TYPE){

            val view = LayoutInflater.from(context).inflate(R.layout.post_layout, parent, false)
            PostHolder(view)
        }else{
            val view = LayoutInflater.from(context).inflate(R.layout.native_ad_layout, parent, false)
            NativeAdHolder(view, context)
        }

    }

    override fun getItemViewType(position: Int): Int {

        return if ((position + 1) %4 == 0 && (position + 1) != 0) {
            NATIVE_AD_VIEW_TYPE
        }else
            DEFAULT_VIEW_TYPE
    }

    private fun getUsername(posterName: TextView, userPostPhoto: CircleImageView,
                            publisher: String?, verified:ImageView) {

        val ref = FirebaseDatabase.getInstance().reference.child("Users")
            .child(publisher!!)

        ref.addValueEventListener(object : ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(UsersModel::class.java)

                posterName.text = user!!.name
                userToken = user.token!!
                try {
                    Picasso.get().load(user.profilePhoto).placeholder(R.drawable.avatar)
                        .into(userPostPhoto)
                } catch (e: Exception){
                    userPostPhoto.setImageResource(R.drawable.avatar)
                }
                if (user.verified == true){
                    verified.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {


            }

        })

    }

    override fun onBindViewHolder(mHolder: RecyclerView.ViewHolder, position: Int) {

        if (mHolder.itemViewType == DEFAULT_VIEW_TYPE){

            val holder : PostHolder = mHolder as PostHolder

            val post = postList[position]
            getUsername(holder.posterName, holder.userPostPhoto, post.publisher, holder.verified )
            getLikesCount(position, holder.likesCount, holder.likeBtn)
            getUnlikeCounts(position, holder.unlikesCount, holder.unlikeBtn )
            getCommentCounts(position, holder.commentCount)

            FirebaseDatabase.getInstance().reference.child("Users").child(currentUser!!.uid)
                .addValueEventListener(object :ValueEventListener{

                    override fun onDataChange(snapshot: DataSnapshot) {

                        val me = snapshot.getValue(UsersModel::class.java)
                        currentUsername = me!!.name!!
                    }

                    override fun onCancelled(error: DatabaseError) {


                    }

                })


            if (post.postTimestamp.isNullOrEmpty()){
                holder.postDate.visibility = View.GONE
            }
            else{
                setPostTIme(post.postTimestamp, holder.postDate)
            }

            if (post.type == "photo") {
                holder.postImage.visibility = View.VISIBLE
                try {
                    Picasso.get().load(post.postPhoto).placeholder(R.drawable.loading)
                        .into(holder.postImage)
                } catch (e: Exception){
                    holder.postImage.setImageResource(R.drawable.avatar)
                }
            } else
                holder.postImage.visibility = View.GONE
            holder.postText.text = post.postText

            holder.likeBtn.setOnClickListener {

                if (liked) {
                    FirebaseDatabase.getInstance().reference.child("Likes")
                        .child(postList[position].postId).child(currentUser.uid).removeValue()
                    holder.likeBtn.setImageResource(R.drawable.ic_thumbs_up)
                }
                if (unliked){
                    FirebaseDatabase.getInstance().reference.child("UnLikes").
                    child(postList[position].postId).child(currentUser.uid).removeValue()
                    holder.unlikeBtn.setImageResource(R.drawable.ic_thumbs_down)

                    /*FirebaseDatabase.getInstance().reference.child("Likes")
                        .child(postList[position].postId).child(currentUser.uid).setValue("liked")
                    holder.likeBtn.setImageResource(R.drawable.ic_liked)*/
                }
                if (!liked) {

                    FirebaseDatabase.getInstance().reference.child("Likes")
                        .child(postList[position].postId).child(currentUser.uid).setValue("liked")
                    holder.likeBtn.setImageResource(R.drawable.ic_liked)

                    PushNotification(
                        NotificationData("Liked", "$currentUsername liked your photo", post.postId
                            , "comment"),
                        userToken).also {
                        sendNotification(it)
                    }
                }

            }

            holder.unlikeBtn.setOnClickListener {

                if (unliked) {
                    FirebaseDatabase.getInstance().reference.child("UnLikes")
                        .child(postList[position].postId).child(currentUser.uid).removeValue()
                    holder.unlikeBtn.setImageResource(R.drawable.ic_thumbs_down)
                }
                if (liked){
                    FirebaseDatabase.getInstance().reference.child("Likes").
                    child(postList[position].postId).child(currentUser.uid).removeValue()
                    holder.likeBtn.setImageResource(R.drawable.ic_thumbs_up)

                    FirebaseDatabase.getInstance().reference.child("UnLikes")
                        .child(postList[position].postId).child(currentUser.uid).setValue("unliked")
                    holder.unlikeBtn.setImageResource(R.drawable.ic_unliked)
                }
                if (!unliked) {
                    FirebaseDatabase.getInstance().reference.child("UnLikes")
                        .child(postList[position].postId).child(currentUser.uid).setValue("unliked")
                    holder.unlikeBtn.setImageResource(R.drawable.ic_unliked)

                    PushNotification(
                        NotificationData("New Like",
                            "$currentUsername unlikes your photo", post.postId, "comment"), userToken).also {
                        sendNotification(it)
                    }

                }

            }

            holder.itemView.setOnClickListener {

                val i = Intent(context, PostActivity::class.java)
                i.putExtra("postId", post.postId)
                context.startActivity(i)

            }

            holder.posterName.setOnClickListener {

                val intent = Intent(context, ProfileActivity::class.java)
                intent.putExtra("user_id", post.publisher)
                context.startActivity(intent)

            }

            holder.likesLayout.setOnClickListener {
                val i = Intent(context, LikesActivity::class.java)
                i.putExtra("postId", post.postId)
                context.startActivity(i)
            }

        }

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

    class NativeAdHolder(view: View, context: Context): RecyclerView.ViewHolder(view){

        private lateinit var template: TemplateView
        val adLoader = AdLoader.Builder(context, Constants.nativeAd)
            .forUnifiedNativeAd { ad: UnifiedNativeAd ->
                // Show the ad.
                val styles = NativeTemplateStyle.Builder()
                    //.withMainBackgroundColor(ColorDrawable(Color.BLACK))
                    .build()

                template = view.findViewById(R.id.my_template)
                template.setStyles(styles)
                template.setNativeAd(ad)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {

                }

                override fun onAdClosed() {
                    super.onAdClosed()
                    AdRequest.Builder().build()
                }

                override fun onAdLoaded() {
                    template.visibility = View.VISIBLE

                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build()).build()
            .loadAd(AdRequest.Builder().build())


    }

    private fun sendNotification(notification : PushNotification)
            = CoroutineScope(Dispatchers.IO).launch{

        try {
            val response = RetrofitInstance.api.postNotification(notification)

            when {
                response.isSuccessful -> {

                }
                else -> {
                    Toast.makeText(
                        context, response.errorBody().toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        }
        catch (e: Exception){

        }

    }

    private fun getLikesCount(position: Int, like: TextView, likeBtn: ImageView){

        val ref = FirebaseDatabase.getInstance().reference.child("Likes")
            .child(postList[position].postId)


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

    private fun getUnlikeCounts(position: Int, unlike: TextView, favorBtn: ImageView) {


        val ref = FirebaseDatabase.getInstance().reference.child("UnLikes")
            .child(postList[position].postId)

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

    private fun getCommentCounts(position: Int, commentCount: TextView) {

        val ref = FirebaseDatabase.getInstance().reference.child("Comments")

        ref.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                var commentNumber = 0
                for (ds in snapshot.children){

                    val comment = ds.getValue(CommentModel::class.java)
                    if (comment!!.postId == postList[position].postId) {
                        commentNumber += 1
                        commentCount.text = "$commentNumber comments"
                    }
                    else
                        commentCount.text = "0 Comments"
                }


            }

            override fun onCancelled(error: DatabaseError) {


            }
        })
    }

    override fun getItemCount(): Int {

        return postList.size
    }

    class PostHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        val userPostPhoto : CircleImageView = itemView.findViewById(R.id.userPostPhoto)
        val posterName : TextView = itemView.findViewById(R.id.posterName)
        val postText : TextView = itemView.findViewById(R.id.postText)
        val postImage : ImageView = itemView.findViewById(R.id.postImage)
        val verified: ImageView = itemView.findViewById(R.id.verified)
        val likeBtn : ImageView = itemView.findViewById(R.id.likeBtn)
        val unlikeBtn : ImageView = itemView.findViewById(R.id.unlikeBtn)
        val likesCount: TextView = itemView.findViewById(R.id.likesCount)
        val commentCount: TextView = itemView.findViewById(R.id.commentsCount)
        val unlikesCount: TextView = itemView.findViewById(R.id.unlikesCount)
        val likesLayout: RelativeLayout = itemView.findViewById(R.id.likesLayout)
        val postDate:TextView = itemView.findViewById(R.id.postDate)



    }

}