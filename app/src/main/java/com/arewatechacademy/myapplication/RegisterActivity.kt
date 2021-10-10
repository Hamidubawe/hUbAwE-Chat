package com.arewatechacademy.myapplication

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.widget.Toast
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {
    private lateinit var pd : ProgressDialog
    private lateinit var mAuth: FirebaseAuth
    private lateinit var reference: DatabaseReference
    private var deviceToken: String? = null
    private var gender: String = ""
    private var state: String = ""
    private var mAdView: AdView? = null
    private lateinit var token: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        mAuth = FirebaseAuth.getInstance()
        pd = ProgressDialog(this)
        deviceToken = ""

        //initializing ads
        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView!!.loadAd(adRequest)


        login_Btn.setOnClickListener {

            val main = Intent(this,
                LoginActivity::class.java)
            main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(main)
        }

        register_Btn.setOnClickListener {

            pd.setMessage("Creating account pls wait...")
            pd.setCanceledOnTouchOutside(false)
            registerUser()

        }

        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            token = it.token
        }

    }

    private fun registerUser() {

        val usernameInput : String = username_register.text.toString()
        val emailInput = email_register.text.toString()
        val passwordInput : String = password_register.text.toString()
        val confirmPword = confirmPassword.text.toString()
        val statusText = "Hey whats going on"

        if (usernameInput == "" || emailInput == "" || passwordInput == "" ||
            (passwordInput != confirmPword)){

            username_register.error = "username field is empty"
            email_register.error = "email field is empty"
            password_register.error = "password field is empty"

            Toast.makeText(this, "Please fill the form correctly",
                Toast.LENGTH_LONG).show()
        }else {

            pd.show()
            mAuth.createUserWithEmailAndPassword(emailInput, passwordInput)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        mAuth.currentUser!!.sendEmailVerification()
                            .addOnCompleteListener { i ->
                                if (i.isSuccessful) {

                                    val toast = Toast.makeText(
                                        this, "Email verification link is sent to" + " ${mAuth.currentUser!!.email}", Toast.LENGTH_LONG
                                    )
                                    toast.setGravity(Gravity.CENTER, 0, 0)
                                    toast.show()

                                    pd.dismiss()
                                    val userId = mAuth.currentUser!!.uid

                                    reference = FirebaseDatabase.getInstance().reference
                                        .child("Users")
                                        .child(userId)

                                    val userMap = HashMap<String, Any>()
                                    userMap["userId"] = userId
                                    userMap["password"] = passwordInput
                                    userMap["name"] = usernameInput
                                    userMap["profilePhoto"] = "default"
                                    userMap["email"] = emailInput
                                    userMap["presence"] = "offline"
                                    userMap["coverPhoto"] = "default"
                                    userMap["status"] = statusText
                                    userMap["gender"] = "default"
                                    userMap["typing"] = "no"
                                    userMap["state"] = "default"
                                    userMap["currentCity"] = "default"
                                    userMap["phone"] = "default"
                                    userMap["relationship"] = "default"
                                    userMap["token"] = token
                                    userMap["verified"] = false

                                    reference.updateChildren(userMap).addOnCompleteListener { ok ->
                                        if (ok.isSuccessful) {
                                            val login = Intent(
                                                this@RegisterActivity, LoginActivity::class.java
                                            )
                                            login.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                            startActivity(login)
                                            finish()
                                            FirebaseAuth.getInstance().signOut()
                                        }
                                    }

                                } else {
                                    val toast = Toast.makeText(this,
                                        "Registration failed, Please check your credentials and try again",
                                        Toast.LENGTH_LONG
                                    )
                                    toast.setGravity(Gravity.CENTER, 0, 0)
                                    toast.show()
                                    pd.dismiss()
                                }


                            }
                    }
                }
        }
    }
}
