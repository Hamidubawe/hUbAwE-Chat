package com.arewatechacademy.hubawe.Adapters


import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.arewatechacademy.myapplication.R
import com.arewatechacademy.myapplication.ChatActivity
import com.arewatechacademy.myapplication.ImageViewActivity
import com.arewatechacademy.myapplication.Models.ChatModel
import com.arewatechacademy.myapplication.Models.UsersModel

import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class ChatslistsAdapter(var context: Context, mUsers: ArrayList<UsersModel>)
    :RecyclerView.Adapter<ChatslistsAdapter.Holder?>(){



    private var mUsers : List<UsersModel> = mUsers
    var lastMsg = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {

        val view = LayoutInflater.from(context).inflate(R.layout.single_user_layout, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int {
        return mUsers.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val user = mUsers[position]

        holder.name.text = user.name
        if (user.profilePhoto != "default"){
            Picasso.get().load(user.profilePhoto).placeholder(R.drawable.avatar).into(holder.profilePic)
        }else
            holder.profilePic.setImageResource(R.drawable.avatar)
        if (user.presence == "online"){
            holder.online.visibility = View.VISIBLE
        }

        if (user.verified == true ){
            holder.verified.visibility = View.VISIBLE
        }

        retrieveMsg(user.userId, holder.message, holder.name)

        holder.itemView.setOnClickListener {
            val i  = Intent(context, ChatActivity::class.java)
            i.putExtra("user_id", user.userId)
            context.startActivity(i)
        }
        holder.profilePic.setOnClickListener {
            val i  = Intent(context, ImageViewActivity::class.java)
            i.putExtra("photo", user.profilePhoto)
            context.startActivity(i)
        }
    }



    private fun retrieveMsg(userId: String?, message: TextView, name: TextView) {

        val currentUser = FirebaseAuth.getInstance().currentUser!!.uid
        val msgRef = FirebaseDatabase.getInstance().reference.child("Messages")
        msgRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                for (ds in p0.children) {
                    val chat: ChatModel? = ds.getValue(
                        ChatModel::class.java)


                    if (chat!!.sender == currentUser && chat.receiver == userId
                            || chat.sender == userId && chat.receiver == currentUser) {

                        lastMsg = chat.message!!.trim()
                        message.text = lastMsg
                        if (chat.type == "image") {
                            message.text = "Photo"
                        }
                        if (chat.sender == userId && chat.receiver == currentUser && !chat.seen) {
                            name.setTextColor(Color.BLACK)
                            message.setTextColor(Color.BLACK)
                            name.setTypeface(null, Typeface.BOLD)
                            message.setTypeface(null, Typeface.BOLD)
                        }

                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })


    }

    class Holder(itemView: View): RecyclerView.ViewHolder(itemView){
        val name = itemView.findViewById<TextView>(R.id.user_displayname)!!
        val message: TextView = itemView.findViewById(R.id.user_status)
        val profilePic : CircleImageView = itemView.findViewById(R.id.user_photo)
        val online: ImageView = itemView.findViewById(R.id.onlineImage)
        val verified: ImageView = itemView.findViewById(R.id.verified)


    }
}

/* private var chatRef: DatabaseReference? = null
    private var userRef: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    private var currentUser: String? = null
    private var lastMsg = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.single_user_layout,  parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int, model: UsersModel) {

        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth!!.currentUser!!.uid
        val userId = getRef(position).key
        chatRef = FirebaseDatabase.getInstance().reference.child("Chats")
        userRef = FirebaseDatabase.getInstance().reference.child("Users")
        val context = holder.itemView.context

        chatRef!!.child(currentUser!!).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                userRef!!.child(userId!!).addValueEventListener(object : ValueEventListener{

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()){
                            val chat: UsersModel? = p0.getValue(UsersModel::class.java)
                            val name = chat!!.name
                            val image = chat.profilePhoto


                            if (chat.presence == "online"){
                                holder.online.visibility = View.VISIBLE
                            }

                            holder.name.text = name
                            if (!image.equals("default")) {
                                Picasso.get().load(image).placeholder(R.drawable.avatar).into(holder.profilePic)
                            }


                            retrieveMsg(chat.userId, holder.message, holder.name)

                            holder.itemView.setOnClickListener{
                                val startChat = Intent(context, ChatActivity::class.java)
                                startChat.putExtra("user_id", chat.userId)
                                context.startActivity(startChat)
                            }
                            holder.profilePic.setOnClickListener {
                                val intent = Intent(context, ImageViewActivity::class.java)
                                intent.putExtra("photo", image)
                                context.startActivity(intent)
                            }

                        }
                    }

                    override fun onCancelled(p0: DatabaseError) {

                    }
                })

            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

    }


    */


