package com.arewatechacademy.myapplication

import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class hUbAwE : Application() {
    var reference : DatabaseReference? = null

    override fun onCreate() {
        super.onCreate()

        //FirebaseDatabase.getInstance().setPersistenceEnabled(true)

        if (FirebaseAuth.getInstance().currentUser != null){

            val currentUser = FirebaseAuth.getInstance().currentUser
            val reference = FirebaseDatabase.getInstance().reference.child("Users")
            val map = HashMap<String, Any>()
            map["presence"] = "online"
            reference.child(currentUser!!.uid).updateChildren(map)
        }
    }

    override fun onTerminate() {
        super.onTerminate()

        if (FirebaseAuth.getInstance().currentUser != null){

            val currentUser = FirebaseAuth.getInstance().currentUser
            val reference = FirebaseDatabase.getInstance().reference.child("Users")
            val map = HashMap<String, Any>()
            map["presence"] = System.currentTimeMillis().toString()
            reference.child(currentUser!!.uid).updateChildren(map)
        }

    }

}