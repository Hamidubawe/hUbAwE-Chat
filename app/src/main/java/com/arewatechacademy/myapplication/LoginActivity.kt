package com.arewatechacademy.myapplication

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    lateinit var mAuth: FirebaseAuth
    var progressDialog: ProgressDialog? = null
    lateinit var email : EditText
    lateinit var password: EditText
    private lateinit var mAdView: AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        progressDialog = ProgressDialog(this)



        mAuth = FirebaseAuth.getInstance()

        email = findViewById(R.id.email_login)
        password = findViewById(R.id.password_login)

        login_Btn.setOnClickListener{

            progressDialog!!.setTitle("Logging In")
            progressDialog!!.setMessage("Please wait a second")
            progressDialog!!.setCanceledOnTouchOutside(false)


            val emailInput: String = email.text.toString()
            val passwordInput: String = password.text.toString()



            if (!TextUtils.isEmpty(emailInput) && !TextUtils.isEmpty(passwordInput)) {
                progressDialog!!.show()
                signIn(emailInput, passwordInput)
            }

            else {
                progressDialog!!.dismiss()
                Toast.makeText(this@LoginActivity, "Please fill the form correctly",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        forgot_Btn.setOnClickListener {
            resetPassword()
        }

        reg_Btn_login.setOnClickListener {
            val main = Intent(this@LoginActivity,
                RegisterActivity::class.java)
            //main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(main)
        }

        //initializing ads
        mAdView = findViewById<AdView>(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

    }

    private fun resetPassword() {
        val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
        val pd = ProgressDialog(this)
        pd.setMessage("Sending password reset link...")
        pd.setCanceledOnTouchOutside(false)

        val builder = AlertDialog.Builder(this).create()
        val mView : View = layoutInflater.inflate(R.layout.text_layout, null)
        val email : EditText = mView.findViewById(R.id.editText)
        val updateBtn : Button = mView.findViewById(R.id.saveBtn)
        val cancelBtn : Button = mView.findViewById(R.id.cancelButton)
        val title : TextView = mView.findViewById(R.id.headertitle)

        title.text = "Recover Password"
        email.hint = "Enter your email"
        updateBtn.text = "Reset"


        updateBtn.setOnClickListener {

            val emailLink = email.text.toString().trim()

            if (emailLink.isEmpty())
                Toast.makeText(this, "error, status can't be blank", Toast.LENGTH_LONG).show()

            else if (emailLink.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(emailLink).matches()){

                pd.show()
                mAuth.sendPasswordResetEmail(emailLink).addOnCompleteListener { task ->
                    if (task.isSuccessful){
                        Toast.makeText(this, "Password reset link has been sent to $emailLink",
                            Toast.LENGTH_LONG).show()
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

    private fun signIn(email: String, password: String) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    if (mAuth.currentUser!!.isEmailVerified) {

                        val mUser = mAuth.currentUser
                        updateUi(mUser)
                    }else{

                        val toast = Toast.makeText(this,
                            "You haven't verify your email: ${mAuth.currentUser!!.email}",
                            Toast.LENGTH_LONG)
                        toast.setGravity(Gravity.CENTER,0,0)
                        toast.show()
                        progressDialog!!.dismiss()
                    }


                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        "Login failed pls check your login details and try again",
                        Toast.LENGTH_SHORT
                    ).show()

                }
                progressDialog!!.dismiss()
            }
    }

    private fun updateUi(user: FirebaseUser?) {
        val main = Intent(this@LoginActivity,
            MainActivity::class.java)
        main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(main)
    }
}
