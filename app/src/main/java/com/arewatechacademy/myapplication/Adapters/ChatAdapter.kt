package com.arewatechacademy.myapplication.Adapters

import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

import com.arewatechacademy.myapplication.ImageViewActivity
import com.arewatechacademy.myapplication.Models.ChatModel
import com.arewatechacademy.myapplication.R
import com.squareup.picasso.Picasso
import java.util.*
import kotlin.collections.HashMap

class ChatAdapter(
    context: Context,
    mChat: List<ChatModel>
) : RecyclerView.Adapter<ChatAdapter.Holder>() {
    private val context: Context = context
    private val mChat: List<ChatModel> = mChat
    private val MSG_LEFT = 0
    private val MSG_RIGHT = 1

    private var currentUser: FirebaseUser? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {

        return if (viewType == MSG_RIGHT) {
            val view = LayoutInflater.from(context).inflate(R.layout.chat_sender, parent, false)
            Holder(view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.chat_receiver, parent, false)
            Holder(view)
        }
    }

    override fun getItemCount(): Int {
        return mChat.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        currentUser = FirebaseAuth.getInstance().currentUser

        val chat: ChatModel = mChat[position]

        setChatTIme(chat.timestamp, holder.date)



        if (chat.type == "text") {
            holder.textMsg!!.text = chat.message
        } else if (chat.type == "image") {
            holder.photoMsg!!.visibility = View.VISIBLE
            Picasso.get().load(chat.message).placeholder(R.drawable.loading).into(holder.photoMsg)
            holder.textMsg!!.visibility = View.GONE
        }

        if (chat.seen) {
            holder.seen!!.setImageResource(R.drawable.ic_seen)
        } else if (!chat.seen) {
            holder.seen!!.setImageResource(R.drawable.ic_sent)
        }

        if (chat.type == "image") {

            holder.photoMsg!!.setOnClickListener {

                val intent = Intent(context, ImageViewActivity::class.java)
                intent.putExtra("photo", chat.message)
                context.startActivity(intent)

            }
        }

        holder.relativeLayout.setOnLongClickListener {
            if (chat.sender == currentUser!!.uid){
                showActions(chat)
            }

            true
        }

    }

    private fun setChatTIme(timestamp: String?, date: TextView?) {

        //converting timestamp to actual time;
        val calendar = Calendar.getInstance(Locale.ENGLISH)
        calendar.timeInMillis = timestamp!!.toLong()

        val t24 = 86400000 //24hrs in milliseconds
        val t48 = t24 * 2  //48hrs in millisecond
        val chatTime: Long = timestamp.toLong() // time message is sent
        val timeCurrent = System.currentTimeMillis() - chatTime

        if ((System.currentTimeMillis() - chatTime) < t24) {
            val time = DateFormat.format("hh:mm aa", calendar).toString()
            date!!.text = time
        }
        if ((timeCurrent > t24) && (timeCurrent < t48)) {
            val time = DateFormat.format("hh:mm aa", calendar).toString()
            date!!.text = "yesterday $time"
        } else if (timeCurrent > t48) {
            val time = DateFormat.format("hh:mm aa dd-MM-yyyy", calendar).toString()
            date!!.text = time

        }

    }


    override fun getItemViewType(position: Int): Int {
        currentUser = FirebaseAuth.getInstance().currentUser

        return if (mChat[position].sender == currentUser!!.uid)
            MSG_RIGHT
        else
            MSG_LEFT
    }


    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var textMsg: TextView? = itemView.findViewById(R.id.messagetx)
        var date: TextView? = itemView.findViewById(R.id.date)
        var seen: ImageView? = itemView.findViewById(R.id.seentx)
        var photoMsg: ImageView? = itemView.findViewById(R.id.message_image)
        var relativeLayout: RelativeLayout = itemView.findViewById(R.id.relative)

    }

    private fun showActions(chat: ChatModel) {

        val actions = arrayOf<CharSequence>(
            "Delete"
        )
        val builder = AlertDialog.Builder(context)
            .setTitle("Select Action")
            .setItems(actions) { _, i ->
                if (i == 0) {
                    /*if (chat.type == "text") {

                        /*
                       val clipboardManager: ClipboardManager =(ClipboardManager!!) getSystemService(Context!!.CLIPBOARD_SERVICE) as ClipboardManager
                        val clipData = ClipData.newPlainText("copiedText", "text")
                        clipboardManager.setPrimaryClip(clipData)
                        Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()

                         */

                    }

                }
                if (i == 1) {*/
                    val dialog = AlertDialog.Builder(context)
                        .setTitle("Delete Message?")
                        .setMessage("This can't be undone..")
                        .setPositiveButton("Delete")
                        { _, _ -> deleteMessage(chat) }
                        .setNegativeButton("Cancel", null).show()
                }

            }
        builder.show()
    }

    private fun deleteMessage(chat: ChatModel) {

        val currentUser = FirebaseAuth.getInstance().currentUser!!.uid
        val sender = chat.sender
        val messgaeText = chat.message
        val ref = FirebaseDatabase.getInstance().reference.child("Messages")
        val query = ref.orderByChild("message").equalTo(messgaeText)

        if (sender == currentUser) {

            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    for (ds in p0.children) {
                        //delete message
                        val hashMap = HashMap<String, Any>()
                        hashMap["message"] = "Message deleted"

                        ds.ref.updateChildren(hashMap)
                    }
                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })
        } else {
            Toast.makeText(context, "You can only delete your own message", Toast.LENGTH_SHORT)
                .show()
        }

    }

}