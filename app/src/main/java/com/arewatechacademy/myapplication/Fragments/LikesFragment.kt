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
import com.arewatechacademy.hubawe.Adapters.UsersAdapter
import com.arewatechacademy.myapplication.LikesActivity
import com.arewatechacademy.myapplication.Models.UsersModel
import com.arewatechacademy.myapplication.R
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.database.*

class LikesFragment : Fragment() {

    private lateinit var userAdapter: UsersAdapter
    private lateinit var mUsers: List<UsersModel>
    private lateinit var likeList: List<String>
    private lateinit var recyclerView: RecyclerView
    private lateinit var ref: DatabaseReference
    private lateinit var shimmerLayout: ShimmerFrameLayout
    private lateinit var noChats: TextView
    private lateinit var postId : String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_likes, container, false)

        recyclerView = view.findViewById(R.id.likeRecycler)
        recyclerView.layoutManager = LinearLayoutManager(context)
        postId = LikesActivity.postId

        shimmerLayout = view.findViewById(R.id.shimmer_layout)
        shimmerLayout.visibility = View.VISIBLE
        shimmerLayout.startShimmer()

        likeList = ArrayList()
        mUsers = ArrayList()
        noChats = view.findViewById(R.id.noChats)

        ref = FirebaseDatabase.getInstance().reference.child("Likes")
            .child(postId)

        ref.addValueEventListener(object : ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for (ds in snapshot.children){
                        val user = ds.key
                        (likeList as ArrayList<String>).add(user!!)
                    }
                }
                else{
                    noChats.visibility = View.VISIBLE
                    shimmerLayout.visibility = View.GONE
                    recyclerView.visibility = View.GONE
                }

                retrieveList(context)

            }

            override fun onCancelled(error: DatabaseError) {


            }
        })

        return view
    }

    private fun retrieveList(context: Context?) {

        val userRef = FirebaseDatabase.getInstance().reference.child("Users")

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for (user in snapshot.children){
                    val userId = user.getValue(UsersModel::class.java)

                    for (like in likeList){
                        if (userId!!.userId == like){
                            (mUsers as ArrayList).add(userId)
                        }

                    }

                    userAdapter= UsersAdapter(context!!, mUsers, true)
                    recyclerView.adapter = userAdapter
                    userAdapter.notifyDataSetChanged()

                    shimmerLayout.stopShimmer()
                    shimmerLayout.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {


            }
        })
    }
}