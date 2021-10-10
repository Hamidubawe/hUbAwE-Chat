package com.arewatechacademy.myapplication.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.arewatechacademy.myapplication.Models.CommentModel
import com.arewatechacademy.myapplication.Models.UsersModel
import com.arewatechacademy.myapplication.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class CommentAdapter(val context: Context, val commentList: List<CommentModel>)
    :RecyclerView.Adapter<CommentAdapter.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(context).inflate(R.layout.comment_layout, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int {

        return commentList.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val comment = commentList[position]
        holder.comment.text = comment.comment
        getUsername(holder.commmentName, holder.commentorPhoto, comment.publisher)
    }

    private fun getUsername(posterName: TextView, userPostPhoto: CircleImageView, publisher: String?) {

        val ref = FirebaseDatabase.getInstance().reference.child("Users")
            .child(publisher!!)

        ref.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(UsersModel::class.java)

                posterName.text = user!!.name
                try {
                    Picasso.get().load(user.profilePhoto).placeholder(R.drawable.avatar)
                        .into(userPostPhoto)
                } catch (e: Exception){
                    userPostPhoto.setImageResource(R.drawable.avatar)
                }

            }

            override fun onCancelled(error: DatabaseError) {


            }

        })

    }


    class Holder(view: View) : RecyclerView.ViewHolder(view){

        val commentorPhoto = view.findViewById<CircleImageView>(R.id.commentorPhoto)
        val commmentName = view.findViewById<TextView>(R.id.commentName)
        val comment = view.findViewById<TextView>(R.id.comment)

    }

}