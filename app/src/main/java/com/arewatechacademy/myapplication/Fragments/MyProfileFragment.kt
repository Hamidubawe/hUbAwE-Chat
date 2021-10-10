package com.arewatechacademy.myapplication.Fragments


import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.arewatechacademy.myapplication.ImageViewActivity
import com.arewatechacademy.myapplication.Models.UsersModel

import com.arewatechacademy.myapplication.R
import com.arewatechacademy.myapplication.SettingsActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_my_profile.*
import kotlinx.android.synthetic.main.fragment_my_profile.view.*
import kotlinx.android.synthetic.main.fragment_my_profile.view.status_myProfile

/**
 * A simple [Fragment] subclass.
 */
class MyProfileFragment : Fragment() {

    private var ref : DatabaseReference? = null
    private var mAuth : FirebaseAuth? = null
    private var myId : FirebaseUser? = null
    private var mStorageRef: StorageReference? = null
    private lateinit var updateSttus : Button
    private lateinit var dialog: AlertDialog.Builder
    //var imageUri = null
    private lateinit var updatePhto : Button
    private lateinit var photo: String
    private var checkCover : String = ""
    lateinit var cover: String

    var progressDialog: ProgressDialog? = null

    private val GALLERY_PICK = 1



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view : View =  inflater.inflate(R.layout.fragment_my_profile, container, false)
        setHasOptionsMenu(true)

        mAuth = FirebaseAuth.getInstance()
        myId = mAuth!!.currentUser!!
        ref = FirebaseDatabase.getInstance().reference
        mStorageRef = FirebaseStorage.getInstance().reference

        progressDialog = ProgressDialog(context)
        progressDialog!!.setMessage("Loading...")

        ref!!.child("Users").child(myId!!.uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    val user : UsersModel? = p0.getValue(
                        UsersModel::class.java)

                    photo = user!!.profilePhoto!!
                    cover = user.coverPhoto!!
                    if (photo !="default") {
                        Picasso.get().load(photo).placeholder(R.drawable.avatar).into(view.picture_myProfile)
                    }
                    else
                        view.picture_myProfile.setImageResource(R.drawable.avatar)

                    view.status_myProfile.text = user.status!!
                    view.myUsername.text = user.name!!

                    if (user.gender != "default")
                        view.myGender.text = user.gender
                    else
                        view.myGender.text = "Not Assigned"

                    if (user.currentCity == "default")
                        view.myCurrentCity.text = "Not Assigned"
                    else
                        view.myCurrentCity.text = user.currentCity

                    view.MyEmail.text = user.email

                    if (user.coverPhoto != "default"){
                        Picasso.get().load(user.coverPhoto).placeholder(R.drawable.avatar).into(view.myCoverPic)
                    }else
                        view.myCoverPic.setImageResource(R.drawable.avatar)

                    if (!user.state.equals("default")){
                        view.myState.text = user.state
                    }
                    else{
                        view.myState.text = "Not Assigned"
                    }

                    if (!user.phone.equals("default"))
                        view.myPhoneNumber.text = user.phone
                    else
                        view.myPhoneNumber.text = "Not Assigned"

                    if (user.relationship != "default")
                        view.myRelationship.text = user.relationship
                    else
                        view.myRelationship.text = "Not Assigned"


                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

        ref!!.child("Friends").child(myId!!.uid).addValueEventListener(object : ValueEventListener {

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {

                val friends = p0.childrenCount.toString()
                view.friendsCount.text = "Total Friends: $friends"
            }
        })


        view.picture_myProfile.setOnClickListener {
            val intent = Intent(context, ImageViewActivity::class.java)
            intent.putExtra("photo", photo)
            startActivity(intent)
        }

        view.myCoverPic.setOnClickListener {
            val intent = Intent(context, ImageViewActivity::class.java)
            intent.putExtra("photo", cover)
            startActivity(intent)
        }

        updateSttus = view.findViewById(R.id.updateStatus)
        updatePhto = view.findViewById(R.id.updatePhoto)

        updateSttus.setOnClickListener {
            val currentUser = FirebaseAuth.getInstance().currentUser
            val reference: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")

            changeStatus(currentUser, reference)


        }

        updatePhto.setOnClickListener {
            val actions = arrayOf<CharSequence>(
                "Change Profile Photo", "Change Cover Photo"
            )
            val builder =
                AlertDialog.Builder(context!!)
                    .setTitle("Select Action")
                    .setItems(actions) { _, i ->
                        if (i == 0) {

                            val pickImage = Intent()
                            pickImage.type = "image/*"
                            pickImage.action = Intent.ACTION_GET_CONTENT
                            startActivityForResult(pickImage, GALLERY_PICK)

                        }
                        if (i == 1) {

                            checkCover = "coverPhoto"
                            val chngeCover = Intent()
                            chngeCover.type = "image/*"
                            chngeCover.action = Intent.ACTION_GET_CONTENT
                            startActivityForResult(chngeCover, GALLERY_PICK)
                        }
                    }
            builder.show()

        }

        return view
    }

    private fun changeStatus(firebaseUser: FirebaseUser?, reference: DatabaseReference) {

        val builder = AlertDialog.Builder(context!!).create()
        val mView : View = layoutInflater.inflate(R.layout.text_layout, null)
        val status : EditText = mView.findViewById(R.id.editText)
        val updateBtn : Button = mView.findViewById(R.id.saveBtn)
        val cancelBtn : Button = mView.findViewById(R.id.cancelButton)


        updateBtn.setOnClickListener {

            val statusTxt = status.text.toString().trim()
            if (statusTxt.isEmpty())
                Toast.makeText(context, "error, status can't be blank", Toast.LENGTH_LONG).show()
            else{

                val map = HashMap<String, Any>()
                map["status"] = statusTxt
                reference.child(firebaseUser!!.uid).updateChildren(map)
                status_myProfile.text = statusTxt
                builder.dismiss()
            }
        }
        cancelBtn.setOnClickListener {
            builder.dismiss()
        }

        builder.setView(mView)
        builder.show()

    }

    override fun onPrepareOptionsMenu(menu: Menu) {

        val friendsItem: MenuItem = menu.findItem(R.id.findFriends)
        val searchItem: MenuItem = menu.findItem(R.id.app_bar_search)
        val usersItem: MenuItem = menu.findItem(R.id.findFriends)
        //val settingsItem: MenuItem = menu.findItem(R.id.settings)

        friendsItem.isVisible = false
        searchItem.isVisible = false
        usersItem.isVisible = false

        super.onPrepareOptionsMenu(menu)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)  {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY_PICK && resultCode == Activity.RESULT_OK) {
            val imageUri = data!!.data


            if (imageUri != null){

                progressDialog!!.setCanceledOnTouchOutside(false)
                progressDialog!!.setMessage("Updating Photo")
                progressDialog!!.show()

                //getting download url from firebase
                val currentUser: String = myId!!.uid

                if(checkCover == "coverPhoto"){

                    val filepath = mStorageRef!!.child("Cover_Images").child("$currentUser.jpg")
                    filepath.putFile(imageUri).addOnSuccessListener {
                        filepath.downloadUrl.addOnCompleteListener { task ->
                            //val downloadURL = task.result.toString()
                            if (task.isSuccessful) {

                                val downloadURL = task.result.toString()

                                val map = HashMap<String, Any>()
                                map["coverPhoto"] = downloadURL
                                ref!!.child("Users").child(currentUser).updateChildren(map)
                                progressDialog!!.dismiss()

                                Toast.makeText(context, "Cover photo updated successfully",
                                    Toast.LENGTH_LONG
                                ).show()


                            } else {
                                Toast.makeText(context, "Couldn't update Photo", Toast.LENGTH_SHORT
                                ).show()
                                progressDialog!!.dismiss()
                            }
                        }
                    }
                }

                else{

                    val filepath = mStorageRef!!.child("Profile_Images").child("$currentUser.jpg")
                    filepath.putFile(imageUri).addOnSuccessListener {
                        filepath.downloadUrl.addOnCompleteListener { task ->
                            //val downloadURL = task.result.toString()
                            if (task.isSuccessful) {

                                val downloadURL = task.result.toString()

                                val map = HashMap<String, Any>()
                                map["profilePhoto"] = downloadURL
                                ref!!.child("Users").child(currentUser).updateChildren(map)
                                progressDialog!!.dismiss()

                                Toast.makeText(context, "Profile picture updated successfully",
                                    Toast.LENGTH_LONG
                                ).show()



                            } else {
                                Toast.makeText(context, "Couldn't update Photo", Toast.LENGTH_SHORT
                                ).show()
                                progressDialog!!.dismiss()
                            }
                        }
                    }
                }



            }

        }

    }

    override fun onStart() {
        super.onStart()

        val currentUser = FirebaseAuth.getInstance().currentUser!!.uid
        val dbRef = FirebaseDatabase.getInstance().reference.child("Users")
            .child(currentUser)
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userInfo = snapshot.getValue(UsersModel::class.java)

                if (userInfo!!.gender == "default" || userInfo.state == "default"
                    || userInfo.currentCity == "default"){

                    dialog = AlertDialog.Builder(context!!)
                    dialog.setMessage("You haven't set up your profile completely")
                        .setTitle("Complete Profile")
                        .setPositiveButton("Set Up") { _, _ ->
                            val i = Intent(context, SettingsActivity::class.java)
                            startActivity(i)
                        }
                        .setNegativeButton("Cancel", null)
                        .create().show()
                }

            }

            override fun onCancelled(p0: DatabaseError) {


            }

        })
    }
}
