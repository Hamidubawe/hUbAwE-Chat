package com.arewatechacademy.myapplication

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.arewatechacademy.myapplication.Models.PostModel
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
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_add_post.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
const val POST = "/topics/myTopic"
class AddPostActivity : AppCompatActivity() {
    private lateinit var postContent : String
    private var imageUri: Uri? = null
    private val PICKPHOTO = 121
    private var type: String = "text"
    private var photoUrl = ""
    private lateinit var newPostEtx : EditText
    private lateinit var builder: AlertDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        //if theres anyone ive never seen feel pain or sorrow then its you
        //if only every decision in this world is that easy
        //sometimes we tried to help others and end up sinking ourselves

        newPostEtx= findViewById(R.id.newPostEtx)

        builder = AlertDialog.Builder(this).create()
        val mView: View = layoutInflater.inflate(R.layout.native_ad_layout, null)
        val nativeView: TemplateView = mView.findViewById(R.id.my_template)
        val layout : RelativeLayout = mView.findViewById(R.id.progressRelative)

        AdLoader.Builder(this, Constants.nativeAd)
            .forUnifiedNativeAd { ad: UnifiedNativeAd ->
                // Show the ad.
                val styles = NativeTemplateStyle.Builder()
                    .build()
                nativeView.setStyles(styles)
                nativeView.setNativeAd(ad)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {

                }
                override fun onAdClosed() {
                    super.onAdClosed()
                    AdRequest.Builder().build()
                }
                override fun onAdLoaded() {
                    nativeView.visibility = View.VISIBLE

                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build()).build()
            .loadAd(AdRequest.Builder().build())

        layout.visibility = View.VISIBLE
        builder.setView(mView)



        publishBtn.setOnClickListener {

            postContent = newPostEtx.text.toString()
            if (postContent != ""){

                uploadToFirebase(postContent)
            }
            else
                Toast.makeText(this, "Empty field", Toast.LENGTH_SHORT).show()
        }

        chosePhoto.setOnClickListener {

            val pickPhoto = Intent()
            pickPhoto.type = "image/*"
            pickPhoto.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(pickPhoto, PICKPHOTO)

        }
    }

    private fun getFileExtension(uri: Uri): String? {

        val contentResolver = contentResolver
        val mimetype = MimeTypeMap.getSingleton()
        return mimetype.getExtensionFromMimeType(contentResolver.getType(uri))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == PICKPHOTO && resultCode == RESULT_OK && data!!.data != null){
            imageUri = data.data!!
            type = "photo"

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            try {
                postImage.visibility = View.VISIBLE
                postImage.setImageBitmap(bitmap)
            }catch (e: IOException){
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
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
                        this@AddPostActivity, response.errorBody().toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        }
        catch (e: Exception){

        }

    }

    private fun uploadToFirebase(postContent: String) {
        builder.show()
        val reference = FirebaseDatabase.getInstance().reference.child("Post")
        val storageRef = FirebaseStorage.getInstance().reference.child("Post")
        val currentUser = FirebaseAuth.getInstance().currentUser!!.uid

        val postId = reference.push().key!!

        if (type == "photo"){

            val filepath = storageRef.child(postId + "." + getFileExtension(imageUri!!))
            filepath.putFile(imageUri!!)
                .addOnSuccessListener {
                    filepath.downloadUrl.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                photoUrl = task.result.toString()
                                val postModel = PostModel(postId, currentUser,
                                    postContent, photoUrl, type, System.currentTimeMillis().toString())

                                reference.child(postId).setValue(postModel).addOnCompleteListener{ task1 ->
                                    if (task1.isSuccessful){

                                        builder.dismiss()
                                        Toast.makeText(this, "published successfully", Toast.LENGTH_SHORT).show()

                                        //going to post activity to view the post
                                        val i = Intent(this, PostActivity::class.java)
                                        i.putExtra("postId", postId)
                                        overridePendingTransition(R.anim.fui_slide_out_left, R.anim.fui_slide_in_right)
                                        startActivity(i)
                                        finish()

                                        //sending notification of a new post
                                        PushNotification(
                                            NotificationData("New Post Alert",
                                                "Trending posts you may have missed", "", "post"), POST).also {
                                            sendNotification(it)
                                        }



                                    }
                                }
                            }
                        }
                }
                .addOnFailureListener {
                    builder.dismiss()
                    Toast.makeText(this, "Failed to publish post", Toast.LENGTH_SHORT).show()
                }
        }
        else{
            val postModel = PostModel(postId, currentUser, postContent, photoUrl, type, System.currentTimeMillis().toString())
            reference.child(postId).setValue(postModel).addOnCompleteListener{ task1 ->
                if (task1.isSuccessful){

                    Toast.makeText(this, "published successfully", Toast.LENGTH_SHORT).show()

                    //going to post activity to view the post
                    val i = Intent(this, PostActivity::class.java)
                    i.putExtra("postId", postId)
                    overridePendingTransition(R.anim.fui_slide_out_left, R.anim.fui_slide_in_right)
                    startActivity(i)
                    finish()

                    //sending notification of a new post
                    PushNotification(
                        NotificationData("New Post Alert",
                            "Trending posts you may have missed", "", "post"), POST).also {
                        sendNotification(it)
                    }
                    builder.dismiss()

                }
                else{
                    Toast.makeText(this, "Error pls check your internet", Toast.LENGTH_SHORT ).show()
                }
            }
        }
    }

}