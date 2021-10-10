package com.arewatechacademy.hubawe.Adapters

import android.app.ProgressDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.arewatechacademy.hubawe.Adapters.RequestAdapter.RequestHolder
import com.arewatechacademy.myapplication.R
import com.arewatechacademy.myapplication.Models.UsersModel
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.text.DateFormat
import java.util.*
import kotlin.collections.HashMap

class RequestAdapter
/**
 * Initialize a [RecyclerView.Adapter] that listens to a Firebase query. See
 * [FirebaseRecyclerOptions] for configuration options.
 *
 * @param options
 */
    (options: FirebaseRecyclerOptions<UsersModel?>) :
    FirebaseRecyclerAdapter<UsersModel?, RequestHolder>(options) {

    private var mAuth: FirebaseAuth? = null
    private var currentUser: String? = null
    //private val userOnline: String? = null
    //private var context: Context? = null
    //var pd: ProgressDialog? = null
    private var mRequestDatabase: DatabaseReference? = null
    private var mUsersDataBase: DatabaseReference? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.request_layout, parent, false)
        return RequestHolder(view)
    }

    override fun onBindViewHolder(holder: RequestHolder, position: Int, model: UsersModel) {

        //context = context
        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth!!.currentUser!!.uid
        mRequestDatabase = FirebaseDatabase.getInstance().reference.child("Friend_Requests").child("received_request")
        mUsersDataBase = FirebaseDatabase.getInstance().reference.child("Users")
        val uid = getRef(position).key

        //checking user id
        mRequestDatabase!!.child(currentUser!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

            //getting user details
            mUsersDataBase!!.child(uid!!)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val name = dataSnapshot.child("name").value.toString()
                        val status =
                            dataSnapshot.child("status").value.toString()
                        val image = dataSnapshot.child("profilePhoto").value.toString()
                        holder.name.text = name
                        holder.status.text = status

                        if (image != "default") {
                            Picasso.get().load(image).placeholder(R.drawable.avatar)
                                .into(holder.profilePic)
                        }

                        val context = holder.itemView.context
                        holder.acceptBtn.setOnClickListener {

                            acceptRequest(context, holder.requeststate, uid, holder.linearLayout)
                        }
                        holder.rejectBtn.setOnClickListener {
                            val builder = AlertDialog.Builder(context)
                            builder.setCancelable(true)
                            builder.setTitle("Decline Request??")
                            builder.setMessage("Decline $name's request??")
                            builder.setPositiveButton("Yes")
                            { _, _ -> declineRequest(context, holder.requeststate, uid, holder.linearLayout) }
                            builder.setNegativeButton("Cancel", null).show()



                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })

    }


    class RequestHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        var name: TextView
        var status: TextView
        var requeststate: TextView
        var linearLayout: LinearLayout
        var profilePic: CircleImageView
        var acceptBtn : Button
        var rejectBtn : Button


        init {
            name = itemView.findViewById(R.id.request_displayname)
            status = itemView.findViewById(R.id.request_status)
            requeststate = itemView.findViewById(R.id.requestConfirm)
            linearLayout = itemView.findViewById(R.id.linearLayout)
            profilePic = itemView.findViewById(R.id.requestImage)
            acceptBtn = itemView.findViewById(R.id.acceptBtn)
            rejectBtn = itemView.findViewById(R.id.rejectBtn)
        }

    }

    private fun declineRequest(
        context: Context,
        requeststate: TextView,
        userId: String,
        linearLayout: LinearLayout
    ) {

        val pd =ProgressDialog(context)
        val reference = FirebaseDatabase.getInstance().reference
        val currentUser = FirebaseAuth.getInstance().currentUser!!
        pd.setMessage("Declining request...")
        pd.setCanceledOnTouchOutside(false)
        pd.show()

        reference.child("Friend_Requests").child("sent_request")
            .child(userId).child(currentUser.uid).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    reference.child("Friend_Requests").child("received_request")
                        .child(currentUser.uid).child(userId).removeValue()
                        .addOnCompleteListener { po ->
                            if (po.isSuccessful) {

                                linearLayout.visibility = View.GONE
                                requeststate.visibility = View.VISIBLE
                                requeststate.text = "You decline the request"
                                pd.dismiss()

                                Toast.makeText(context,
                                    "Friend request declined", Toast.LENGTH_LONG).show()

                            }

                        }
                } else {
                    pd!!.dismiss()
                    Toast.makeText(context,
                        "error: failed to cancel Friend request", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun acceptRequest(
        context: Context,
        requestState: TextView,
        userId: String,
        linearLayout: LinearLayout
    ) {

        val pd =ProgressDialog(context)
        val reference = FirebaseDatabase.getInstance().reference
        val currentUser = FirebaseAuth.getInstance().currentUser!!

        pd.setMessage("Accepting request...")
        pd.setCanceledOnTouchOutside(false)
        pd.show()

        val date =
            DateFormat.getDateTimeInstance().format(Date())
        val friendMap = HashMap<String, Any>()
        friendMap["id"] = userId
        friendMap["date"] = date
        reference.child("Friends")
            .child(currentUser.uid).child(userId).setValue(friendMap)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    val friendMap2 = HashMap<String, Any>()
                    friendMap2["id"] = currentUser.uid
                    friendMap2["date"] = date
                    reference.child("Friends")
                        .child(userId).child(currentUser.uid)
                        .setValue(friendMap2).addOnCompleteListener { add ->

                            if (add.isSuccessful) {
                                reference.child("Friend_Requests")
                                    .child("sent_request")
                                    .child(userId).child(currentUser.uid).removeValue()
                                    .addOnCompleteListener { sent ->
                                        if (sent.isSuccessful) {
                                            reference.child("Friend_Requests")
                                                .child("received_request")
                                                .child(currentUser.uid).child(userId)
                                                .removeValue().addOnCompleteListener { po ->
                                                    if (po.isSuccessful) {

                                                        linearLayout.visibility = View.GONE
                                                        requestState.visibility = View.VISIBLE
                                                        requestState.text = "You are now friends"
                                                        pd.dismiss()

                                                        Toast.makeText(
                                                            context, "You are now friends", Toast.LENGTH_LONG).show()
                                                    }

                                                }
                                        }
                                    }
                            }
                        }
                } else {
                    Toast.makeText(
                        context, "error: pls check your internet connection", Toast.LENGTH_LONG
                    ).show()
                }
            }

    }
}