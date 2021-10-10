package com.arewatechacademy.hubawe.Adapters

import android.app.ProgressDialog
import android.content.Intent
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.arewatechacademy.hubawe.Adapters.FriendsAdapter.ViewHolder
import com.arewatechacademy.myapplication.ChatActivity
import com.arewatechacademy.myapplication.ImageViewActivity
import com.arewatechacademy.myapplication.R
import com.arewatechacademy.myapplication.Models.UsersModel
import com.arewatechacademy.myapplication.ProfileActivity
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class FriendsAdapter
/**
 * Initialize a [RecyclerView.Adapter] that listens to a Firebase query. See
 * [FirebaseRecyclerOptions] for configuration options.
 *
 * @param options
 */
    (options: FirebaseRecyclerOptions<UsersModel?>) :
    FirebaseRecyclerAdapter<UsersModel?, ViewHolder>(options) {

    private var mFriendsDatabase: DatabaseReference? = null
    private var mUsersDataBase: DatabaseReference? = null
    var mAuth: FirebaseAuth? = null
    private var currentUser: String? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_user_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: UsersModel) {
        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth!!.currentUser!!.uid
        mFriendsDatabase = FirebaseDatabase.getInstance().reference.child("Friends")
        mUsersDataBase = FirebaseDatabase.getInstance().reference.child("Users")
        val userId = getRef(position).key
        val context = holder.itemView.context

        mFriendsDatabase!!.child(currentUser!!).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                mUsersDataBase!!.child(userId!!).addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()) {
                            val friend: UsersModel? = p0.getValue(
                                UsersModel::class.java)

                            val dialog = ProgressDialog(context)
                            dialog.setCanceledOnTouchOutside(false)
                            dialog.setMessage("Loading...")
                            //val friend = mUsers[position]
                            val name = friend!!.name
                            val image = friend.profilePhoto
                            val status = friend.status


                            holder.name.text = name
                            if (!image.equals("default"))
                                Picasso.get().load(image).placeholder(R.drawable.avatar)
                                    .into(holder.profilePic)
                            holder.status.text = status

                            if (friend.presence == "online") {
                                holder.online.visibility = View.VISIBLE
                            }

                            holder.itemView.setOnClickListener {
                                dialog.show()
                                val handler = Handler()
                                handler.postDelayed({ dialog.dismiss() }, 2500)

                                val actions = arrayOf<CharSequence>(
                                    "View Profile", "Send Message", "View profile pic"
                                )
                                val builder =
                                    AlertDialog.Builder(context)
                                        .setTitle("Select Action")
                                        .setItems(
                                            actions
                                        ) { dialog, i ->
                                            if (i == 0) {
                                                val intent =
                                                    Intent(context, ProfileActivity::class.java)
                                                intent.putExtra("user_id", friend.userId)
                                                context.startActivity(intent)
                                            }
                                            if (i == 1) {
                                                val chat = Intent(context, ChatActivity::class.java)
                                                chat.putExtra("user_id", friend.userId)
                                                context.startActivity(chat)
                                            }
                                            if (i == 2) {
                                                val intent =
                                                    Intent(context, ImageViewActivity::class.java)
                                                intent.putExtra("photo", friend.profilePhoto)
                                                context.startActivity(intent)
                                            }
                                        }
                                builder.show()
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


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var name: TextView
        var status: TextView
        var profilePic: CircleImageView
        var online: ImageView


        init {
            name = itemView.findViewById(R.id.user_displayname)
            status = itemView.findViewById(R.id.user_status)
            profilePic = itemView.findViewById(R.id.user_photo)
            online = itemView.findViewById(R.id.onlineImage)
        }

    }

    /*override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val dialog = ProgressDialog(context)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setMessage("Loading...")
        val friend = mUsers[position]
        val name = friend.name
        val image = friend.profilePhoto
        val status = friend.status


        holder.name.text = name
        if (!image.equals("default"))
            Picasso.get().load(image).placeholder(R.drawable.avatar).into(holder.profilePic)
        holder.status.text = status

        if (friend.presence == "online"){
            holder.online.visibility = View.VISIBLE
        }

        holder.itemView.setOnClickListener {
            dialog.show()
            val handler = Handler()
            handler.postDelayed({ dialog.dismiss() }, 2500)

            val context = holder.itemView.context
            val actions = arrayOf<CharSequence>(
                "View Profile", "Send Message", "View profile pic",
                "Remove User"
            )
            val builder =
                AlertDialog.Builder(context)
                    .setTitle("Select Action")
                    .setItems(
                        actions
                    ) { dialog, i ->
                        if (i == 0) {
                            val intent =
                                Intent(context, ProfileActivity::class.java)
                            intent.putExtra("user_id", friend.userId)
                            context.startActivity(intent)
                        }
                        if (i == 1) {
                            val chat = Intent(context, ChatActivity::class.java)
                            chat.putExtra("user_id", friend.userId)
                            context.startActivity(chat)
                        }
                    }
            builder.show()
        }
    }*/
}