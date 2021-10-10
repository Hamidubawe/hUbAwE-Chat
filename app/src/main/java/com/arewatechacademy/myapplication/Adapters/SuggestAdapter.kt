package com.arewatechacademy.hubawe.Adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.arewatechacademy.myapplication.Models.UsersModel
import com.arewatechacademy.myapplication.ProfileActivity
import com.arewatechacademy.myapplication.R
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class SuggestAdapter(
    val mActivity: Activity,
    private val mSuggestions: List<UsersModel>,
    private val isSuggested : Boolean
) : RecyclerView.Adapter<SuggestAdapter.Holder?>() {


    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var nAme: TextView = itemView.findViewById(R.id.peopleName)
        var profilePic: ImageView = itemView.findViewById(R.id.peoplePhoto)
        var profileButton : Button = itemView.findViewById(R.id.viewProfile)
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view : View = LayoutInflater.from(mActivity)
            .inflate(R.layout.people_layout, parent, false)

        return Holder(view)
    }

    override fun getItemCount(): Int {
        return mSuggestions.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {

        val user: UsersModel? = mSuggestions[position]
        val username = mSuggestions[position].name

        holder.nAme.text = username!!

        if (!user!!.profilePhoto.equals("default")) {
            Picasso.get().load(user.profilePhoto).placeholder(R.drawable.avatar).into(holder.profilePic)
        }else
            holder.profilePic.setImageResource(R.drawable.avatar)

        holder.itemView.setOnClickListener {

            val intent = Intent(mActivity, ProfileActivity::class.java)
            intent.putExtra("user_id", user.userId)
            mActivity.startActivity(intent)
            mActivity.overridePendingTransition(R.anim.slide_out_bottom, R.anim.slide_in_bottom)
        }
        holder.profileButton.setOnClickListener {
            val intent = Intent(mActivity, ProfileActivity::class.java)
            intent.putExtra("user_id", user.userId)
            mActivity.startActivity(intent)
            mActivity.overridePendingTransition(R.anim.slide_out_bottom, R.anim.slide_in_bottom)
        }
    }
}