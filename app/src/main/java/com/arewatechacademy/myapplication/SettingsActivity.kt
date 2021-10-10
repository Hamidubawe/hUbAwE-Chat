package com.arewatechacademy.myapplication

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.arewatechacademy.myapplication.Models.UsersModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {
    private lateinit var mUserRef: DatabaseReference
    private var listView: ListView? = null
    private lateinit var pd: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        pd = ProgressDialog(this)
        pd.setMessage("Updating please wait")
        pd.setCanceledOnTouchOutside(false)
        val user = FirebaseAuth.getInstance().currentUser

        mUserRef = FirebaseDatabase.getInstance().reference.child("Users")
            .child(user!!.uid)

        listView = findViewById(R.id.settingListView)
        val listItems = arrayOf(
            "Change Name", "Update Phone Number", "Update Current City", "Change password",
            "Update Email", "Relationship Status", "State of Residence", "Update Gender"
        )

        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listItems)
        listView!!.adapter = arrayAdapter

        listView!!.setOnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> updateName(mUserRef)
                1 -> updatePhone(mUserRef)
                2 -> updateCity(mUserRef)
                3 -> updatePassword(user, mUserRef)
                4 -> updateEmail(user)
                5 -> chooseRelationStatus(mUserRef)
                6 -> chooseState(mUserRef)
                7 -> updateGender(mUserRef)
            }
        }

        mUserRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val myName: UsersModel? = dataSnapshot.getValue(
                        UsersModel::class.java)

                    usernameSettings.text = myName!!.name
                    val photo = myName.profilePhoto!!
                    if (photo != "default") {
                        Picasso.get().load(photo).placeholder(R.drawable.avatar)
                            .into(settingsImage)
                    } else
                        settingsImage.setImageResource(R.drawable.avatar)


                }

            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

        logOut.setOnClickListener {

            val dialog: AlertDialog.Builder = AlertDialog.Builder(this)
            dialog.setMessage("Log out of your account??")
                .setTitle("Log Out>")
                .setPositiveButton("Log Out") { _, _ ->

                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this, LoginActivity::class.java)
                    overridePendingTransition(
                        R.anim.slide_in_bottom,
                        R.anim.slide_out_bottom
                    )
                    startActivity(intent)
                }

                .setNegativeButton("No", null)
                .create().show()
        }


    }

    private fun updateCity(mUserRef: DatabaseReference) {
        val builder = AlertDialog.Builder(this).create()
        val mView: View = layoutInflater.inflate(R.layout.text_layout, null)
        val nameTxt: EditText = mView.findViewById(R.id.editText)
        val updateBtn: Button = mView.findViewById(R.id.saveBtn)
        val cancelBtn: Button = mView.findViewById(R.id.cancelButton)
        val title: TextView = mView.findViewById(R.id.headertitle)

        title.text = "Update Current City"
        nameTxt.hint = "city name..."

        updateBtn.setOnClickListener {

            val statusTxt = nameTxt.text.toString().trim()
            if (statusTxt.isEmpty())
                Toast.makeText(this, "Field is empty", Toast.LENGTH_LONG).show()
            else {

                pd.show()
                val map = HashMap<String, Any>()
                map["currentCity"] = statusTxt
                mUserRef.updateChildren(map).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Current City Updated successfully", Toast.LENGTH_LONG).show()
                        builder.dismiss()
                        pd.dismiss()
                    } else {
                        Toast.makeText(this,
                            "Failed to update current please try again later", Toast.LENGTH_LONG).show()
                        builder.dismiss()
                        pd.dismiss()

                    }

                }


            }
        }
        cancelBtn.setOnClickListener {
            builder.dismiss()
        }

        builder.setView(mView)
        builder.show()


    }

    private fun chooseRelationStatus(mUserRef: DatabaseReference) {


        val actions = arrayOf<CharSequence>(
            "Single", "Its Complicated", "Engaged", "Married", "Divorced", "Widowed"
        )
        var currentRelation = "default"
        val builder =
            AlertDialog.Builder(this)
                .setTitle("Select Status")
                .setItems(actions) { _, i ->
                    if (i == 0) {
                        currentRelation = "Single"
                    }
                    if (i == 1) {
                        currentRelation = "Complicated"
                    }
                    if (i == 2) {
                        currentRelation = "Engaged"
                    }
                    if (i == 3) {
                        currentRelation = "Married"
                    }
                    if (i == 4) {
                        currentRelation = "Divorced"
                    }
                    if (i == 5) {
                        currentRelation = "Widow"
                    }
                    pd.show()

                    mUserRef.child("relationship").setValue(currentRelation)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                pd.dismiss()
                                Toast.makeText(
                                    this,
                                    "Relationship status is updated",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    this,
                                    "Failed to update relationship status",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                }
        builder.show()

    }

    private fun updateEmail(user: FirebaseUser) {

        val builder = AlertDialog.Builder(this).create()
        val mView: View = layoutInflater.inflate(R.layout.text_layout, null)
        val nameTxt: EditText = mView.findViewById(R.id.editText)
        val editText2: EditText = mView.findViewById(R.id.editText2)
        val editText3: EditText = mView.findViewById(R.id.editText3)
        val updateBtn: Button = mView.findViewById(R.id.saveBtn)
        val cancelBtn: Button = mView.findViewById(R.id.cancelButton)
        val title = mView.findViewById<TextView>(R.id.headertitle)

        editText2.visibility = View.VISIBLE

        nameTxt.hint = "Current Email"
        editText2.hint = "New Email"
        title.text = "Update Mail "



        updateBtn.setOnClickListener {

            val currentEmail = nameTxt.text.toString()
            val newEmail = editText2.text.toString()
            val myMail = user.email

            if (currentEmail == myMail && Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()
                && Patterns.EMAIL_ADDRESS.matcher(currentEmail).matches()
            ) {

                pd.show()
                user.updateEmail(newEmail).addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        Toast.makeText(this, "Email Changed successfully", Toast.LENGTH_LONG).show()
                        builder.dismiss()
                        pd.dismiss()

                    } else {
                        pd.dismiss()
                        Toast.makeText(this, "failed to update email", Toast.LENGTH_LONG).show()
                    }
                }

            } else {
                Toast.makeText(this, "Pls type your email correctly", Toast.LENGTH_LONG).show()
            }


        }
        cancelBtn.setOnClickListener {
            builder.dismiss()
        }

        builder.setView(mView)
        builder.show()


    }

    private fun updatePassword(user: FirebaseUser, mUserRef: DatabaseReference) {


        val builder = AlertDialog.Builder(this).create()
        val mView: View = layoutInflater.inflate(R.layout.text_layout, null)
        val oldPassword: EditText = mView.findViewById(R.id.editText)
        val newPassword: EditText = mView.findViewById(R.id.editText2)
        val confirmPassword: EditText = mView.findViewById(R.id.editText3)
        val updateBtn: Button = mView.findViewById(R.id.saveBtn)
        val cancelBtn: Button = mView.findViewById(R.id.cancelButton)
        val title = mView.findViewById<TextView>(R.id.headertitle)


        newPassword.visibility = View.VISIBLE
        confirmPassword.visibility = View.VISIBLE

        oldPassword.hint = "Old Password"
        newPassword.hint = "New Password"
        confirmPassword.hint = "Confirm new Password"
        title.text = "Update Password"
        var updatedPassword = ""
        var currentPassword = ""

        mUserRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val user = snapshot.getValue(UsersModel::class.java)

                currentPassword = user!!.password!!
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })


        updateBtn.setOnClickListener {

            val newPassowrd = newPassword.text.toString()
            val confirmPasswrd = confirmPassword.text.toString()

            when {
                newPassowrd != confirmPasswrd -> Toast.makeText(this, "Error password do not match", Toast.LENGTH_LONG).show()
                oldPassword.text.toString() != currentPassword -> Toast.makeText(this, "Old Password is not correct", Toast.LENGTH_LONG).show()
                else -> {

                    pd.show()
                    user.updatePassword(newPassowrd).addOnCompleteListener { task ->
                        if (task.isSuccessful) {

                            mUserRef.child("password").setValue(newPassowrd)
                            pd.dismiss()
                            Toast.makeText(this, "Password updated successfully", Toast.LENGTH_LONG)
                                .show()
                            builder.dismiss()

                        }
                    }
                }
            }

        }
        cancelBtn.setOnClickListener {
            builder.dismiss()
        }

        builder.setView(mView)
        builder.show()

    }

    private fun updatePhone(mUserRef: DatabaseReference) {

        val builder = AlertDialog.Builder(this).create()
        val mView: View = layoutInflater.inflate(R.layout.text_layout, null)
        val nameTxt: EditText = mView.findViewById(R.id.editText)
        val updateBtn: Button = mView.findViewById(R.id.saveBtn)
        val cancelBtn: Button = mView.findViewById(R.id.cancelButton)
        val title = mView.findViewById<TextView>(R.id.headertitle)

        nameTxt.hint = "Phone number"
        title.text = "Update Phone Number"



        updateBtn.setOnClickListener {

            var statusTxt = nameTxt.text.toString().trim()
            if (statusTxt.isEmpty())
                Toast.makeText(this, "Error input is empty", Toast.LENGTH_LONG).show()
            else {

                pd.show()
                val map = HashMap<String, Any>()
                map["phone"] = statusTxt
                mUserRef.updateChildren(map).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        pd.dismiss()
                        Toast.makeText(this, "Phone Number updated successfully", Toast.LENGTH_LONG)
                            .show()
                    } else {
                        Toast.makeText(
                            this, "Failed to update phone number please try again later",
                            Toast.LENGTH_LONG
                        ).show()
                        pd.dismiss()
                    }
                    builder.dismiss()
                }


            }
        }
        cancelBtn.setOnClickListener {
            builder.dismiss()
        }

        builder.setView(mView)
        builder.show()

    }

    private fun updateName(mUserRef: DatabaseReference) {

        val builder = AlertDialog.Builder(this).create()
        val mView: View = layoutInflater.inflate(R.layout.text_layout, null)
        val nameTxt: EditText = mView.findViewById(R.id.editText)
        val updateBtn: Button = mView.findViewById(R.id.saveBtn)
        val cancelBtn: Button = mView.findViewById(R.id.cancelButton)
        val title: TextView = mView.findViewById(R.id.headertitle)

        title.text = "Update Username"
        nameTxt.hint = "Username..."




        updateBtn.setOnClickListener {

            val statusTxt = nameTxt.text.toString().trim()
            if (statusTxt.isEmpty())
                Toast.makeText(this, "Error, input is empty", Toast.LENGTH_LONG).show()
            else {

                pd.show()
                val map = HashMap<String, Any>()
                map["name"] = statusTxt
                mUserRef.updateChildren(map).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Name Updated successfully", Toast.LENGTH_LONG).show()
                        builder.dismiss()
                        pd.dismiss()
                    } else {
                        Toast.makeText(this,
                            "Failed to update name please try again later", Toast.LENGTH_LONG).show()
                        builder.dismiss()
                        pd.dismiss()

                    }

                }


            }
        }
        cancelBtn.setOnClickListener {
            builder.dismiss()
        }

        builder.setView(mView)
        builder.show()
    }

    private fun chooseState(mUserRef: DatabaseReference) {
        val states = arrayOf("Abia", "Adamawa",
            "Anambra", "Akwa Ibom", "Bauchi", "Bayelsa", "Benue", "Borno", "Cross River",
            "Delta", "Ebonyi", "Edo", "Ekiti", "Enugu", "Gombe", "Imo", "Jigawa", "Kaduna", "Kano",
            "Katsina", "Kebbi", "Kogi", "Kwara", "Lagos", "Nasarawa", "Niger", "Ogun", "Ondo", "Osun",
            "Oyo", "Plateau", "Rivers", "Sokoto", "Taraba", "Yobe", "Zamfara", "Abuja")

        val builder = android.app.AlertDialog.Builder(this)
        builder.setSingleChoiceItems(states, -1) { _, i ->
            val state = listOf(*states)[i]

            mUserRef.child("state").setValue(state).addOnCompleteListener { task ->
                if (task.isSuccessful) {

                } else {

                    Toast.makeText(this, "Couldn't update faculty", Toast.LENGTH_SHORT).show()

                }
            }
        }
        builder.setPositiveButton("Update", null).setNegativeButton("Cancel", null)
            .show()


    }

    private fun updateGender(mUserRef: DatabaseReference) {

        val actions = arrayOf<CharSequence>(
            "Male", "Female", "Rather not say"
        )
        val builder = AlertDialog.Builder(this)
            .setTitle("Select Gender")
            .setItems(actions) { _, i ->
                if (i == 0) {
                    mUserRef.child("gender").setValue("Male")

                }
                if (i == 1) {
                    mUserRef.child("gender").setValue("Female")
                }
                if (i == 2) {
                    mUserRef.child("gender").setValue("default")
                }

            }
        builder.show()
    }
}
