package com.arewatechacademy.hubawe.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.arewatechacademy.myapplication.Models.UsersModel
import com.arewatechacademy.myapplication.ProfileActivity
import com.arewatechacademy.myapplication.R
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class UsersAdapter(context: Context,
                   mUsers: List<UsersModel>,
                   isChatChecked: Boolean) : RecyclerView.Adapter<UsersAdapter.ViewHolder?>() {
    private val mUsers: List<UsersModel>
    private val context: Context
    private var isChatChecked: Boolean
    init {
        this.mUsers = mUsers
        this.context = context
        this.isChatChecked = isChatChecked
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view : View = LayoutInflater.from(context)
            .inflate(R.layout.single_user_layout, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mUsers.size
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val user: UsersModel? = mUsers[position]
        val username = mUsers[position].name

        holder.name.text = username!!
        holder.status.text = mUsers[position].status!!

        if (!user!!.profilePhoto.equals("default")) {
            Picasso.get().load(user.profilePhoto).placeholder(R.drawable.avatar).into(holder.profilePic)
        }else
            holder.profilePic.setImageResource(R.drawable.avatar)



        holder.itemView.setOnClickListener {
            setUpInsterAds()
            val intent = Intent(context, ProfileActivity::class.java)
            intent.putExtra("user_id", user.userId)
            context.startActivity(intent)
        }

    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        var name: TextView
        var status: TextView
        var profilePic: CircleImageView


        init {
            name = itemView.findViewById(R.id.user_displayname)
            status = itemView.findViewById(R.id.user_status)
            profilePic = itemView.findViewById(R.id.user_photo)
        }

    }

    private fun setUpInsterAds(){
        val mInterstitialAd: InterstitialAd?

        //initialize inter ads
        mInterstitialAd = InterstitialAd(context)
        mInterstitialAd.adUnitId = R.string.admob_interstitial_id.toString()
        mInterstitialAd.loadAd(AdRequest.Builder().build())

        if (mInterstitialAd.isLoaded) mInterstitialAd.show()
    }
}