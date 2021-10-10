package com.arewatechacademy.myapplication.Fragments


import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arewatechacademy.hubawe.Adapters.ChatslistsAdapter
import com.arewatechacademy.myapplication.Models.ChatModel
import com.arewatechacademy.myapplication.Models.UsersModel

import com.arewatechacademy.myapplication.R
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

/**
 * A simple [Fragment] subclass.
 */
class ChatFragment : Fragment() {
    //private lateinit var chatAdapter: ChatListAdapter
    private lateinit var chats: ChatslistsAdapter
    private lateinit var mUsers: List<UsersModel>
    private lateinit var mChatlist: List<ChatModel>
    private lateinit var recyclerView: RecyclerView
    private lateinit var ref: DatabaseReference
    private lateinit var shimmerLayout: ShimmerFrameLayout
    private lateinit var noChats: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_chat, container, false)

        recyclerView = view.findViewById(R.id.chatRecycler)
        recyclerView.layoutManager = LinearLayoutManager(context)

        shimmerLayout = view.findViewById(R.id.shimmer_layout)
        shimmerLayout.visibility = View.VISIBLE
        shimmerLayout.startShimmer()

        mChatlist = ArrayList()
        mUsers = ArrayList()
        noChats = view.findViewById(R.id.noChats)


        val currentUser = FirebaseAuth.getInstance().currentUser!!
        ref = FirebaseDatabase.getInstance().reference.child("Chats")
            .child(currentUser.uid)

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                if (p0.exists()){
                    (mChatlist as ArrayList).clear()
                    for (dataSnapshot in p0.children) {
                        val chatlist = dataSnapshot.getValue(ChatModel::class.java)
                        (mChatlist as ArrayList).add(chatlist!!)

                    }
                    retrieveFiends(mUsers as ArrayList<UsersModel>, context!!)
                }
                else{
                    noChats.visibility = View.VISIBLE
                    shimmerLayout.visibility = View.GONE
                    recyclerView.visibility = View.GONE
                }


            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

        return view
    }


    private fun retrieveFiends(mUsers: ArrayList<UsersModel>, context: Context) {
        val chatRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")

        chatRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                (mUsers).clear()

                for (snapshot in p0.children) {
                    val user: UsersModel? = snapshot.getValue(
                        UsersModel::class.java)

                    for (chat in mChatlist) {
                        if (user!!.userId == chat.id)
                            (mUsers).add(user)
                        if (mUsers.isEmpty()){
                            //noChats.visibility = View.VISIBLE
                            shimmerLayout.visibility = View.GONE
                            recyclerView.visibility = View.GONE
                        }
                    }

                }
                chats= ChatslistsAdapter(context, mUsers)
                recyclerView.adapter = chats
                chats.notifyDataSetChanged()

                shimmerLayout.stopShimmer()
                shimmerLayout.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE

            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

}
